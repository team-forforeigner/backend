package com.codingrecipe.board.controller;

import com.codingrecipe.board.dto.ApiResponseDto;
import com.codingrecipe.board.dto.AnalysisResponse;
import com.codingrecipe.board.dto.DescriptionResponse;
import com.codingrecipe.board.dto.FinalResponseDTO;
import com.codingrecipe.board.service.S3UploaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

    // S3 버킷 내 챗봇 이미지가 저장될 폴더 경로
    @Value("${cloud.aws.s3.folder.analysis}")
    private String analysisFolder;

    @Value("${ai-server.access-client-id}")
    private String accessClientId;

    @Value("${ai-server.access-client-secret}")
    private String accessClientSecret;

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ApiResponseDto<FinalResponseDTO>> analyzeImage(
            @RequestParam("file") MultipartFile imageFile,
            @RequestParam("type") String type) {

        // --- 1. S3 업로드 및 Presigned URL 생성 ---
        String fileKey = s3UploaderService
                .map(uploader -> {
                    try {
                        return uploader.uploadImage(imageFile, analysisFolder);
                    } catch (IOException e) {
                        throw new RuntimeException("S3 업로드 실패", e);
                    }
                })
                .orElse("s3-disabled-in-local");

        String s3Url = s3UploaderService.map(uploader -> uploader.generatePresignedUrl(fileKey))
                .orElse("s3-disabled-in-local");

        System.out.println("S3 Presigned URL 생성 완료: " + s3Url);
        System.out.println("AI 서버로 전송할 type: " + type);

        // --- 2. AI 서버로 S3 URL을 포함한 JSON 요청 전송 ---
        WebClient webClient = webClientBuilder.baseUrl("https://ai.navoodiai.site").build();

        // 요청 본문에 들어갈 JSON 문자열 생성
        String requestBody = String.format("{\"image_url\":\"%s\", \"type\":\"%s\"}", s3Url, type);

        // --- 3. YOLO 분석 요청 (JSON 방식) ---
        return webClient.post()
                .uri("/api/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .header("CF-Access-Client-Id", accessClientId)
                .header("CF-Access-Client-Secret", accessClientSecret)
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .doOnNext(body -> System.out.println("AI 서버 에러 응답 본문: " + body))
                                .then(Mono.error(new RuntimeException("AI 서버 요청 실패: " + clientResponse.statusCode()))))
                .bodyToMono(AnalysisResponse.class)
                .doOnNext(response -> System.out.println("AI 서버 응답: " + response))
                .flatMap(aiResponse -> {
                    // --- 4. 응답 처리 및 설명 요청 (기존과 동일) ---
                    List<String> detectedObjects = aiResponse.detectedObjects();

                    if (detectedObjects == null || detectedObjects.isEmpty()) {
                        FinalResponseDTO response = new FinalResponseDTO(s3Url, detectedObjects, "탐지된 객체가 없습니다.");
                        return Mono.just(ApiResponseDto.success(response));
                    }

                    String targetObject = detectedObjects.get(0);

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