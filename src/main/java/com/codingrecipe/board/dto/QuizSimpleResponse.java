// 퀴즈 목록 조회 등 간략한 정보만 필요할 때 사용하는 DTO
package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.Quiz;
import com.codingrecipe.board.domain.QuizType;
import lombok.Getter;

@Getter
public class QuizSimpleResponse {
    private final Long id; // 퀴즈 고유 식별자
    private final String question; // 퀴즈 문제 내용
    private final QuizType quizType; // 퀴즈 유형


    public QuizSimpleResponse(Quiz quiz) {
        this.id = quiz.getId();
        this.question = quiz.getQuestion();
        this.quizType = quiz.getQuizType();
    }
}
