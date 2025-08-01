package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// JpaRepository를 상속받도록 수정합니다.
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 회원 정보를 저장하거나 업데이트 (JpaRepository가 제공)
    // Member save(Member member);

    // DB의 기본 키(id)로 회원을 찾는다. (JpaRepository가 제공)
    // Optional<Member> findById(Long id);

    // 이메일로 회원을 찾는다.
    Optional<Member> findByEmail(String email);

    // 소셜 로그인 제공자와 제공자 ID로 회원을 찾는다.
    Optional<Member> findByProviderAndProviderId(String provider, String providerId);

    // 모든 회원 목록을 반환 (JpaRepository가 제공)
    // List<Member> findAll();

    List<Member> findTop100ByOrderByExperienceDesc();
}