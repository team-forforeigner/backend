package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.Category;
import com.codingrecipe.board.domain.QuizType;
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
    private List<ChoiceRequest> choices;
    private int correctChoiceIndex;
    private String shortAnswer;

    @Getter
    @Setter
    public static class ChoiceRequest {
        private String content;
    }
}