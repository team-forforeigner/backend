// 사용자 신고 기능 관련 API 컨트롤러
package com.codingrecipe.board.controller;

import com.codingrecipe.board.dto.ReportDTO;
import com.codingrecipe.board.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/report") // 이 컨트롤러의 모든 API는 /api/report 경로를 가짐
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<String> saveReport(@RequestBody ReportDTO reportDTO) {
        // 사용자로부터 받은 신고 정보 저장
        try {
            reportService.saveReport(reportDTO);
            return new ResponseEntity<>("신고가 정상적으로 접수되었습니다", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("신고 접수 중 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
