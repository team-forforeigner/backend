package com.survival.Entity;

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

    private Long userId;

    private Long seriesId;

    private LocalDateTime completedAt;
}
