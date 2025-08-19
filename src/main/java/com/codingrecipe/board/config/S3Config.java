// AWS S3 클라이언트 및 Presigner 설정 관리
// 'local' 프로필이 아닐 경우에만 활성화됨
package com.codingrecipe.board.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Profile("!local") // 'local' 프로필이 아닐 때만 이 설정을 활성화
@Configuration
public class S3Config {

    @Value("${spring.cloud.aws.s3.bucket-name}")
    private String bucket; // 버킷 이름

    @Value("${spring.cloud.aws.region.static}")
    private String region; // AWS 리전(지역)

    @Bean
    public String s3Bucket() {
        return bucket;
    }

    @Bean
    public Region s3BucketRegion() {
        return Region.of(region);
    }

    /**
     * S3와 통신하기 위한 클라이언트 빈을 생성
     * 파일 업로드, 삭제 등 S3의 기본 작업을 처리
     */
    @Bean
    public S3Client s3Client() {

        // S3 클라이언트 빌더를 사용하여 클라이언트 객체 생성
        return S3Client.builder()
                .region(Region.of(region)) // S3 버킷이 위치한 리전 설정
                .build();
    }

    /**
     * S3 객체에 대한 미리 서명된 URL를 생성하기 위한 빈
     * 클라이언트가 개인 S3 객체에 임시로 접근할 수 있는 URL을 생성할 때 사용
     */
    @Bean
    public S3Presigner s3Presigner() {

        // S3 Presigner 빌더를 사용하여 객체 생성
        return S3Presigner.builder()
                .region(Region.of(region))
                .build();
    }
}
