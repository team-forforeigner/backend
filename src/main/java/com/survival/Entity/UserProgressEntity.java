package com.survival.Entity;

import com.codingrecipe.board.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

//사용자의 게임 진행 기록을 저장하는 엔티티
// 어떤 사용자가 어떤 시리즈에서 어떤 선택지를 클릭했는지 기록

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="user_progress")
public class UserProgressEntity {

    private Long userId; // 저장 을 위함,  member에서 외래키로 받아와야 하는 부분

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private long user_id;

    // 현재 진행 중인 시리즈의 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="series_id")
    private SeriesEntity series;

    // 사용자가 현재 도달한 에피소드 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="episode_id")
    private EpisodesEntity episode;

    // 사용자가 선택한 선택지 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="choice_id")
    private ChoicesEntity choice;

    // 선택이 이루어진 시각
    private LocalDateTime playedAt;


}
