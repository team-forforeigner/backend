package com.codingrecipe.board.repository;

import com.codingrecipe.board.entity.BoardEntity;
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

    @Modifying
    @Query("update BoardEntity b set b.boardLikes = b.boardLikes + 1 where b.id = :id")
    void incrementLikes(@Param("id") Long id);

    List<BoardEntity> findTop3ByOrderByBoardLikesDesc();

    // 카테고리 ID로 게시글 목록을 페이징하여 조회
    Page<BoardEntity> findByCategoryId(Long categoryId, Pageable pageable);

    // 작성자 ID로 게시글 목록을 페이징하여 조회
    Page<BoardEntity> findByWriter_UserId(String userId, Pageable pageable);

    // 제목 또는 내용에 키워드가 포함된 게시글을 페이징하여 검색
    Page<BoardEntity> findByBoardTitleContainingOrBoardContentsContaining(String titleKeyword, String contentsKeyword, Pageable pageable);
}