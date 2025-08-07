// 사용자의 실시간 보스전 상태를 관리하는 엔티티
package com.codingrecipe.board.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "boss_battle_state") // 'boss_battle_state' 테이블과 매핑
public class BossBattleState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 상태 기록 고유 식별자

    @OneToOne(fetch = FetchType.LAZY) // 사용자와 전투 상태는 일대일 관계
    @JoinColumn(name = "member_id", unique = true) // 'member_id'로 Member와 조인, 한 사용자는 하나의 전투만 진행
    private Member member; // 전투를 진행 중인 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boss_stage_id") // 'boss_stage_id'로 BossStage와 조인
    private BossStage bossStage; // 현재 도전 중인 보스

    @Column(nullable = false)
    private int currentHp; // 보스의 현재 체력

    @Column(nullable = false)
    private int currentPhase; // 현재 진행 중인 페이즈 번호

    @Column(nullable = false)
    private int correctCountInPhase; // 현재 페이즈에서 맞힌 문제 수

    @Column(nullable = false)
    private LocalDateTime phaseStartTime; // 현재 페이즈 시작 시간 (시간제한 체크용)
}
