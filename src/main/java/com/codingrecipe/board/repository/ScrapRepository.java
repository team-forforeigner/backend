// 게시글 스크랩 데이터베이스 처리를 위한 리포지토리 인터페이스
package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.BoardEntity;
import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.domain.ScrapEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScrapRepository extends JpaRepository<ScrapEntity, Long> {

    // 특정 사용자와 게시글로 스크랩 기록 조회 (중복 스크랩 방지용)
    Optional<ScrapEntity> findByMemberAndBoard(Member member, BoardEntity board);

    // 특정 사용자가 스크랩한 모든 기록 조회
    List<ScrapEntity> findAllByMember(Member member);
}
