package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataJpaMemberRepository extends JpaRepository<Member, Long>, MemberRepository {


    // 아래 @Override 된 메서드들은 MemberRepository에 정의된 기능들을
    // Spring Data JPA가 자동으로 구현하도록 연결해주는 역할을 합니다.
    @Override
    Optional<Member> findByName(String name);

    @Override
    Optional<Member> findByEmail(String email);

    @Override
    Optional<Member> findByUserId(String userId);

    @Override
    Optional<Member> findByNameAndEmail(String name, String email);

    @Override
    Optional<Member> findByUserIdAndNameAndEmail(String userId, String name, String email);

    // Spring Data JPA가 자동으로 쿼리 생성
    @Override
    Optional<Member> findByProviderAndProviderId(String provider, String providerId);
}