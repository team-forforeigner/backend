package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.BoardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardRepository extends JpaRepository<BoardEntity, Long> {

    @Modifying
    @Query(value = "update BoardEntity b set b.boardHits=b.boardHits+1 where b.id=:id")
    void updateHits(@Param("id") Long id);

    List<BoardEntity> findTop3ByOrderByBoardLikesDesc();

    Page<BoardEntity> findByCategoryId(Long categoryId, Pageable pageable);

    Page<BoardEntity> findByWriter_Email(String email, Pageable pageable);

    Page<BoardEntity> findByBoardTitleContainingOrBoardContentsContaining(String titleKeyword, String contentsKeyword, Pageable pageable);
}