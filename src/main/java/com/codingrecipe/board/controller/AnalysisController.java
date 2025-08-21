package com.codingrecipe.board.controller;

import com.codingrecipe.board.dto.ApiResponseDto;
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
import java.util.Optional;

@Profile("!local")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnalysisController {

    private final WebClient.Builder webClientBuilder;
    private final S3UploaderService s3UploaderService;
//    private final Optional<S3UploaderService> s3UploaderService;

    @Value("${ai-server.access-client-id}")
    private String accessClientId;

    @Value("${ai-server.access-client-secret}")
    private String accessClientSecret;

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ApiResponseDto<FinalResponseDTO>> analyzeImage(@RequestParam("image") MultipartFile imageFile) throws IOException {

        /**
         * [수정 사항]
         * - s3 업로드 폴더 명을 images -> chatbot으로 변경
         * - s3Url 변수명을 -> fileKey로 전부 변경
         * - builder에서 MultipartFile 전달 -> 바이트 스트림을 전달하도록 변경
         */

        // --- 1. S3 업로드 -> 객체 키 반환 ---
        String fileKey = s3UploaderService.uploadImage(imageFile, "chatbot");
        System.out.println("S3 업로드 완료. fileKey: " + fileKey);

        // --- 2. 바이트 배열 추출 (S3에서 읽기) ---
        byte[] imageBytes = s3UploaderService.downloadAsBytes(fileKey);

        WebClient webClient = webClientBuilder.baseUrl("https://ai.navoodiai.site").build();

        // --- 3. MultipartBodyBuilder ---
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return imageFile.getOriginalFilename();
            }
        });

        /*// --- 1. S3 업로드 -> 객체 키 반환 ---
        String fileKey = s3UploaderService
                .map(uploader -> {
                    try {
                        return uploader.uploadImage(imageFile, "chatbot");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElse("s3-disabled-in-local");

        System.out.println("S3 업로드 완료. fileKey: " + fileKey);

        // --- 2. 바이트 배열 추출 (S3에서 읽기) ---
        byte[] imageBytes = s3UploaderService
                .map(uploader -> uploader.downloadAsBytes(fileKey))
                .orElse(imageFile.getBytes()); // local이면 그냥 업로드된 파일 사용

        WebClient webClient = webClientBuilder.baseUrl("https://ai.navoodiai.site").build();

        // --- 3. MultipartBodyBuilder ---
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return imageFile.getOriginalFilename();
            }
        });*/

        // --- 4. YOLO 분석 요청 ---
        return webClient.post()
                .uri("/api/analyze")
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
                        FinalResponseDTO response = new FinalResponseDTO(fileKey, detectedObjects, "탐지된 객체가 없습니다.");
                        return Mono.just(ApiResponseDto.success(response));
                    }

                    String targetObject = detectedObjects.get(0);

                    // --- 5. 설명 요청 ---
                    return webClient.post()
                            .uri("/api/describe")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("CF-Access-Client-Id", accessClientId)
                            .header("CF-Access-Client-Secret", accessClientSecret)
                            .body(BodyInserters.fromValue("{\"object_name\":\"" + targetObject + "\"}"))
                            .retrieve()
                            .bodyToMono(String.class)
                            .map(description -> {
                                System.out.println("AI 설명 결과: " + description);
                                FinalResponseDTO finalResponse = new FinalResponseDTO(fileKey, detectedObjects, description);
                                return ApiResponseDto.success(finalResponse);
                            });
                });
    }
}
