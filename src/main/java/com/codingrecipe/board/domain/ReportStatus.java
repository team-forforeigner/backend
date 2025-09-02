package com.codingrecipe.board.domain;

// 신고 처리 상태를 나타내는 Enum
public enum ReportStatus {
    PENDING,    // 처리 대기 중
    PROCESSING, // 처리 중
    COMPLETED,  // 처리 완료
    REJECTED    // 반려
}