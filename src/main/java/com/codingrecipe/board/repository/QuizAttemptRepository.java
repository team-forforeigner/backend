// 퀴즈 풀이 기록 데이터베이스 처리를 위한 리포지토리 인터페이스
package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.domain.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    // 특정 사용자의 모든 퀴즈 풀이 기록을 최신순으로 조회
    List<QuizAttempt> findByMemberIdOrderByAttemptedAtDesc(Long memberId);

    // 특정 사용자의 모든 오답 기록 조회
    List<QuizAttempt> findAllByMemberAndIsCorrectFalse(Member member);

    // 특정 사용자의 모든 풀이 기록 조회
    List<QuizAttempt> findByMember(Member member);
}
