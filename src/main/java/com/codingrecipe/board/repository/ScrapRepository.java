package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.BoardEntity;
import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.domain.ScrapEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScrapRepository extends JpaRepository<ScrapEntity, Long> {

    // 사용자와 게시글로 스크랩 정보가 있는지 확인 (중복 방지용)
    Optional<ScrapEntity> findByMemberAndBoard(Member member, BoardEntity board);

    // 사용자가 스크랩한 모든 게시글 목록 조회
    List<ScrapEntity> findAllByMember(Member member);
}