package com.codingrecipe.board.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String title;

    @Column(nullable = false)
    private String question;

    private String hint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizType quizType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Lob
    private String explanation;

    private String imageUrl;

    private boolean isTranslatable = true;

    private boolean isActive = true;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("quiz-choice")
    private List<QuizChoice> choices = new ArrayList<>();

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("quiz-attempt")
    private List<QuizAttempt> attempts = new ArrayList<>();

    public String getAnswer() {
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        return choices.stream()
                .filter(QuizChoice::isAnswer)
                .findFirst()
                .map(QuizChoice::getContent)
                .orElse(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quiz quiz = (Quiz) o;
        return id != null && id.equals(quiz.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}