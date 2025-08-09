// 게시글 또는 댓글 신고 관련 비즈니스 로직을 처리하는 서비스
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

    /**
     * 사용자로부터 받은 신고 정보를 저장
     */
    @Transactional
    public void saveReport(ReportDTO reportDTO) {
        ReportEntity reportEntity = new ReportEntity();
        // 신고 유형을 대문자로 통일하여 저장
        reportEntity.setReportType(reportDTO.getReportType().toUpperCase());
        reportEntity.setReportContent(reportDTO.getContent());
        reportEntity.setReporter(reportDTO.getReporter());

        // 신고 유형(대소문자 무관)에 따라 게시글 ID 또는 댓글 ID를 설정
        if ("BOARD".equalsIgnoreCase(reportDTO.getReportType())) {
            reportEntity.setBoardId(reportDTO.getTargetId());
        } else if ("COMMENT".equalsIgnoreCase(reportDTO.getReportType())) {
            reportEntity.setCommentId(reportDTO.getTargetId());
        } else {
            // "BOARD" 또는 "COMMENT"가 아닌 경우 예외 발생
            throw new IllegalArgumentException("잘못된 신고 타입입니다");
        }

        reportRepository.save(reportEntity);
    }
}
