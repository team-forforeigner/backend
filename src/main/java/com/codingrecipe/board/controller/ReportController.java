package com.codingrecipe.board.controller;

import com.codingrecipe.board.dto.ApiResponseDto;
import com.codingrecipe.board.dto.ReportDTO;
import com.codingrecipe.board.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<Void>> saveReport(@RequestBody ReportDTO reportDTO) {
        reportService.saveReport(reportDTO);
        return ResponseEntity.ok(ApiResponseDto.success("신고가 정상적으로 접수되었습니다."));
    }
}