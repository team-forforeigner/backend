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
                        return uploader.upload(imageFile, "chatbot");
                    } catch (IOException e) {
                        throw new RuntimeException("S3 업로드 실패", e);
                    }
                })
                .orElse("s3-disabled-in-local");

        System.out.println("S3 업로드 완료. fileKey: " + fileKey);

        // S3에 업로드된 파일의 완전한 URL을 생성합니다.
        String s3Url = s3UploaderService.map(uploader -> uploader.generatePresignedUrl(fileKey))
                .orElse("s3-disabled-in-local");


        // --- 2. AI 서버로 보낼 이미지 바이트 데이터 준비 ---
        byte[] imageBytes;
        if (s3UploaderService.isPresent()) {
            // S3가 활성화된 경우, S3에서 파일을 다시 다운로드하여 바이트를 얻습니다.
            imageBytes = s3UploaderService.get().downloadAsBytes(fileKey);
        } else {
            // local 환경일 경우, 업로드된 MultipartFile에서 직접 바이트를 얻습니다.
            imageBytes = imageFile.getBytes();
        }

        WebClient webClient = webClientBuilder.baseUrl("https://ai.navoodiai.site").build();

        // --- 3. AI 서버로 보낼 Multipart 요청 본문 생성 ---
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                // 원본 파일 이름을 지정해줘야 AI 서버가 파일을 인식할 수 있습니다.
                return imageFile.getOriginalFilename();
            }
        });

        // --- 4. YOLO 분석 요청 (AI 서버) ---
        return webClient.post()
                // AI 서버로 요청 시 'type' 쿼리 파라미터를 추가
                .uri(uriBuilder -> uriBuilder.path("/api/analyze")
                        .queryParam("type", type)
                        .build())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("CF-Access-Client-Id", accessClientId)
                .header("CF-Access-Client-Secret", accessClientSecret)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(AnalysisResponse.class)
                .flatMap(aiResponse -> {
                    List<String> detectedObjects = aiResponse.detectedObjects();
                    System.out.println("YOLO 탐지 결과: " + detectedObjects);

                    if (detectedObjects == null || detectedObjects.isEmpty()) {
                        FinalResponseDTO response = new FinalResponseDTO(s3Url, detectedObjects, "탐지된 객체가 없습니다.");
                        return Mono.just(ApiResponseDto.success(response));
                    }

                    String targetObject = detectedObjects.get(0);

                    // --- 5. 설명 요청 (AI 서버) ---
                    // 설명 요청 시에도 'type' 쿼리 파라미터 추가
                    return webClient.post()
                            .uri(uriBuilder -> uriBuilder.path("/api/describe")
                                    .queryParam("type", type)
                                    .build())
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("CF-Access-Client-Id", accessClientId)
                            .header("CF-Access-Client-Secret", accessClientSecret)
                            .body(BodyInserters.fromValue("{\"object_name\":\"" + targetObject + "\"}"))
                            .retrieve()
                            .bodyToMono(DescriptionResponse.class) // 응답을 DTO로 받도록 수정
                            .map(descriptionResponse -> {
                                String description = descriptionResponse.description();
                                System.out.println("AI 설명 결과: " + description);
                                FinalResponseDTO finalResponse = new FinalResponseDTO(s3Url, detectedObjects, description);
                                return ApiResponseDto.success(finalResponse);
                            });
                });
    }
}
