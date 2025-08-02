package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.domain.BoardEntity;
import com.codingrecipe.board.domain.BoardLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoardLikeRepository extends JpaRepository<BoardLikeEntity, Long> {

    // 사용자와 게시글로 '좋아요' 정보가 있는지 확인 (중복 방지용)
    Optional<BoardLikeEntity> findByMemberAndBoard(Member member, BoardEntity board);

    // 사용자가 '좋아요' 누른 모든 게시글 목록 조회
    List<BoardLikeEntity> findAllByMember(Member member);
}