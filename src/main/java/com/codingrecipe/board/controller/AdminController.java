package com.codingrecipe.board.controller;

import com.codingrecipe.board.entity.ReportEntity;
import com.codingrecipe.board.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final ReportRepository reportRepository;

    // "localhost:8080/admin/reports"로 접속하면 모든 신고 내역을 최신순으로 확인
    @GetMapping("/reports")
    public List<ReportEntity> getAllReports() {
        return reportRepository.findAll(Sort.by(Sort.Direction.DESC, "reportedAt"));
    }
}