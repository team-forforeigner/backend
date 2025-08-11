// AWS S3 파일 업로드 및 Presigned URL 생성을 처리하는 서비스
package com.codingrecipe.board.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
//@Profile("!local") // 'local' 프로필이 아닐 때만 이 서비스를 활성화
public class S3UploaderService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket-name}")
    private String bucket; // S3 버킷 이름

    @Value("${cloud.aws.region.static}")
    private String region; // 주입받음

    /**
     * S3 객체에 대한 미리 서명된 URL(Presigned URL)을 생성
     * (제한된 시간 동안만 개인 S3 객체에 접근할 수 있는 임시 URL)
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
                    .signatureDuration(Duration.ofMinutes(10)) // URL 유효 시간 10분으로 설정
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
            // 시스템 기본 임시 디렉토리에 임시 파일 생성
            tempFilePath = Files.createTempFile("s3-upload-", multipartFile.getOriginalFilename());

            // MultipartFile의 내용을 임시 파일에 씁니다.
            multipartFile.transferTo(tempFilePath);
            File uploadFile = tempFilePath.toFile();

            // S3에 업로드할 파일 이름 생성
            String fileName = dirName + "/" + UUID.randomUUID() + "_" + uploadFile.getName();

            // S3에 파일 업로드
            putS3(uploadFile, fileName);

            // S3에 저장된 파일 경로(key) 반환
            return fileName;
        } finally {
            // 업로드 성공/실패 여부와 관계없이 임시 파일 삭제
            if (tempFilePath != null) {
                removeNewFile(tempFilePath.toFile());
            }
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
     * S3에서 파일을 다운로드하여 로컬 임시 파일로 저장
     */
    /**
     * S3에서 파일을 다운로드하여 로컬 임시 파일로 저장
     * 사용이 끝나면 반드시 removeNewFile() 호출하여 삭제해야 함
     */
    public File download(String fileKey) {
        if (fileKey == null || fileKey.isEmpty()) {
            log.warn("파일 키가 null이거나 비어 있습니다.");
            return null;
        }
        try {
            // 임시 파일 생성 (확장자는 원본 key에서 가져오기)
            String suffix = "";
            int dotIndex = fileKey.lastIndexOf('.');
            if (dotIndex > -1) {
                suffix = fileKey.substring(dotIndex);
            }
            File tempFile = File.createTempFile("s3-download-", suffix);

            // S3에서 파일 다운로드
            s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileKey)
                    .build(), tempFile.toPath());

            log.info("S3 파일 다운로드 완료: {}", tempFile.getAbsolutePath());
            return tempFile;

        } catch (IOException e) {
            log.error("S3 파일 다운로드 중 오류 발생: {}", fileKey, e);
            return null;
        }
    }

    /**
     * S3에서 파일 다운로드 후 바이트 배열로 반환
     * 호출 후 임시 파일은 내부에서 삭제 처리
     */
    public byte[] getFileAsBytes(String fileKey) {
        File tempFile = null;
        try {
            tempFile = download(fileKey);
            if (tempFile == null || !tempFile.exists()) {
                throw new IllegalArgumentException("파일이 존재하지 않습니다.");
            }
            return Files.readAllBytes(tempFile.toPath());
        } catch (IOException e) {
            log.error("파일 바이트 변환 실패: {}", fileKey, e);
            throw new RuntimeException("파일 바이트 변환 실패", e);
        } finally {
            if (tempFile != null) {
                removeNewFile(tempFile);
            }
        }
    }

    /**
     * S3에서 파일 다운로드 후 콘텐츠 타입 반환
     * 호출 후 임시 파일은 내부에서 삭제 처리
     */
    public String getFileContentType(String fileKey) {
        File tempFile = null;
        try {
            tempFile = download(fileKey);
            if (tempFile == null || !tempFile.exists()) {
                throw new IllegalArgumentException("파일이 존재하지 않습니다.");
            }
            return Files.probeContentType(tempFile.toPath());
        } catch (IOException e) {
            log.error("파일 콘텐츠 타입 조회 실패: {}", fileKey, e);
            throw new RuntimeException("파일 콘텐츠 타입 조회 실패", e);
        } finally {
            if (tempFile != null) {
                removeNewFile(tempFile);
            }
        }
    }


    /**
     * 업로드 과정에서 생성된 로컬 임시 파일을 삭제
     */
    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            log.info("로컬 임시 파일이 삭제되었습니다");
        } else {
            log.info("로컬 임시 파일이 삭제되지 못했습니다");
        }
    }

    /**
     * MultipartFile을 로컬 시스템의 임시 File 객체로 변환
     */
    private Optional<File> convert(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            log.warn("원본 파일 이름이 null입니다. UUID로 대체합니다");
            originalFilename = UUID.randomUUID().toString();
        }
//
        Path tempFile = Files.createTempFile("s3-upload-", originalFilename);

        // MultipartFile의 내용을 임시 파일에 씁니다.
        file.transferTo(tempFile);

        return Optional.of(tempFile.toFile());
    }

    // S3 파일 목록 조회
    public List<Map<String, Object>> listImages() {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .build();

        ListObjectsV2Response result = s3Client.listObjectsV2(request);

        return result.contents().stream()
                .map(s -> {
                    Map<String, Object> fileData = new HashMap<>();
                    fileData.put("filename", s.key());
                    fileData.put("url", String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, s.key()));
                    fileData.put("size", s.size());
                    fileData.put("lastModified", s.lastModified().toString());
                    return fileData;
                })
                .toList();
    }

    // S3 파일 삭제
    public void deleteFile(String fileKey) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .build();
        s3Client.deleteObject(deleteRequest);
    }

    // bucket 이름 getter
    public String getBucketName() {
        return bucket;
    }

    // region getter
    public String getRegion() {
        return region;
    }

}
