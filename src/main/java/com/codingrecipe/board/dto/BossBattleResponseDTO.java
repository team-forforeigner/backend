// 보스전 시작 또는 다음 페이즈 시작 시 클라이언트에 보낼 데이터를 담는 DTO
package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.BossPhase;
import com.codingrecipe.board.domain.BossStage;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BossBattleResponseDTO {

    private String bossName;
    private String bossImageUrl;
    private int maxHp;
    private int currentHp;
    private int phaseNumber;
    private String missionText;
    private int timeLimitSeconds;
    private List<QuizDetailResponse> quizzes;

    public BossBattleResponseDTO(BossStage stage, BossPhase phase, List<QuizDetailResponse> quizzes) {
        this.bossName = stage.getBossName();
        this.bossImageUrl = stage.getBossImageUrl();
        this.maxHp = stage.getTotalHp();
        this.currentHp = stage.getTotalHp(); // 시작 시에는 항상 최대 체력
        this.phaseNumber = phase.getPhaseNumber();
        this.missionText = phase.getMissionText();
        this.timeLimitSeconds = phase.getTimeLimitSeconds();
        this.quizzes = quizzes;
    }
}
