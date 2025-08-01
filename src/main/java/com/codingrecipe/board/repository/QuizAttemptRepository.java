package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.domain.Quiz;
import com.codingrecipe.board.domain.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    // Member ID로 퀴즈 기록을 최신순으로 조회
    List<QuizAttempt> findByMemberIdOrderByAttemptedAtDesc(Long memberId);

    // Member 객체로 해당 사용자의 모든 오답 기록을 조회
    List<QuizAttempt> findAllByMemberAndIsCorrectFalse(Member member);

    // 특정 사용자가 특정 문제를 틀린 기록이 있는지 확인
    boolean existsByMemberAndQuizAndIsCorrectFalse(Member member, Quiz quiz);

    // 사용자가 푼 모든 기록을 조회하는 메소드
    List<QuizAttempt> findByMember(Member member);
}