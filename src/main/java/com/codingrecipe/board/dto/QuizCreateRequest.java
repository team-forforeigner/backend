// 새로운 퀴즈를 생성할 때 필요한 데이터들을 담는 DTO
package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.Category;
import com.codingrecipe.board.domain.QuizType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuizCreateRequest {
    private String title;
    private String imageUrl;
    private String hint;
    private String question;
    private QuizType quizType;
    private Category category;
    private String explanation;

    // --- 객관식 퀴즈용 필드 ---
    private List<ChoiceRequest> choices;

    @NotNull(message = "객관식 또는 OX 퀴즈는 정답 인덱스가 반드시 필요합니다.")
    private Integer correctChoiceIndex;

    // --- 주관식 퀴즈용 필드 ---
    private String shortAnswer;

    @Getter
    @Setter
    public static class ChoiceRequest {
        private String content;
    }
}