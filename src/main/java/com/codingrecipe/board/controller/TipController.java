package com.codingrecipe.board.controller;

import com.codingrecipe.board.domain.TipCategory;
import com.codingrecipe.board.dto.ApiResponse;
import com.codingrecipe.board.dto.TipCreateRequest;
import com.codingrecipe.board.dto.TipResponse;
import com.codingrecipe.board.dto.TipUpdateRequest;
import com.codingrecipe.board.exception.TipAlreadyExistsException;
import com.codingrecipe.board.exception.TipNotFoundException;
import com.codingrecipe.board.service.TipServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.codingrecipe.board.util.ApiResponseUtil.*;

@RestController
@RequestMapping("/api/tips")
@RequiredArgsConstructor
public class TipController {

    private final TipServiceImpl tipServiceImpl;

    // 설명 : 팁 리스트 조회. 카테고리별로 조회 가능 (기본값은 ALL)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TipResponse>>> getTips(
            @RequestParam(defaultValue = "ALL") String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (page < 0) return badRequest("페이지 번호는 0 이상이어야 합니다.");
        if (size <= 0 || size > 100) return badRequest("페이지 크기는 1~100 사이여야 합니다.");

        Pageable pageable = PageRequest.of(page, size);
        Page<TipResponse> tips;

        if ("ALL".equalsIgnoreCase(category)) {
            tips = tipServiceImpl.getTips(pageable);
        } else {
            try {
                TipCategory tipCategory = TipCategory.valueOf(category.toUpperCase());
                tips = tipServiceImpl.getTipsByCategory(tipCategory, pageable);
            } catch (IllegalArgumentException e) {
                return badRequest("잘못된 카테고리입니다.");
            }
        }
        return ok(tips, "성공적으로 조회되었습니다.");
    }

    // 설명 : 팁 1개 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TipResponse>> getTipById(@PathVariable Long id) {
        try {
            return ok(tipServiceImpl.getTipById(id), "성공적으로 조회되었습니다.");
        } catch (TipNotFoundException e) {
            return fail(HttpStatus.NOT_FOUND, "해당 팁을 찾을 수 없습니다.");
        }
    }

    // 설명 : 팁 1개 저장
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> saveTip(@RequestBody @Valid TipCreateRequest dto) {
        try {
            Long id = tipServiceImpl.createTip(dto);
            return created(Map.of("id", id), "팁이 성공적으로 등록되었습니다.");
        } catch (TipAlreadyExistsException e) {
            return fail(HttpStatus.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            return badRequest("유효하지 않은 데이터입니다.");
        }
    }

    // 설명 : 팁 수정
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> updateTip(@PathVariable Long id,
                                                    @RequestBody @Valid TipUpdateRequest dto) {
        boolean changed = tipServiceImpl.updateTip(id, dto);

        String message = changed ? "수정 되었습니다." : "수정 되지 않았습니다.";
        return ok(Map.of("changed", changed), message);
    }

    // 설명 : 팁 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTip(@PathVariable Long id) {
        try {
            tipServiceImpl.deleteTip(id);
            return ok(null, "팁이 삭제되었습니다.");
        } catch (TipNotFoundException e) {
            return fail(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // 설명 : 팁 여러 개 저장 (JSON 배열로 받기)
    @PostMapping("/import")
    public ResponseEntity<ApiResponse<Map<String, String>>> importFromJson(@RequestBody @Valid List<TipCreateRequest> tipList) {
        String result = tipServiceImpl.importFromJson(tipList);
        return ok(Map.of("message", result), "팁이 일괄 등록되었습니다.");
    }

}



