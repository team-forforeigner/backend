// 퀴즈 문제, 선택지, 정답 등 퀴즈와 관련된 정보를 관리하는 엔티티
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
    private Long id; // 퀴즈 고유 식별자

    @Column(length = 100)
    private String title; // 퀴즈 제목

    @Column(nullable = false)
    private String question; // 퀴즈 문제 내용

    private String hint; // 퀴즈 힌트

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizType quizType; // 퀴즈 유형 (객관식, 주관식 등)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category; // 퀴즈 카테고리 (신조어, 역사 등)

    @Lob // 대용량 텍스트를 저장하기 위한 어노테이션
    private String explanation; // 퀴즈 정답에 대한 해설

    private String imageUrl; // 퀴즈와 관련된 이미지 URL

    private boolean isTranslatable = true; // 문제/선택지 번역 가능 여부

    private boolean isActive = true; // 퀴즈 활성화 상태 (사용자에게 노출 여부)

    // --- [보스전] 신규 필드 추가 ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boss_phase_id") // 'boss_phase_id'로 BossPhase와 조인
    private BossPhase bossPhase; // 이 퀴즈가 속한 보스전 페이즈 (일반 퀴즈는 null)
    // --------------------------------

    // 퀴즈가 삭제되면 연관된 선택지들도 함께 삭제
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("quiz-choice") // 순환 참조 방지
    private List<QuizChoice> choices = new ArrayList<>();

    // 퀴즈가 삭제되면 연관된 시도 기록들도 함께 삭제
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("quiz-attempt") // 순환 참조 방지
    private List<QuizAttempt> attempts = new ArrayList<>();

    /**
     * 선택지 목록에서 정답인 선택지를 찾아 그 내용을 반환
     */
    public String getAnswer() {
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        return choices.stream()
                .filter(QuizChoice::isAnswer) // 정답인 선택지만 필터링
                .findFirst() // 첫 번째 정답을 찾음
                .map(QuizChoice::getContent) // 선택지의 내용을 가져옴
                .orElse(null); // 정답이 없으면 null 반환
    }

    /**
     * id 값이 같으면 같은 객체로 판단
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quiz quiz = (Quiz) o;
        return id != null && id.equals(quiz.id);
    }

    /**
     * id 값을 기반으로 해시 코드 생성
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
