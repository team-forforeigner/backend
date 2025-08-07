// 사용자의 퀴즈 풀이 기록을 응답으로 보낼 때 사용하는 DTO
package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.QuizAttempt;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class QuizAttemptResponse {

    private final Long attemptId; // 풀이 기록 고유 식별자
    private final Long quizId; // 풀이한 퀴즈의 ID
    private final String question; // 풀이한 퀴즈의 문제 내용
    private final boolean isCorrect; // 정답 여부
    private final LocalDateTime attemptedAt; // 퀴즈 풀이 시간

    public QuizAttemptResponse(QuizAttempt attempt) {
        this.attemptId = attempt.getId();
        this.quizId = attempt.getQuiz().getId();
        this.question = attempt.getQuiz().getQuestion();
        this.isCorrect = attempt.isCorrect();
        this.attemptedAt = attempt.getAttemptedAt();
    }
}
