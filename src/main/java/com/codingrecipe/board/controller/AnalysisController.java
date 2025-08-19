package com.codingrecipe.board.controller;

import com.codingrecipe.board.dto.AnalysisResponse;
import com.codingrecipe.board.dto.FinalResponseDTO;
import com.codingrecipe.board.service.S3UploaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
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

    @Value("${ai-server.access-client-id}")
    private String accessClientId;

    @Value("${ai-server.access-client-secret}")
    private String accessClientSecret;

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<FinalResponseDTO> analyzeImage(@RequestParam("image") MultipartFile imageFile) throws IOException {

        /**
         * [수정 사항]
         * - s3 업로드 폴더 명을 images -> chatbot으로 변경
         * - s3Url 변수명을 -> fileKey로 전부 변경
         * - builder에서 MultipartFile 전달 -> 바이트 스트림을 전달하도록 변경
         */

        // 1. S3 업로드 → 객체 키 반환
        String fileKey = s3UploaderService.uploadImage(imageFile, "chatbot");
        System.out.println("S3 업로드 완료. fileKey: " + fileKey);

        // 2. S3에서 바이트 읽기
        byte [] imageBytes = s3UploaderService.downloadAsBytes(fileKey);

        WebClient webClient = webClientBuilder.baseUrl("https://ai.navoodiai.site").build();

        // 3. MultipartBodyBuilder에 바이트 전달
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return imageFile.getOriginalFilename();
            }
        });

        return webClient.post()
                .uri("/api/analyze")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                // --- Cloudflare 인증 헤더 2개 추가 ---
                .header("CF-Access-Client-Id", accessClientId)
                .header("CF-Access-Client-Secret", accessClientSecret)

                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(AnalysisResponse.class)
                .flatMap(aiResponse -> {
                    List<String> detectedObjects = aiResponse.detectedObjects();
                    System.out.println("YOLO 탐지 결과: " + detectedObjects);

                    if (detectedObjects == null || detectedObjects.isEmpty()) {
                        return Mono.just(new FinalResponseDTO(fileKey, detectedObjects, "탐지된 객체가 없습니다."));
                    }

                    String targetObject = detectedObjects.get(0);

                    return webClient.post()
                            .uri("/api/describe")
                            .contentType(MediaType.APPLICATION_JSON)
                            // --- Cloudflare 인증 헤더 2개 추가 ---
                            .header("CF-Access-Client-Id", accessClientId)
                            .header("CF-Access-Client-Secret", accessClientSecret)

                            .body(BodyInserters.fromValue("{\"object_name\":\"" + targetObject + "\"}"))
                            .retrieve()
                            .bodyToMono(String.class)
                            .map(description -> {
                                System.out.println("AI 설명 결과: " + description);
                                return new FinalResponseDTO(fileKey, detectedObjects, description);
                            });
                });
    }
}
