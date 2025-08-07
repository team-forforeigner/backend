// 보스전의 각 단계를 관리하는 엔티티
package com.codingrecipe.board.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "boss_phase") // 'boss_phase' 테이블과 매핑
public class BossPhase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 페이즈 고유 식별자

    @Column(nullable = false)
    private int phaseNumber; // 페이즈 번호 (1, 2...)

    @Column
    private String missionText; // 페이즈 미션 설명 (예: "총 7문제 중 4문제 이상을 맞혀라!")

    @Column(nullable = false)
    private int damagePerQuiz; // 문제당 데미지 (예: 100)

    @Column(nullable = false)
    private int timeLimitSeconds; // 페이즈 전체 제한시간 (초 단위)

    @Column(nullable = false)
    private int totalQuestions; // 해당 페이즈의 총 문제 수

    @Column(nullable = false)
    private int requiredCorrectAnswers; // 통과에 필요한 정답 수

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boss_stage_id") // 'boss_stage_id'로 BossStage와 조인
    private BossStage bossStage; // 이 페이즈가 속한 보스 스테이지
}
