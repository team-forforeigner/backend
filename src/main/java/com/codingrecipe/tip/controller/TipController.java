package com.codingrecipe.tip.controller;

import com.codingrecipe.tip.dto.TipDTO;
import com.codingrecipe.tip.service.TipService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    @GetMapping
    public ResponseEntity<Page<TipDTO>> getTips(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<TipDTO> tipPage = tipService.getTips(pageable);
        return ResponseEntity.ok(tipPage);
    }

    // 설명 : JSON 배열로 받아서 DB 저장
    @PostMapping("/import")
    public ResponseEntity<String> importFromJson(@RequestBody List<TipDTO> tipList) {
        tipService.importFromJson(tipList);
        return ResponseEntity.status(HttpStatus.CREATED).body("팁이 성공적으로 저장되었습니다.");
    }

}



