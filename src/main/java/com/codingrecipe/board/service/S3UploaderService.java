package com.codingrecipe.board.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile("!local")
public class S3UploaderService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket-name}")
    private String bucket;

    /**
     * S3 객체에 대한 미리 서명된 URL을 생성
     */
    public String generatePresignedUrl(String fileKey) {
        if (fileKey == null || fileKey.isEmpty()) {
            return null;
        }
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileKey)
                    .build();

            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
            log.info("Presigned URL 생성 완료: {}", presignedGetObjectRequest.url());
            return presignedGetObjectRequest.url().toString();

        } catch (Exception e) {
            log.error("Presigned URL 생성 중 오류 발생: {}", fileKey, e);
            return null;
        }
    }

    /**
     * MultipartFile을 받아 S3에 업로드하고 로컬 임시 파일을 삭제
     */
    public String upload(MultipartFile multipartFile, String dirName) throws IOException {
        Path tempFilePath = null;
        try {
            tempFilePath = Files.createTempFile("s3-upload-", multipartFile.getOriginalFilename());
            multipartFile.transferTo(tempFilePath);
            File uploadFile = tempFilePath.toFile();
            String fileName = dirName + "/" + UUID.randomUUID() + "_" + uploadFile.getName();
            putS3(uploadFile, fileName);
            return fileName;
        } finally {
            if (tempFilePath != null) {
                Files.deleteIfExists(tempFilePath);
                log.info("로컬 임시 파일이 삭제되었습니다.");
            }
        }
    }

    /**
     * S3에 저장된 파일을 byte 배열로 다운로드하는 메소드
     */
    public byte[] downloadAsBytes(String fileKey) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileKey)
                    .build();
            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
            return objectBytes.asByteArray();
        } catch (Exception e) {
            log.error("S3 파일 다운로드 중 오류 발생: key={}", fileKey, e);
            throw new RuntimeException("S3 파일 다운로드 실패", e);
        }
    }

    /**
     * S3Client를 사용하여 파일을 S3 버킷에 업로드
     */
    private void putS3(File uploadFile, String fileName) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .build();
        s3Client.putObject(request, RequestBody.fromFile(uploadFile));
    }

    /**
     * S3에서 파일을 삭제하는 메서드
     */
    public void delete(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) {
            log.warn("삭제할 S3 파일 키가 비어있습니다.");
            return;
        }
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileKey)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            log.info("S3 파일 삭제 완료: {}", fileKey);
        } catch (Exception e) {
            log.error("S3 파일 삭제 중 오류 발생: key={}", fileKey, e);
        }
    }

    /**
     * 전체 S3 URL에서 파일 키만 추출하는 헬퍼 메서드
     */
    public String extractFileKeyFromUrl(String fileUrl) {
        if(fileUrl == null || fileUrl.isBlank()){
            return null;
        }
        try {
            URL url = new URL(fileUrl);
            return url.getPath().substring(1);
        } catch (Exception e) {
            log.warn("S3 URL에서 파일 키를 추출하는 데 실패했습니다: {}", fileUrl);
            return null;
        }
    }
}
