package com.codingrecipe.board.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="user_progress")
public class UserProgressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="series_id")
    private SeriesEntity series;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="episode_id")
    private EpisodesEntity episode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="choice_id")
    private ChoicesEntity choice;

    private LocalDateTime playedAt;
}
