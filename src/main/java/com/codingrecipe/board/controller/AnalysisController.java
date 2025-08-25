package com.codingrecipe.board.controller;

import com.codingrecipe.board.dto.ApiResponseDto;
import com.codingrecipe.board.dto.AnalysisResponse;
import com.codingrecipe.board.dto.DescriptionResponse; // [추가] 새로운 DTO 임포트
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
import java.util.Optional;

@Profile("!local")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnalysisController {

    private final WebClient.Builder webClientBuilder;
    // [유지] Optional<S3UploaderService> 구조를 그대로 유지합니다.
    private final Optional<S3UploaderService> s3UploaderService;

    @Value("${ai-server.access-client-id}")
    private String accessClientId;

    @Value("${ai-server.access-client-secret}")
    private String accessClientSecret;

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // --- [수정] 메소드 시그니처 변경 ---
    public Mono<ApiResponseDto<FinalResponseDTO>> analyzeImage(
            @RequestParam("file") MultipartFile imageFile, // 파라미터 이름을 "file"로 변경
            @RequestParam(value = "type", defaultValue = "palace") String type // "type" 파라미터 추가
    ) throws IOException {

        // [유지] S3UploaderService가 없는 local 환경을 고려하는 로직을 그대로 유지합니다.
        String s3Url = s3UploaderService.map(uploader -> {
            try {
                // 기존의 upload 메소드를 사용합니다.
                return uploader.upload(imageFile, "images");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).orElse("s3-disabled-in-local");

        System.out.println("S3 업로드 완료. URL: " + s3Url);

        WebClient webClient = webClientBuilder.baseUrl("https://ai.navoodiai.site").build();

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", imageFile.getResource());

        // --- [수정] AI 서버 요청 URI를 동적으로 생성 ---
        String analyzeUri = String.format("/api/analyze?type=%s", type);
        String describeUri = String.format("/api/describe?type=%s", type);

        return webClient.post()
                .uri(analyzeUri) // 수정된 URI 사용
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
                        // [유지] 기존 ApiResponseDto 형식으로 응답을 감싸줍니다.
                        return Mono.just(ApiResponseDto.success(response));
                    }

                    String targetObject = detectedObjects.get(0);

                    return webClient.post()
                            .uri(describeUri) // 수정된 URI 사용
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("CF-Access-Client-Id", accessClientId)
                            .header("CF-Access-Client-Secret", accessClientSecret)
                            .body(BodyInserters.fromValue("{\"object_name\":\"" + targetObject + "\"}"))
                            .retrieve()
                            // --- [수정] 응답을 String이 아닌 DescriptionResponse DTO로 받도록 변경 ---
                            .bodyToMono(DescriptionResponse.class)
                            .map(descriptionResponse -> {
                                // DTO에서 설명 텍스트만 추출합니다.
                                String description = descriptionResponse.description();
                                System.out.println("AI 설명 결과: " + description);
                                FinalResponseDTO finalResponse = new FinalResponseDTO(s3Url, detectedObjects, description);
                                // [유지] 기존 ApiResponseDto 형식으로 응답을 감싸줍니다.
                                return ApiResponseDto.success(finalResponse);
                            });
                });
    }
}
