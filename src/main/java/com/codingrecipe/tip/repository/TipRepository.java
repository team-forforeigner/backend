package com.codingrecipe.tip.repository;

import com.codingrecipe.tip.TipCategory;
import com.codingrecipe.tip.entity.TipEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TipRepository extends JpaRepository<TipEntity, Long> {

    // Page : 결과 목록과 페이징 정보가 함께 담긴 객체.

    Page<TipEntity> findByCategory(TipCategory category, Pageable pageable);

    Page<TipEntity> findAll(Pageable pageable);

    boolean existsByQuestion(String question);

}
