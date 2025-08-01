package com.codingrecipe.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SubmitAnswerResponse {
    private boolean isCorrect;
    private String explanation;
}