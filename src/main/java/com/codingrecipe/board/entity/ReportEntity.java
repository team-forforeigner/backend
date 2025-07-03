package com.codingrecipe.board.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "report_table")
public class ReportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long boardId;

    @Column
    private Long commentId;

    @Column(nullable = false)
    private String reportType;

    @Column(nullable = false, length=500)
    private String reportContent;

    @Column
    private String reporter;

    @Column(updatable = false)
    private LocalDateTime reportedAt;

    @PrePersist
    public void reportedAt() {
        this.reportedAt = LocalDateTime.now();
    }
}