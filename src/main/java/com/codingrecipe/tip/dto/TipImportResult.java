package com.codingrecipe.tip.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 설명 : 팁 import 결과를 담는 DTO
 */

@Getter
@AllArgsConstructor
public class TipImportResult {

    private int savedCount; // 정상적으로 저장된 팁 개수
    private List<String> duplicatedQuestions; // 중복된 질문 목록

}
