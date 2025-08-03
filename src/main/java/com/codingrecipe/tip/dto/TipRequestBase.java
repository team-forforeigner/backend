package com.codingrecipe.tip.dto;

import com.codingrecipe.tip.domain.TipCategory;

/**
 * 설명 : 팁 요청의 기본 인터페이스입니다.
 */

public interface TipRequestBase {

    String getQuestion();
    String getAnswer();
    TipCategory getCategory();

}
