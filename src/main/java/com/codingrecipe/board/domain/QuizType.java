// 퀴즈의 유형(객관식, 단답형 등)을 정의
package com.codingrecipe.board.domain;

public enum QuizType {
    MULTIPLE_CHOICE("객관식"), // 객관식 문제 유형
    SHORT_ANSWER("단답형"),    // 단답형 문제 유형
    OX("O/X");                 // OX 문제 유형

    private final String description; // 퀴즈 유형의 한글 설명

    QuizType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
