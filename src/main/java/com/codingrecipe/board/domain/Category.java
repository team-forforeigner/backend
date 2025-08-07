// 퀴즈의 카테고리 타입을 정의
package com.codingrecipe.board.domain;

public enum Category {
    ALL("전체 퀴즈"), // 전체 퀴즈
    NEW_WORDS("신조어/유행어"), // 신조어/유행어
    HISTORY("K-역사"), // K-역사
    CULTURE("K-문화"), // K-문화
    K_CONTENTS("K-콘텐츠"); // K-콘텐츠

    private final String description; // 카테고리의 한글 설명

    Category(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
