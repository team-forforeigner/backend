package com.codingrecipe.board.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    @Value("${cloud.aws.region.static}")
    private String region; // AWS S3 리전 설정

    @Bean
    public S3Client s3Client() {
        // EC2의 IAM Role을 사용하므로, 별도의 자격 증명 설정이 필요 없습니다.
        return S3Client.builder()
                .region(Region.of(region))
                .build();
    }

    // URL 생성을 위한 S3Presigner Bean
    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(region))
                .build();
    }

}
