package com.codingrecipe.board.repository;

import com.codingrecipe.board.entity.BoardEntity;
import com.codingrecipe.board.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    // select * from comment_table where board_id=? order by id desc;
    List<CommentEntity> findAllByBoardEntityOrderByIdDesc(BoardEntity boardEntity);

    // BoardEntity의 id 필드를 기준으로 조회하고, BaseEntity의 createdTime 필드를 기준으로 오름차순 정렬합니다.
    List<CommentEntity> findAllByBoardEntity_IdOrderByCreatedTimeAsc(Long boardId);
}