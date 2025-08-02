package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 이메일로 회원을 찾는다.
    Optional<Member> findByEmail(String email);

    // 랭킹 기능을 위해 경험치 순으로 상위 100명을 찾는다.
    List<Member> findTop100ByOrderByExperienceDesc();
}