// [최종 수정본] com/codingrecipe/board/repository/CommentRepository.java

package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    // --- N+1 문제 해결을 위한 Fetch Join 적용 ---
    @Query("SELECT c FROM CommentEntity c LEFT JOIN FETCH c.writer WHERE c.boardEntity.id = :boardId ORDER BY c.createdTime ASC")
    List<CommentEntity> findAllByBoardIdWithWriter(@Param("boardId") Long boardId);

}