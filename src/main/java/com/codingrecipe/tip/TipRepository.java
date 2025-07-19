package com.codingrecipe.tip;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;

@Repository
public interface TipRepository extends JpaRepository<Tip, Long> {

    // Page : 결과 목록 + 페이징 정보가 함께 담긴 객체이다.
    // Pageable 파라미터 : 페이징 처리

    // 1. 키워드 검색
    @Query("SELECT t FROM Tip t WHERE t.question LIKE %:keyword% OR t.answer LIKE %:keyword%")
    Page<Tip> findByKeyword(String question, String answer, Pageable pageable);

    // 2. 태그 검색
    Page<Tip> findByTags(String tag, Pageable pageable);

}
