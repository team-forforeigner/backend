// 게시글 또는 댓글 신고 시 클라이언트에서 서버로 데이터를 전송할 때 사용하는 DTO
package com.codingrecipe.board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportDTO {
    private Long targetId;      // 신고 대상의 ID (게시글 ID 또는 댓글 ID)
    private String reportType;  // 신고 유형 (예: "욕설", "스팸")
    private String content;     // 신고 상세 내용
    private String reporter;    // 신고한 사용자의 식별자
}
