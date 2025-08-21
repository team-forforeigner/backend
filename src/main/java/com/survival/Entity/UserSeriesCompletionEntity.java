package com.survival.Entity;

import com.codingrecipe.board.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_series_completion")
public class UserSeriesCompletionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userSeriesCompletionId;

    private Long userId; // 저장 을 위함,  member에서 외래키로 받아와야 하는 부분

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="series_id")
    private SeriesEntity series;

    private LocalDateTime completedAt;
}
