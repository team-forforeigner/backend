// 퀴즈 답안 제출 시 필요한 데이터를 담는 DTO
package com.codingrecipe.board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitAnswerRequest {
    private Long userId; // 퀴즈를 푸는 사용자의 ID
    private Long quizId; // 풀이 대상 퀴즈의 ID
    private String userAnswer; // 사용자가 제출한 답안
    private boolean fromRetryList = false; // 오답 노트에서 문제를 푸는 것인지 여부
}
