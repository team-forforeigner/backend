package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.ReportEntity;
import com.codingrecipe.board.domain.ReportStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReportAdminResponseDto {
    private Long id;
    private Long boardId;
    private Long commentId;
    private String reportType;
    private String reportContent;
    private String reporter;
    private LocalDateTime reportedAt;
    private ReportStatus status;
    private String adminMemo;

    public ReportAdminResponseDto(ReportEntity report) {
        this.id = report.getId();
        this.boardId = report.getBoardId();
        this.commentId = report.getCommentId();
        this.reportType = report.getReportType();
        this.reportContent = report.getReportContent();
        this.reporter = report.getReporter();
        this.reportedAt = report.getReportedAt();
        this.status = report.getStatus();
        this.adminMemo = report.getAdminMemo();
    }
}