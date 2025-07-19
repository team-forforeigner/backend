package com.codingrecipe.board.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3UploaderService {

    // AWS SDK v2의 S3Client 주입
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket-name}")
    private String bucket;

    public String upload(MultipartFile multipartFile, String dirName) throws IOException {
        File uploadFile = convert(multipartFile)
                .orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File 전환 실패"));
        return upload(uploadFile, dirName);
    }

    private String upload(File uploadFile, String dirName) {
        String fileName = dirName + "/" + uploadFile.getName();
        String uploadImageUrl = putS3(uploadFile, fileName);

        removeNewFile(uploadFile);  // 로컬에 생성된 임시 파일 삭제

        return uploadImageUrl;
    }

    // S3 버킷에 파일을 올리는 로직 (AWS SDK v2)
    private String putS3(File uploadFile, String fileName) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                // .acl("public-read") // ACL 관련 에러가 발생했다면 이 줄은 주석 처리 또는 삭제
                .build();

        s3Client.putObject(request, RequestBody.fromFile(uploadFile));

        // 업로드된 파일의 URL 생성
        return s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(fileName)).toExternalForm();
    }

    // 로컬에 저장된 임시 파일 삭제
    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            log.info("로컬 임시 파일이 삭제되었습니다.");
        } else {
            log.info("로컬 임시 파일이 삭제되지 못했습니다.");
        }
    }

    // 임시 파일을 프로젝트 내부에 생성하여 권한 문제 해결
    private Optional<File> convert(MultipartFile file) throws IOException {
        // 1. 원본 파일 이름이 null일 경우를 대비한 방어 코드
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            log.warn("원본 파일 이름이 null입니다. UUID로 대체합니다.");
            originalFilename = UUID.randomUUID().toString();
        }

        // 2. 고유한 파일 이름 생성
        String uniqueFileName = UUID.randomUUID() + "_" + originalFilename;

        // 3. 시스템 임시 폴더 대신 프로젝트 루트 경로에 임시 파일 생성
        File convertFile = new File(uniqueFileName);
        if (convertFile.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            }
            return Optional.of(convertFile);
        }
        return Optional.empty();
    }
}
