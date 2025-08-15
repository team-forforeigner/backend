package com.codingrecipe.board.controller;

import com.codingrecipe.board.service.S3UploaderService;
import com.codingrecipe.board.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/s3")
public class S3Controller {

    private final S3UploaderService s3UploaderService;

    /**
     * S3 이미지 목록 조회
     */
    @GetMapping
    public List<Map<String, Object>> listImages() {
        return s3UploaderService.listImages();
    }

    /**
     * 이미지 업로드
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category) throws IOException {

        // 허용 카테고리 검증
        List<String> allowedCategories = List.of("community", "profile", "banner");
        if (!allowedCategories.contains(category.toLowerCase())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid category"));
        }

        String key = s3UploaderService.upload(file, category);

        String url = String.format("https://%s.s3.%s.amazonaws.com/%s",
                s3UploaderService.getBucketName(),
                s3UploaderService.getRegion(),
                key);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("url", url, "key", key));
    }


    /**
     * 통합 조회/다운로드
     */
    @GetMapping("/image")
    public ResponseEntity<ByteArrayResource> getImage(
            @RequestParam String fileKey,
            @RequestParam(defaultValue = "inline") String disposition) {

        byte[] data = s3UploaderService.downloadAsBytes(fileKey);
        ByteArrayResource resource = new ByteArrayResource(data);

        String contentType = s3UploaderService.getFileContentType(fileKey);
        String fileName = FileUtil.getBaseName(fileKey);
        String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + encodedName + "\"")
                .body(resource);
    }

    /**
     * 이미지 수정(교체)
     * - 기존 이미지를 삭제하고 새로운 이미지를 업로드합니다.
     */
    @PutMapping("/{fileKey}")
    public ResponseEntity<?> updateImage(@PathVariable String fileKey,
                                         @RequestParam("file") MultipartFile file) throws IOException {
        s3UploaderService.deleteFile(fileKey);
        String newKey = s3UploaderService.upload(file, "uploads");
        String url = String.format("https://%s.s3.%s.amazonaws.com/%s",
                s3UploaderService.getBucketName(),
                s3UploaderService.getRegion(),
                newKey);
        return ResponseEntity.ok(Map.of("url", url, "key", newKey));
    }

    /**
     * 이미지 삭제
     */
    @DeleteMapping("/{fileKey}")
    public ResponseEntity<Void> deleteImage(@PathVariable String fileKey) {
        s3UploaderService.deleteFile(fileKey);
        return ResponseEntity.noContent().build();
    }

}