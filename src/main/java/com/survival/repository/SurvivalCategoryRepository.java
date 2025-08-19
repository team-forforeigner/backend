package com.survival.repository;

import com.survival.Entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// 카테고리 엔티티에 대한 접근 담당
@Repository
public interface SurvivalCategoryRepository extends JpaRepository<CategoryEntity, Long> {
    // categoryId로 CategoryEntity를 조회하는 메서드
    Optional<CategoryEntity> findByCategoryId(Long categoryId);
}
