package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.Quiz;
import com.codingrecipe.board.domain.QuizType;
import lombok.Getter;

@Getter
public class QuizSimpleResponse {
    private final Long id;
    private final String question;
    private final QuizType quizType;

    public QuizSimpleResponse(Quiz quiz) {
        this.id = quiz.getId();
        this.question = quiz.getQuestion();
        this.quizType = quiz.getQuizType();
    }
}