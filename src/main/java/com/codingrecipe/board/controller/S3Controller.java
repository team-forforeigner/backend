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
@CrossOrigin(origins = "*")
public class S3Controller {

    private final S3UploaderService s3UploaderService;

    /** 전체 이미지 목록 조회 */
    @GetMapping("/list")
    public List<Map<String, Object>> listImages() {
        return s3UploaderService.listImages();
    }

    /** 이미지 업로드 */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category) throws IOException {

        List<String> allowedCategories = List.of("community", "profile", "banner", "chatbot", "etc");
        if (!allowedCategories.contains(category.toLowerCase())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid category"));
        }

        String fileKey = s3UploaderService.uploadImage(file, category);
        String url = s3UploaderService.buildFileUrl(fileKey);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("url", url, "fileKey", fileKey));
    }

    /** 이미지 조회/다운로드 */
    @GetMapping("/image")
    public ResponseEntity<ByteArrayResource> getImage(
            @RequestParam String fileKey,
            @RequestParam(defaultValue = "inline") String disposition) {

        byte[] data = s3UploaderService.downloadAsBytes(fileKey);
        ByteArrayResource resource = new ByteArrayResource(data);

        String contentType = FileUtil.getMimeType(fileKey);
        String fileName = FileUtil.getBaseName(fileKey);
        String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + encodedName + "\"")
                .body(resource);
    }

    /** 이미지 수정 */
    @PutMapping("/update")
    public ResponseEntity<?> updateImage(
            @RequestParam String fileKey,
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category) throws IOException {

        Map<String, String> result = s3UploaderService.updateImage(fileKey, file, category);
        return ResponseEntity.ok(result);
    }

    /** 이미지 삭제 */
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteImage(@RequestParam String fileKey) {
        s3UploaderService.deleteImage(fileKey);
        return ResponseEntity.noContent().build();
    }

}