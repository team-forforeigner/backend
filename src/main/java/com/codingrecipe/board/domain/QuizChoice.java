// 퀴즈의 선택지 정보를 관리
package com.codingrecipe.board.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class QuizChoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 선택지 고유 식별자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    @JsonBackReference
    private Quiz quiz; // 이 선택지가 속한 퀴즈

    private String content; // 선택지의 내용 (예: "대한민국")

    private boolean isAnswer; // 이 선택지가 정답인지 여부

    // id 값이 같으면 같은 객체로 판단
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuizChoice that = (QuizChoice) o;
        return id != null && id.equals(that.id);
    }

    // id 값을 기반으로 해시 코드 생성
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
