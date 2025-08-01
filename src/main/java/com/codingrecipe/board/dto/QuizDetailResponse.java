package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.Quiz;
import com.codingrecipe.board.domain.QuizChoice;
import com.codingrecipe.board.domain.QuizType;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class QuizDetailResponse {
    private final Long id;
    private final String title;
    private final String imageUrl;
    private final String question;
    private final String hint;
    private final QuizType quizType;
    private final String category;
    private final String explanation;
    private final List<ChoiceResponse> choices;

    public QuizDetailResponse(Quiz quiz, boolean hintEnabled) {
        this.id = quiz.getId();
        this.title = quiz.getTitle();
        this.imageUrl = quiz.getImageUrl();
        this.question = quiz.getQuestion();
        this.hint = hintEnabled ? quiz.getHint() : null;
        this.quizType = quiz.getQuizType();
        this.category = quiz.getCategory().getDescription();
        this.explanation = quiz.getExplanation();
        this.choices = quiz.getChoices().stream()
                .map(ChoiceResponse::new)
                .collect(Collectors.toList());
    }

    public QuizDetailResponse(Quiz quiz) {
        this(quiz, true);
    }

    @Getter
    private static class ChoiceResponse {
        private final String content;

        public ChoiceResponse(QuizChoice choice) {
            this.content = choice.getContent();
        }
    }
}