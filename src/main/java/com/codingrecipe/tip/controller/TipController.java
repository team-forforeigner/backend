package com.codingrecipe.tip.controller;

import com.codingrecipe.tip.TipCategory;
import com.codingrecipe.tip.dto.ApiResponse;
import com.codingrecipe.tip.dto.TipCreateRequest;
import com.codingrecipe.tip.dto.TipResponse;
import com.codingrecipe.tip.dto.TipUpdateRequest;
import com.codingrecipe.tip.exception.InvalidCategoryException;
import com.codingrecipe.tip.exception.TipAlreadyExistsException;
import com.codingrecipe.tip.service.TipService;
import com.codingrecipe.tip.service.TipServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tips")
@RequiredArgsConstructor
public class TipController {

    private final TipServiceImpl tipServiceImpl;

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
                tips = tipServiceImpl.getTips(pageable);
            } else {
                tips = tipServiceImpl.getTipsByCategory(tipCategory, pageable);
            }
            return ResponseEntity.ok(tips);
        } catch (InvalidCategoryException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 설명 : 팁 저장 (단일)
    @PostMapping
    public ResponseEntity<ApiResponse<?>> saveTip(@RequestBody @Valid TipCreateRequest dto) {
        try {
            Long id = tipServiceImpl.createTip(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(Map.of("id", id)));

        } catch (TipAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.failure("TIP_ALREADY_EXISTS", "이미 등록된 질문입니다."));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("INVALID_DATA", "유효하지 않은 데이터입니다."));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("SERVER_ERROR", "서버 오류"));
        }
    }

    // 설명 : 팁 수정
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateTip(@PathVariable Long id, @RequestBody TipUpdateRequest dto) {
        dto.setId(id); // 요청 경로에서 ID를 DTO에 설정
        boolean changed = tipServiceImpl.updateTip(dto);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("changed", changed);

        return ResponseEntity.ok(response);
    }

    // 설명 : 팁 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTip(@PathVariable Long id) {
        tipServiceImpl.deleteTip(id);
        return ResponseEntity.noContent().build();
    }

    // 설명 : JSON 배열로 받아서 DB 저장
    @PostMapping("/import")
    public ResponseEntity<String> importFromJson(@RequestBody @Valid List<TipCreateRequest> tipList) {
        String result = tipServiceImpl.importFromJson(tipList);
        return ResponseEntity.ok(result);
    }

}



