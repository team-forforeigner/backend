// 보스전 답안 제출 후 서버가 클라이언트에 보내는 응답 데이터를 담는 DTO
package com.codingrecipe.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SubmitBossAnswerResponseDTO {
    private boolean isCorrect;          // 현재 제출한 답안의 정답 여부
    private int currentHp;              // 갱신된 보스의 현재 체력
    private boolean phaseClear;         // 현재 페이즈 클리어 여부
    private boolean battleClear;        // 보스전 최종 클리어 여부
    private boolean isTimeOut;          // 시간 초과 여부
    private BossBattleResponseDTO nextPhaseData; // 다음 페이즈 정보 (페이즈 클리어 시에만 데이터 포함)
}
