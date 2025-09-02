// 게시글 또는 댓글에 대한 사용자 신고 정보를 관리하는 엔티티
package com.codingrecipe.board.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "report_table") // 'report_table' 테이블과 매핑
public class ReportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 신고 기록 고유 식별자

    @Column
    private Long boardId; // 신고된 게시글의 ID (댓글 신고 시에는 null)

    @Column
    private Long commentId; // 신고된 댓글의 ID (게시글 신고 시에는 null)

    @Column(nullable = false)
    private String reportType; // 신고 유형 (예: "욕설", "스팸")

    @Column(nullable = false, length=500)
    private String reportContent; // 신고 상세 내용

    @Column
    private String reporter; // 신고한 사용자의 식별자 (예: 이메일)

    @Column(updatable = false)
    private LocalDateTime reportedAt; // 신고가 접수된 시간

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.PENDING; // 기본값 PENDING

    @Column(length = 1000)
    private String adminMemo; // 관리자 메모

    // 저장되기 전, 신고 시간을 현재 시간으로 자동 설정
    @PrePersist
    public void reportedAt() {
        this.reportedAt = LocalDateTime.now();
    }
}
