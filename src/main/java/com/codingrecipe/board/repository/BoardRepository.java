// 게시글 데이터베이스 처리를 위한 리포지토리 인터페이스
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

    // JPQL을 사용하여 특정 ID의 게시글 조회수를 1 증가시킴
    @Modifying
    @Query(value = "update BoardEntity b set b.boardHits=b.boardHits+1 where b.id=:id")
    void updateHits(@Param("id") Long id);

    // 좋아요가 많은 순서대로 상위 3개 게시글 조회
    List<BoardEntity> findTop3ByOrderByBoardLikesDesc();

    // 특정 카테고리 ID에 해당하는 게시글들을 페이징하여 조회
    Page<BoardEntity> findByCategoryId(Long categoryId, Pageable pageable);

    // 특정 작성자 이메일(writer.email)에 해당하는 게시글들을 페이징하여 조회
    Page<BoardEntity> findByWriter_Email(String email, Pageable pageable);

    // 제목 또는 내용에 특정 키워드가 포함된 게시글들을 페이징하여 조회
    Page<BoardEntity> findByBoardTitleContainingOrBoardContentsContaining(String titleKeyword, String contentsKeyword, Pageable pageable);
}
