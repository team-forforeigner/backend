package com.codingrecipe.board.service;

import com.codingrecipe.board.dto.ReportDTO;
import com.codingrecipe.board.domain.ReportEntity;
import com.codingrecipe.board.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    @Transactional
    public void saveReport(ReportDTO reportDTO) {
        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setReportType(reportDTO.getReportType().toUpperCase());
        reportEntity.setReportContent(reportDTO.getContent());
        reportEntity.setReporter(reportDTO.getReporter());

        if ("BOARD".equalsIgnoreCase(reportDTO.getReportType())) {
            reportEntity.setBoardId(reportDTO.getTargetId());
        } else if ("COMMENT".equalsIgnoreCase(reportDTO.getReportType())) {
            reportEntity.setCommentId(reportDTO.getTargetId());
        } else {
            throw new IllegalArgumentException("잘못된 신고 타입입니다.");
        }

        reportRepository.save(reportEntity);
    }
}