package com.survival.repository;

import com.survival.Entity.UserSeriesCompletionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserSeriesCompletionRepository extends JpaRepository<UserSeriesCompletionEntity, Long> {
    // 특정 유저가 특정 시리즈를 완료했는지 확인하는 메서드
    Optional<UserSeriesCompletionEntity> findByUserIdAndSeriesId(Long userId, Long seriesId);
    // 특정 유저가 클리어한 시리즈 전부 조회하는 메서드
    List<UserSeriesCompletionEntity> findByUserId(Long userId);
}
