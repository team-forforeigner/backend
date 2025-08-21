package com.survival.repository;

import com.survival.Entity.SeriesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeriesRepository extends JpaRepository<SeriesEntity, Long> {
    // SeriesEntity의 category 필드의 categoryId를 기준으로 조회
    List<SeriesEntity> findByCategory_CategoryId(Long categoryId);
}