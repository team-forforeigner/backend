package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.Quiz;
import com.codingrecipe.board.domain.QuizChoice;
import com.codingrecipe.board.domain.QuizType;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 관리자 페이지에서 퀴즈 상세 정보를 보여주기 위한 DTO (정답 정보 포함)
 */
@Getter
public class QuizAdminDetailResponse {
    private final Long id;
    private final String title;
    private final String imageUrl;
    private final String question;
    private final String hint;
    private final QuizType quizType;
    private final String category;
    private final String explanation;
    private final List<ChoiceResponse> choices;
    private final String answer;

    public QuizAdminDetailResponse(Quiz quiz) {
        this.id = quiz.getId();
        this.title = quiz.getTitle();
        this.imageUrl = quiz.getImageUrl();
        this.question = quiz.getQuestion();
        this.hint = quiz.getHint();
        this.quizType = quiz.getQuizType();
        this.category = quiz.getCategory().getDescription();
        this.explanation = quiz.getExplanation();
        this.choices = quiz.getChoices().stream()
                .map(ChoiceResponse::new)
                .collect(Collectors.toList());
        this.answer = quiz.getAnswer(); // 엔티티에서 정답을 가져와 설정
    }

    // 선택지 정보와 정답 여부를 함께 담는 내부 DTO
    @Getter
    private static class ChoiceResponse {
        private final String content;
        private final boolean isAnswer; // 정답 여부 필드

        public ChoiceResponse(QuizChoice choice) {
            this.content = choice.getContent();
            this.isAnswer = choice.isAnswer();
        }
    }
}
