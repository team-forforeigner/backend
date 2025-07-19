package com.codingrecipe.tip;

// 설명 : 팁 카테고리입니다. (추후) 태그로 구분합니다.
public enum TipCategory {

    LIVING("주거/일상생활"),
    CULTURE("문화/전통"),
    LANGUAGE("언어"),
    LAW("법률/제도"),
    MONEY("금융/환전"),
    FOOD("음식/식당"),
    TECH("기술/인터넷"),
    TRAVEL("여행/교통"),
    SHOPPING("쇼핑/구매"),
    ETIQUETTE("예절/에티켓"),
    SUPPORT("지원제도/서비스"),
    EMERGENCY("응급상황/안전"),
    WEATHER("날씨"),
    K_CULTURE("K-문화/대중문화"),
    ETC("기타");


    private final String description;

    // enum은 기본 생성자 필요 없음. 그냥 생성자만 정의하면 됨.
    TipCategory(String description) {
        this.description = description;
    }

}
