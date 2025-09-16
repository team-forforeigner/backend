package com.codingrecipe.board.controller;

import com.codingrecipe.board.dto.ApiResponse;
import com.codingrecipe.board.service.S3UploaderService;
import com.codingrecipe.board.util.ApiResponseUtil;
import com.codingrecipe.board.util.FileUtil;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;

/**
 * [테스트용] 이미지 컨트롤러
 */

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Slf4j
public class ImageController {

    private final S3UploaderService s3UploaderService;

    /**
     * [테스트용] 이미지 업로드
     * - 테스트 폴더(test)에 업로드
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(
            @RequestParam("file") MultipartFile file
    ) {
        if (file.isEmpty()) {
            return ApiResponseUtil.badRequest("파일이 비어 있습니다.");
        }

        try {
            String fileKey = s3UploaderService.uploadImage(file, "test");
            log.info("이미지 업로드 성공: {}", fileKey);
            return ApiResponseUtil.created(Map.of("fileKey", fileKey), "이미지 업로드 성공");
        } catch (IOException e) {
            log.error("이미지 업로드 실패", e);
            return ApiResponseUtil.fail(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드 실패");
        }
    }

    /**
     * 이미지 조회 (성공: 바이트 배열, 실패: ApiResponse JSON)
     */
    @GetMapping("/view")
    public ResponseEntity<?> getImage(@RequestParam String fileKey) {
        try {
            byte[] imageBytes = s3UploaderService.downloadAsBytes(fileKey);
            String mimeType = FileUtil.getMimeType(fileKey);
            MediaType mediaType = MediaType.parseMediaType(mimeType);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(imageBytes);

        } catch (Exception e) {
            log.error("이미지 조회 실패: {}", fileKey, e);
            return ApiResponseUtil.fail(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 조회 실패");
        }
    }

    /**
     * Presigned URL 발급 API
     */
    @GetMapping("/presigned")
    public ResponseEntity<ApiResponse<String>> getPresignedUrl(@RequestParam String fileKey) {
        try {
            String presignedUrl = s3UploaderService.generatePresignedUrl(fileKey);

            if (presignedUrl == null) {
                return ApiResponseUtil.fail(HttpStatus.INTERNAL_SERVER_ERROR, "Presigned URL 생성 실패");
            }

            return ApiResponseUtil.ok(presignedUrl, "Presigned URL 생성 완료");

        } catch (Exception e) {
            log.error("Presigned URL 생성 중 오류 발생: {}", fileKey, e);
            return ApiResponseUtil.fail(HttpStatus.INTERNAL_SERVER_ERROR, "Presigned URL 생성 실패");
        }
    }

    /**
     * 이미지 수정 (기존 파일 삭제 후 새 파일 업로드)
     */
    @PutMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> updateImage(
            @RequestParam String fileKey,
            @RequestParam("file") MultipartFile newFile
    ) {
        if (newFile.isEmpty()) {
            return ApiResponseUtil.badRequest("새 파일이 비어 있습니다.");
        }

        try {
            String newFileKey = s3UploaderService.updateImage(fileKey, newFile, "test");
            log.info("이미지 수정 성공: {} -> {}", fileKey, newFileKey);
            return ApiResponseUtil.ok(Map.of("fileKey", newFileKey), "이미지 수정 성공");
        } catch (Exception e) {
            log.error("이미지 수정 실패: {}", fileKey, e);
            return ApiResponseUtil.fail(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 수정 실패");
        }
    }

    /**
     * 이미지 삭제
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<String>> deleteImage(@RequestParam String fileKey) {
        try {
            s3UploaderService.deleteImage(fileKey);
            log.info("이미지 삭제 성공: {}", fileKey);
            return ApiResponseUtil.ok(fileKey, "이미지 삭제 성공");
        } catch (ResponseStatusException e) {
            log.warn("이미지 삭제 실패 (파일 없음): {}", fileKey, e);
            return ApiResponseUtil.fail(e.getStatusCode(), e.getReason());
        } catch (Exception e) {
            log.error("이미지 삭제 실패: {}", fileKey, e);
            return ApiResponseUtil.fail(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 삭제 실패");
        }
    }
}
