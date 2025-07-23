package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.Member;
import java.util.List;
import java.util.Optional;

// 회원 데이터에 접근하기 위한 규칙(메서드)을 정의하는 인터페이스 (수정됨)
public interface MemberRepository {

    // 회원 정보를 저장하거나 업데이트
    Member save(Member member);

    // DB의 기본 키(id)로 회원을 찾는다.
    Optional<Member> findById(Long id);

    // 이메일로 회원을 찾는다. (이제 주요 식별자)
    Optional<Member> findByEmail(String email);

    // 소셜 로그인 제공자와 제공자 ID로 회원을 찾는다.
    Optional<Member> findByProviderAndProviderId(String provider, String providerId);

    // 모든 회원 목록을 반환
    List<Member> findAll();
}