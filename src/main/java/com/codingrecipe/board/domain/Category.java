package com.codingrecipe.board.domain;

public enum Category {
    ALL("전체 퀴즈"),
    // "신조어/유행어"
    NEW_WORDS("신조어/유행어"),
    // "K-역사"
    HISTORY("K-역사"),
    // "K-문화"
    CULTURE("K-문화"),
    // "K-콘테츠"
    K_CONTENTS("K-콘텐츠");

    private final String description;

    Category(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}