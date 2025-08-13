package com.survival.repository;

import com.survival.Entity.SeriesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// 시리즈 엔티티에 대한 접근 담당
@Repository
public interface SeriesRepository extends JpaRepository<SeriesEntity, Long> {
    // 특정 categoryId에 속한 모든 SeriesEntity를 조회하는 메서드
    List<SeriesEntity> findByCategoryCategoryId(Long categoryId);
}
