// 퀴즈 답안 제출 후, 서버가 클라이언트에게 보내는 응답 데이터를 담는 DTO
package com.codingrecipe.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SubmitAnswerResponse {
    private boolean isCorrect;  // 정답 여부
    private String explanation; // 퀴즈 정답에 대한 해설
}
