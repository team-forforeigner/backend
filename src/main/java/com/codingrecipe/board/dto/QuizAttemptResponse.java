package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.QuizAttempt;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class QuizAttemptResponse {

    private final Long attemptId;
    private final Long quizId;
    private final String question;
    private final boolean isCorrect;
    private final LocalDateTime attemptedAt;

    public QuizAttemptResponse(QuizAttempt attempt) {
        this.attemptId = attempt.getId();
        this.quizId = attempt.getQuiz().getId();
        this.question = attempt.getQuiz().getQuestion();
        this.isCorrect = attempt.isCorrect();
        this.attemptedAt = attempt.getAttemptedAt();
    }
}