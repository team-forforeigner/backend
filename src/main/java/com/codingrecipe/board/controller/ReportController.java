package com.codingrecipe.board.controller;

import com.codingrecipe.board.dto.ReportDTO;
import com.codingrecipe.board.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/report")
    public ResponseEntity<String> saveReport(@RequestBody ReportDTO reportDTO) {
        try {
            reportService.saveReport(reportDTO);
            return new ResponseEntity<>("신고가 정상적으로 접수되었습니다.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("신고 접수 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}