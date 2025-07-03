package com.codingrecipe.board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportDTO {
    private Long targetId;
    private String reportType;
    private String content;
    private String reporter;
}