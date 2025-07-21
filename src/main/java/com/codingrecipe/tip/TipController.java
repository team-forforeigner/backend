package com.codingrecipe.tip;

import com.codingrecipe.tip.service.TipImportService;
import com.codingrecipe.tip.service.TipService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/tips")
@RequiredArgsConstructor
public class TipController {

    private final TipService tipService;
    private final TipImportService tipImportService;
    private final ObjectMapper objectMapper;

    @GetMapping("/api/tips")
    public Page<TipDTO> getTipsByCategory(@RequestParam(required = false)TipCategory category, Pageable pageable) {
        if (category != null) {
            return tipService.findByCategory(category, pageable);
        }
        return tipService.findAll(pageable);
    }

    // 설명 : JSON 파일로부터 여러 Tip을 한 번에 DB에 저장한다.
    @PostMapping("/upload")
    public ResponseEntity<String> importTips(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("파일이 비어 있습니다.");
        }

        try {
            List<TipDTO> tipList = objectMapper.readValue(
                    file.getInputStream(),
                    new TypeReference<List<TipDTO>>() {}
            );

            tipImportService.importFromJson(tipList);

            return ResponseEntity.ok("성공적으로 저장되었습니다. 저장된 팁 개수: " + tipList.size());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 처리 중 오류가 발생했습니다.");
        }
    }
}



