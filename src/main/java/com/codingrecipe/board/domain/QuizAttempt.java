// 사용자의 퀴즈 풀이 기록을 관리하는 엔티티
package com.codingrecipe.board.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "quiz_attempt") // 'quiz_attempt' 테이블과 매핑
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 풀이 기록 고유 식별자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @JsonBackReference("member-attempt") // 순환 참조 방지
    private Member member; // 퀴즈를 풀이한 사용자

// 서바이벌을 위해 생략
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "quiz_id")
//    @JsonBackReference("quiz-attempt") // 순환 참조 방지
//    private Quiz quiz; // 풀이한 퀴즈

    @Column(nullable = false)
    private boolean isCorrect; // 정답 여부 (true: 정답, false: 오답)

    @CreatedDate // 엔티티 생성 시 시간 자동 저장
    @Column(updatable = false) // 수정 시에는 업데이트되지 않도록 설정
    private LocalDateTime attemptedAt; // 퀴즈 풀이 시간
}