// 회원 데이터베이스 처리를 위한 리포지토리 인터페이스
package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 이메일로 회원 정보 조회
    Optional<Member> findByEmail(String email);

    // 경험치 높은 순으로 상위 100명 조회 (랭킹용)
    List<Member> findTop100ByOrderByExperienceDesc();
}
