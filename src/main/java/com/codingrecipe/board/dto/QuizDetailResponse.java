// 퀴즈의 상세 정보를 클라이언트에 응답으로 보낼 때 사용하는 DTO
package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.Quiz;
import com.codingrecipe.board.domain.QuizChoice;
import com.codingrecipe.board.domain.QuizType;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class QuizDetailResponse {
    private final Long id; // 퀴즈 고유 식별자
    private final String title; // 퀴즈 제목
    private final String imageUrl; // 퀴즈 관련 이미지 URL
    private final String question; // 퀴즈 문제 내용
    private final String hint; // 퀴즈 힌트 (사용자 설정에 따라 null일 수 있음)
    private final QuizType quizType; // 퀴즈 유형
    private final String category; // 퀴즈 카테고리 (한글 설명)
    private final String explanation; // 정답 해설
    private final List<ChoiceResponse> choices; // 선택지 목록

    /**
     * Quiz 엔티티와 사용자의 힌트 설정 여부를 받아 DTO를 생성
     */
    public QuizDetailResponse(Quiz quiz, boolean hintEnabled) {
        this.id = quiz.getId();
        this.title = quiz.getTitle();
        this.imageUrl = quiz.getImageUrl();
        this.question = quiz.getQuestion();
        this.hint = hintEnabled ? quiz.getHint() : null; // 힌트 설정이 활성화된 경우에만 힌트 제공
        this.quizType = quiz.getQuizType();
        this.category = quiz.getCategory().getDescription();
        this.explanation = quiz.getExplanation();
        this.choices = quiz.getChoices().stream()
                .map(ChoiceResponse::new) // 각 QuizChoice를 ChoiceResponse로 변환
                .collect(Collectors.toList());
    }

    // 힌트 설정을 고려하지 않고 무조건 힌트를 포함하여 DTO를 생성하는 보조 생성자
    public QuizDetailResponse(Quiz quiz) {
        this(quiz, true); // 기본적으로 힌트를 활성화한 상태로 위 생성자 호출
    }

    // 퀴즈 선택지의 내용만 담는 내부 응답 클래스
    @Getter
    private static class ChoiceResponse {
        private final String content; // 선택지 내용

        public ChoiceResponse(QuizChoice choice) {
            this.content = choice.getContent();
        }
    }
}
