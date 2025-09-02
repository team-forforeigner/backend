package com.codingrecipe.board.service;

import com.codingrecipe.board.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile("!local") // 'local' 프로필이 아닐 때만 이 서비스를 활성화
public class S3UploaderService {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket-name}")
    private String s3Bucket;

    @Value("${cloud.aws.region.static}")
    private Region s3BucketRegion;

    private final S3Presigner s3Presigner;

    /**
     * S3 객체에 대한 미리 서명된 URL(Presigned URL)을 생성
     */
    public String generatePresignedUrl(String fileKey) {
        if (fileKey == null || fileKey.isEmpty()) {
            return null;
        }
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(s3Bucket)
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
     * 스트림 형식의 MultipartFile을 S3에 업로드
     */
    public String uploadImage(MultipartFile file, String category) throws IOException {
        String uuid = UUID.randomUUID().toString();
        String extension = FileUtil.getFileExtension(file.getOriginalFilename());
        String yyyyMM = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String fileKey = String.format("uploads/%s/%s/%s%s", category, yyyyMM, uuid, extension);

        try (InputStream is = file.getInputStream()) {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(s3Bucket)
                            .key(fileKey)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(is, file.getSize())
            );
            return fileKey;
        } catch (software.amazon.awssdk.services.s3.model.S3Exception e) {
            // S3에서 응답을 제대로 받았지만 권한/버킷 정책 문제 등
            log.error("S3 업로드 실패 (버킷={}, 파일 키={}) - {}", s3Bucket, fileKey, e.awsErrorDetails().errorMessage(), e);
            throw e;
        } catch (SdkClientException e) {
            // 네트워크 문제, 자격 증명 문제 등
            log.error("S3 SDK 클라이언트 오류 (버킷={}, 파일 키={})", s3Bucket, fileKey, e);
            throw e;
        } catch (Exception e) {
            // 기타 예외
            log.error("알 수 없는 오류 발생 (버킷={}, 파일 키={})", s3Bucket, fileKey, e);
            throw e;
        }
    }


    /**
     * S3에서 파일을 다운로드하고 InputStream(바이트)으로 반환
     */
    public byte[] downloadAsBytes(String fileKey) {
        if (fileKey == null || fileKey.isEmpty()) {
            log.warn("파일 키가 null이거나 비어 있습니다.");
            return new byte[0];
        }
        try (InputStream in = s3Client.getObject(GetObjectRequest.builder()
                .bucket(s3Bucket)
                .key(fileKey)
                .build())) {
            return in.readAllBytes();
        } catch (IOException e) {
            log.error("S3 파일 다운로드 중 오류 발생: {}", fileKey, e);
            throw new RuntimeException("파일 다운로드 실패", e);
        }
    }

    /**
     * S3 전체 이미지 목록 조회 (테스트용)
     */
    public List<Map<String, Object>> listImages() {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(s3Bucket)
                .build();

        ListObjectsV2Response result = s3Client.listObjectsV2(request);

        return result.contents().stream()
                .map(s -> {
                    Map<String, Object> fileData = new HashMap<>();
                    fileData.put("filename", s.key());
                    fileData.put("url", String.format("https://%s.s3.%s.amazonaws.com/%s", s3Bucket, s3BucketRegion.id(), s.key()));
                    fileData.put("size", s.size());
                    fileData.put("lastModified", s.lastModified().toString());
                    return fileData;
                })
                .toList();
    }

    /**
     * S3 이미지 수정
     * - 기존 이미지를 삭제하고 새로운 이미지를 업로드합니다.
     */
    @Transactional
    public String updateImage(String fileKey, MultipartFile newFile, String category) throws IOException {
        try {
            String newFileKey = uploadImage(newFile, category);

            try {
                deleteImage(fileKey);
            } catch (S3Exception e) {
                // 삭제 실패 시 로깅만 하고, 예외를 던지지 않음
                log.warn("기존 파일 삭제 실패: {} (새 파일은 이미 업로드됨)", fileKey, e);
            }

            return newFileKey;

        } catch (S3Exception e) {
            log.error("S3 수정 중 오류 발생: {}", fileKey, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "S3 수정 실패", e);
        } catch (IOException e) {
            log.error("파일 업로드 중 오류 발생: {}", newFile.getOriginalFilename(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드 실패", e);
        } catch (Exception e) {
            log.error("이미지 수정 중 알 수 없는 오류 발생", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 수정 실패", e);
        }
    }

    /**
     * S3 이미지 삭제
     * - 파일 키를 통해 S3에서 이미지를 삭제합니다.
     */
    public void deleteImage(String fileKey) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(s3Bucket)
                .key(fileKey)
                .build();
        try {
            s3Client.deleteObject(deleteRequest);
        } catch (S3Exception e) {
            if ("NoSuchKey".equals(e.awsErrorDetails().errorCode())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "파일이 존재하지 않습니다: " + fileKey);
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제 중 오류 발생", e);
        }
    }

    /**
     * S3 버킷에 저장된 파일의 URL을 생성
     * - 클라이언트에 단순 반환하는 용도
     */
    /*public String buildFileUrl(String fileKey) {
        if (fileKey == null || fileKey.isEmpty()) {
            return null;
        }
        return String.format("https://%s.s3.%s.amazonaws.com/%s", s3Bucket, s3BucketRegion.id(), fileKey);
    }*/


}
