package com.codingrecipe.board.controller;

import com.codingrecipe.board.dto.AnalysisResponse;
import com.codingrecipe.board.dto.FinalResponseDTO;
import com.codingrecipe.board.service.S3UploaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
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

@Profile("!local")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnalysisController {

    private final WebClient.Builder webClientBuilder;
    private final S3UploaderService s3UploaderService;

    // --- application.yml에서 Cloudflare 서비스 토큰 정보 주입 ---
    @Value("${ai-server.access-client-id}")
    private String accessClientId;

    @Value("${ai-server.access-client-secret}")
    private String accessClientSecret;
    // ----------------------------------------------------------------

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<FinalResponseDTO> analyzeImage(@RequestParam("image") MultipartFile imageFile) throws IOException {

        String s3Url = s3UploaderService.upload(imageFile, "images");
        System.out.println("S3 업로드 완료. URL: " + s3Url);

        WebClient webClient = webClientBuilder.baseUrl("https://ai.navoodiai.site").build();

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", imageFile.getResource());

        return webClient.post()
                .uri("/api/analyze")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                // --- Cloudflare 인증 헤더 2개 추가 ---
                .header("CF-Access-Client-Id", accessClientId)
                .header("CF-Access-Client-Secret", accessClientSecret)
                // -------------------------------------------
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(AnalysisResponse.class)
                .flatMap(aiResponse -> {
                    List<String> detectedObjects = aiResponse.detectedObjects();
                    System.out.println("YOLO 탐지 결과: " + detectedObjects);

                    if (detectedObjects == null || detectedObjects.isEmpty()) {
                        return Mono.just(new FinalResponseDTO(s3Url, detectedObjects, "탐지된 객체가 없습니다."));
                    }

                    String targetObject = detectedObjects.get(0);

                    return webClient.post()
                            .uri("/api/describe")
                            .contentType(MediaType.APPLICATION_JSON)
                            // --- Cloudflare 인증 헤더 2개 추가 ---
                            .header("CF-Access-Client-Id", accessClientId)
                            .header("CF-Access-Client-Secret", accessClientSecret)
                            // -------------------------------------------
                            .body(BodyInserters.fromValue("{\"object_name\":\"" + targetObject + "\"}"))
                            .retrieve()
                            .bodyToMono(String.class)
                            .map(description -> {
                                System.out.println("AI 설명 결과: " + description);
                                return new FinalResponseDTO(s3Url, detectedObjects, description);
                            });
                });
    }
}
