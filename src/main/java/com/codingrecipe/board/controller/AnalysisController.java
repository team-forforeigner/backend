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
                .orElse(imageFile.getBytes()); // local이면 그냥 업로드된 파일 사용

        WebClient webClient = webClientBuilder.baseUrl("https://ai.navoodiai.site").build();

        // --- 3. AI 서버로 보낼 Multipart 요청 본문 생성 ---
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return imageFile.getOriginalFilename();
            }
        });

        // ★★★★★ 1. 변경된 부분 ★★★★★
        // 'type' 파라미터를 요청 본문(form-data)에 추가합니다.
        builder.part("type", type);


        // --- 4. YOLO 분석 요청 ---
        return webClient.post()
                // ★★★★★ 2. 변경된 부분 ★★★★★
                // URI를 만들 때 더 이상 URL에 쿼리 파라미터(?type=...)를 추가하지 않습니다.
                .uri(uriBuilder -> uriBuilder.path("/api/analyze").build())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("CF-Access-Client-Id", accessClientId)
                .header("CF-Access-Client-Secret", accessClientSecret)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(AnalysisResponse.class)
                .flatMap(aiResponse -> {
                    List<String> detectedObjects = aiResponse.detectedObjects();

                    if (detectedObjects == null || detectedObjects.isEmpty()) {
                        FinalResponseDTO response = new FinalResponseDTO(s3Url, detectedObjects, "탐지된 객체가 없습니다.");
                        return Mono.just(ApiResponseDto.success(response));
                    }

                    String targetObject = detectedObjects.get(0);

                    // --- 5. 설명 요청 ---
                    return webClient.post()
                            .uri(uriBuilder -> uriBuilder.path("/api/describe")
                                    .queryParam("type", type) // 설명 요청에는 type을 쿼리 파라미터로 전달
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
