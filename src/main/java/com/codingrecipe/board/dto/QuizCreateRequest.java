// 새로운 퀴즈를 생성할 때 필요한 데이터들을 담는 DTO
package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.Category;
import com.codingrecipe.board.domain.QuizType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuizCreateRequest {
    private String title; // 퀴즈 제목
    private String imageUrl; // 퀴즈 관련 이미지 URL
    private String hint; // 퀴즈 힌트
    private String question; // 퀴즈 문제 내용
    private QuizType quizType; // 퀴즈 유형 (객관식, 단답형, OX)
    private Category category; // 퀴즈 카테고리 (신조어, 역사 등)
    private String explanation; // 정답 해설

    // --- 객관식 퀴즈용 필드 ---
    private List<ChoiceRequest> choices; // 선택지 목록
    private int correctChoiceIndex; // 정답 선택지의 인덱스 (0부터 시작)

    // --- 주관식 퀴즈용 필드 ---
    private String shortAnswer; // 단답형 정답

    // 객관식 선택지의 내용을 담는 내부 클래스
    @Getter
    @Setter
    public static class ChoiceRequest {
        private String content; // 선택지 내용
    }
}
