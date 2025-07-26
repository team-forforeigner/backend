package com.codingrecipe.tip.controller;

import com.codingrecipe.tip.TipCategory;
import com.codingrecipe.tip.dto.TipDTO;
import com.codingrecipe.tip.exception.InvalidCategoryException;
import com.codingrecipe.tip.service.TipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tips")
@RequiredArgsConstructor
public class TipController {

    private final TipService tipService;

    // 설명 : 팁 리스트 조회 (Page 적용)
    // 카테고리별로 조회 가능, 기본값은 ALL
    @GetMapping
    public ResponseEntity<Page<TipDTO>> getTips(
            @RequestParam(required = false, defaultValue = "ALL") String category,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TipDTO> tips;
        TipCategory tipCategory = TipCategory.valueOf(category.toUpperCase()); // String -> Enum 변환

        try {
            if ("ALL".equalsIgnoreCase(category)) {
                tips = tipService.getTips(pageable);
            } else {
                tips = tipService.getTipsByCategory(tipCategory, pageable);
            }
            return ResponseEntity.ok(tips);
        } catch (InvalidCategoryException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 설명 : 팁 업데이트
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateTip(@PathVariable Long id, @RequestBody TipDTO dto) {
        tipService.updateTip(id, dto);
        return ResponseEntity.ok().build();
    }

    // 설명 : 팁 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTip(@PathVariable Long id) {
        tipService.deleteTip(id);
        return ResponseEntity.noContent().build();
    }

    // 설명 : JSON 배열로 받아서 DB 저장
    @PostMapping("/import")
    public ResponseEntity<String> importFromJson(@RequestBody @Valid List<TipDTO> tipList) {
        tipService.importFromJson(tipList);
        return ResponseEntity.status(HttpStatus.CREATED).body("팁이 성공적으로 저장되었습니다.");
    }

}



