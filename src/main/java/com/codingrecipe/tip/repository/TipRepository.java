package com.codingrecipe.tip.repository;

import com.codingrecipe.tip.TipCategory;
import com.codingrecipe.tip.TipEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;

@Repository
public interface TipRepository extends JpaRepository<TipEntity, Long> {

    // Page : 결과 목록 + 페이징 정보가 함께 담긴 객체
    // Pageable 파라미터 : 페이징 처리

    // 1. 카테고리 검색
    // 설명 : categories에 포함된 카테고리 중 하나라도 매칭되는 팁 조회
    Page<TipEntity> findByCategoriesIn(TipCategory category, Pageable pageable);

}
