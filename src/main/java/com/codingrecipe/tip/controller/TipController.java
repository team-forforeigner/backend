package com.codingrecipe.tip.controller;

import com.codingrecipe.tip.TipCategory;
import com.codingrecipe.tip.dto.TipCreateRequest;
import com.codingrecipe.tip.dto.TipResponse;
import com.codingrecipe.tip.dto.TipImportResult;
import com.codingrecipe.tip.dto.TipUpdateRequest;
import com.codingrecipe.tip.exception.InvalidCategoryException;
import com.codingrecipe.tip.exception.TipAlreadyExistsException;
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
    public ResponseEntity<Page<TipResponse>> getTips(
            @RequestParam(required = false, defaultValue = "ALL") String category,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TipResponse> tips;
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

    // 설명 : 팁 저장 (단일)
    @PostMapping
    public ResponseEntity<?> saveTip(@RequestBody @Valid TipCreateRequest dto) {
        try {
            Long id = tipService.createTip(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(id);
        } catch (TipAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 등록된 질문입니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("유효하지 않은 데이터입니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류");
        }
    }

    // 설명 : 팁 수정
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateTip(@PathVariable Long id, @RequestBody TipUpdateRequest dto) {
        tipService.updateTip(dto);
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
    public ResponseEntity<TipImportResult> importFromJson(@RequestBody @Valid List<TipCreateRequest> tipList) {
        TipImportResult result = tipService.importFromJson(tipList);
        return ResponseEntity.ok(result);
    }

}



