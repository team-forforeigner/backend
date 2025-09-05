package com.codingrecipe.board.service;

import com.codingrecipe.board.domain.ReportEntity;
import com.codingrecipe.board.dto.ReportAdminResponseDto;
import com.codingrecipe.board.dto.ReportDTO;
import com.codingrecipe.board.dto.ReportStatusUpdateRequestDto;
import com.codingrecipe.board.exception.CustomException;
import com.codingrecipe.board.exception.ErrorCode;
import com.codingrecipe.board.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
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

    /**
     * 관리자가 신고의 상태와 메모를 업데이트
     */
    public void updateReportStatus(Long reportId, ReportStatusUpdateRequestDto dto) {
        ReportEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        report.setStatus(dto.getStatus());
        report.setAdminMemo(dto.getAdminMemo());
    }

    /**
     * 관리자 페이지를 위해 모든 신고 목록을 조회
     */
    @Transactional(readOnly = true)
    public List<ReportAdminResponseDto> findAllReportsForAdmin() {
        List<ReportEntity> reports = reportRepository.findAll(Sort.by(Sort.Direction.DESC, "reportedAt"));
        return reports.stream()
                .map(ReportAdminResponseDto::new)
                .collect(Collectors.toList());
    }
}

