package com.codingrecipe.board.controller;

import com.codingrecipe.board.dto.ApiResponseDto;
import com.codingrecipe.board.dto.AnalysisResponse;
import com.codingrecipe.board.dto.DescriptionResponse;
import com.codingrecipe.board.dto.FinalResponseDTO;
import com.codingrecipe.board.service.S3UploaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Profile("!local")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnalysisController {

    private final WebClient.Builder webClientBuilder;
    private final Optional<S3UploaderService> s3UploaderService;

    @Value("${ai-server.access-client-id}")
    private String accessClientId;

    @Value("${ai-server.access-client-secret}")
    private String accessClientSecret;

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ApiResponseDto<FinalResponseDTO>> analyzeImage(
            @RequestParam("file") MultipartFile imageFile,
            @RequestParam("type") String type) throws IOException {

        // --- 1. S3 업로드 -> 파일 키(key) 반환 ---
        String fileKey = s3UploaderService
                .map(uploader -> {
                    try {
                        return uploader.uploadImage(imageFile, "chatbot");
                    } catch (IOException e) {
                        throw new RuntimeException("S3 업로드 실패", e);
                    }
                })
                .orElse("s3-disabled-in-local");

        System.out.println("S3 업로드 완료. fileKey: " + fileKey);

        // 최종 응답에 담길 S3 Presigned URL 생성
        String s3Url = s3UploaderService.map(uploader -> uploader.generatePresignedUrl(fileKey))
                .orElse("s3-disabled-in-local");

        // --- 2. 바이트 배열 추출 (S3에서 읽기) ---
        byte[] imageBytes = s3UploaderService
                .map(uploader -> uploader.downloadAsBytes(fileKey))
                .orElse(imageFile.getBytes());

        System.out.println("이미지 바이트 배열 크기: " + imageBytes.length + " bytes");
        System.out.println("원본 파일명: " + imageFile.getOriginalFilename());
        System.out.println("Content Type: " + imageFile.getContentType());
        System.out.println("전송할 type 파라미터: " + type);

        // --- 3. RestTemplate을 사용한 multipart 요청 ---
        return Mono.fromCallable(() -> {
                    try {
                        RestTemplate restTemplate = new RestTemplate();

                        // 헤더 설정
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                        headers.set("CF-Access-Client-Id", accessClientId);
                        headers.set("CF-Access-Client-Secret", accessClientSecret);

                        // 파일 리소스 생성 (filename을 올바르게 설정)
                        ByteArrayResource fileResource = new ByteArrayResource(imageBytes) {
                            @Override
                            public String getFilename() {
                                return imageFile.getOriginalFilename();
                            }
                        };

                        // MultiValueMap으로 form 데이터 구성
                        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                        body.add("file", fileResource);
                        body.add("type", type);

                        // HttpEntity 생성
                        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

                        System.out.println("RestTemplate 요청 구성 완료");

                        // POST 요청 실행
                        ResponseEntity<AnalysisResponse> response = restTemplate.postForEntity(
                                "https://ai.navoodiai.site/api/analyze",
                                requestEntity,
                                AnalysisResponse.class
                        );

                        System.out.println("AI 서버 응답 상태: " + response.getStatusCode());
                        System.out.println("AI 서버 응답: " + response.getBody());

                        return response.getBody();

                    } catch (Exception e) {
                        System.out.println("RestTemplate 요청 중 오류: " + e.getMessage());
                        throw new RuntimeException("AI 서버 통신 중 오류", e);
                    }
                })
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
                .flatMap(aiResponse -> {
                    List<String> detectedObjects = aiResponse.detectedObjects();

                    if (detectedObjects == null || detectedObjects.isEmpty()) {
                        FinalResponseDTO response = new FinalResponseDTO(s3Url, detectedObjects, "탐지된 객체가 없습니다.");
                        return Mono.just(ApiResponseDto.success(response));
                    }

                    String targetObject = detectedObjects.get(0);

                    // --- 4. 설명 요청 (WebClient 유지) ---
                    WebClient webClient = webClientBuilder.baseUrl("https://ai.navoodiai.site").build();

                    return webClient.post()
                            .uri(uriBuilder -> uriBuilder.path("/api/describe")
                                    .queryParam("type", type)
                                    .build())
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("CF-Access-Client-Id", accessClientId)
                            .header("CF-Access-Client-Secret", accessClientSecret)
                            .body(BodyInserters.fromValue("{\"object_name\":\"" + targetObject + "\"}"))
                            .retrieve()
                            .bodyToMono(DescriptionResponse.class)
                            .map(descriptionResponse -> {
                                String description = descriptionResponse.description();
                                FinalResponseDTO finalResponse = new FinalResponseDTO(s3Url, detectedObjects, description);
                                return ApiResponseDto.success(finalResponse);
                            });
                });
    }
}