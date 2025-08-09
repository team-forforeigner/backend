// 댓글 데이터베이스 처리를 위한 리포지토리 인터페이스
package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.BoardEntity;
import com.codingrecipe.board.domain.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    // 특정 게시글에 달린 모든 댓글을 작성 시간 오름차순으로 조회
    List<CommentEntity> findAllByBoardEntity_IdOrderByCreatedTimeAsc(Long boardId);
}
