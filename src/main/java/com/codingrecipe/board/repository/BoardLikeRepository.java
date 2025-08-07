// 게시글 좋아요 데이터베이스 처리를 위한 리포지토리 인터페이스
package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.domain.BoardEntity;
import com.codingrecipe.board.domain.BoardLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoardLikeRepository extends JpaRepository<BoardLikeEntity, Long> {

    // 특정 사용자와 게시글로 좋아요 기록 조회 (중복 좋아요 방지용)
    Optional<BoardLikeEntity> findByMemberAndBoard(Member member, BoardEntity board);

    // 특정 사용자가 누른 모든 좋아요 기록 조회
    List<BoardLikeEntity> findAllByMember(Member member);
}
