package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

// MemberRepository 인터페이스에 정의된 메소드들은
// Spring Data JPA 명명 규칙에 따라 자동으로 구현되므로,
// 이 인터페이스의 내용은 비워둡니다.
public interface SpringDataJpaMemberRepository extends JpaRepository<Member, Long>, MemberRepository {
    // 비워둠
}