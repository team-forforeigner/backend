package com.survival.Entity;

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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long userId;

    // 현재 진행 중인 시리즈의 ID
    private long seriesId;

    // 사용자가 현재 도달한 에피소드 ID
    private long episodeId;

    // 사용자가 선택한 선택지 ID
    private long choiceId;

    // 선택이 이루어진 시각
    private LocalDateTime playedAt;
}
