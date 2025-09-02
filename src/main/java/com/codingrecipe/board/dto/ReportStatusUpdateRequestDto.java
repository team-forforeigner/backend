package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.ReportStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportStatusUpdateRequestDto {
    private ReportStatus status;
    private String adminMemo;
}