package com.codingrecipe.board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitAnswerRequest {
    private Long userId;
    private Long quizId;
    private String userAnswer;
    private boolean fromRetryList = false;
}