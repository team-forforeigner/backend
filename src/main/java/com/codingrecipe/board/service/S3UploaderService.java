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
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile("!local") // 'local' 프로필이 아닐 때만 이 서비스를 활성화
public class S3UploaderService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket-name}")
    private String bucket; // S3 버킷 이름

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
     * MultipartFile을 받아 S3에 업로드
     */
//    public String upload(MultipartFile multipartFile, String dirName) throws IOException {
//        // MultipartFile을 로컬 File 객체로 변환
//        File uploadFile = convert(multipartFile)
//                .orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File 전환 실패"));
//        return upload(uploadFile, dirName);
//    }

    /**
     * 로컬 File을 S3에 업로드하고 로컬 파일은 삭제
     */
//    private String upload(File uploadFile, String dirName) {
//        String fileName = dirName + "/" + uploadFile.getName(); // S3에 저장될 파일 경로 생성
//        putS3(uploadFile, fileName); // S3에 파일 업로드
//        removeNewFile(uploadFile); // 로컬에 생성된 임시 파일 삭제
//        return fileName; // S3에 저장된 파일 경로(key) 반환
//    }
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
                Files.deleteIfExists(tempFilePath);
                log.info("로컬 임시 파일이 삭제되었습니다.");
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
//        // 파일 이름 중복을 피하기 위해 UUID를 앞에 붙임g
//        String uniqueFileName = UUID.randomUUID() + "_" + originalFilename;
//        File convertFile = new File(uniqueFileName);
//        if (convertFile.createNewFile()) {
//            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
//                fos.write(file.getBytes());
//            }
//            return Optional.of(convertFile);
//        }
//        return Optional.empty();
        Path tempFile = Files.createTempFile("s3-upload-", originalFilename);

        // MultipartFile의 내용을 임시 파일에 씁니다.
        file.transferTo(tempFile);

        return Optional.of(tempFile.toFile());
    }
}
