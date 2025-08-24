package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.SurvivalCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SurvivalCategoryRepository extends JpaRepository<SurvivalCategoryEntity, Long> {
    Optional<SurvivalCategoryEntity> findByCategoryId(Long categoryId);
}
