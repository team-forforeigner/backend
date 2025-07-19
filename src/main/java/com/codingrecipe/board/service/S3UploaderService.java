// * =================================================================
// * [배포 시 주석 해제]
// * AWS S3 파일 업로드를 위한 서비스입니다.
// * =================================================================
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

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket-name}")
    private String bucket;

    // MultipartFile을 S3에 업로드하는 메소드
    public String upload(MultipartFile multipartFile, String dirName) throws IOException {
        File uploadFile = convert(multipartFile)
                .orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File 전환 실패"));
        return upload(uploadFile, dirName);
    }

    // 파일을 S3에 업로드하고 URL을 반환
    private String upload(File uploadFile, String dirName) {
        String fileName = dirName + "/" + UUID.randomUUID() + "_" + uploadFile.getName();
        String uploadImageUrl = putS3(uploadFile, fileName);

        removeNewFile(uploadFile);  // 로컬에 생성된 File 삭제

        return uploadImageUrl;      // 업로드된 파일의 S3 URL 주소 반환
    }

    // S3 버킷에 파일을 올리는 로직
    private String putS3(File uploadFile, String fileName) {
        /*amazonS3Client.putObject(
                new PutObjectRequest(bucket, fileName, uploadFile)
                        .withCannedAcl(CannedAccessControlList.PublicRead) // PublicRead 권한으로 업로드
        );
        return amazonS3Client.getUrl(bucket, fileName).toString();*/

        // [추가!] [AWS SDK v2 사용 시]
        // 설명 : 임시로 위 코드를 주석 처리하고, AWS SDK v2를 사용하여 S3에 파일을 업로드하는 로직으로 변경합니다.
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .build();

        s3Client.putObject(request, RequestBody.fromFile(uploadFile));

        String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucket,
                s3Client.serviceClientConfiguration().region().id(),
                fileName
        );

        return fileUrl;

    }

    // 로컬에 저장된 임시 파일 삭제
    private void removeNewFile(File targetFile) {
        if(targetFile.delete()) {
            log.info("로컬 파일이 삭제되었습니다.");
        }else {
            log.info("로컬 파일이 삭제되지 못했습니다.");
        }
    }

    // MultipartFile -> File 변환
    // [수정!] EC2 홈 디렉터리 내 tmp 폴더에 임시 파일을 저장하도록 변경했습니다. (EC2에 java.io.tmpdir 쓰기 권한이 없습니다)
    //       (예: /home/ec2-user/tmp)
    private Optional<File> convert(MultipartFile file) throws IOException {
        // 홈 디렉터리 내 tmp 경로 지정 (예: /home/ec2-user/tmp)
        String homeTmpDir = System.getProperty("user.home") + "/tmp";

        // tmp 폴더가 없으면 생성
        File dir = new File(homeTmpDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 파일명 분리 (확장자 포함)
        String originalFilename = file.getOriginalFilename();
        String prefix = "tempfile";
        String suffix = null;

        if (originalFilename != null && originalFilename.contains(".")) {
            prefix = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
            suffix = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }

        // 임시 파일 생성 (지정 폴더 아래)
        File convertFile = File.createTempFile(prefix, suffix, dir);

        try (FileOutputStream fos = new FileOutputStream(convertFile)) {
            fos.write(file.getBytes());
        }

        return Optional.of(convertFile);
    }

}
