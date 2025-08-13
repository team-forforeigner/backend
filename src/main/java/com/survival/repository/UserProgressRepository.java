package com.survival.repository;

import com.survival.Entity.UserProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// UserProgress 엔티티에 대한 데이터 접근을 담당 / 사용자 진행기록 담당
@Repository
public interface UserProgressRepository extends JpaRepository<UserProgressEntity, Long> {
    // 특정 유저와 시리즈의 진행 기록을 조회하는 메서드
    List<UserProgressEntity> findByUserIdAndSeriesId(Long userId, Long seriesId);

    // 특정 유저와 시리즈의 진행 기록을 삭제하는 메서드
    void deleteByUserIdAndSeriesId(Long userId, Long seriesId);
}
