package com.codingrecipe.board.domain;

public enum QuizType {
    MULTIPLE_CHOICE("객관식"),
    SHORT_ANSWER("단답형"),
    OX("O/X"); // OX 타입

    private final String description;

    QuizType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}