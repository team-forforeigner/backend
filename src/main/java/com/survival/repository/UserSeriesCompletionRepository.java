package com.survival.repository;

import com.survival.Entity.UserSeriesCompletionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserSeriesCompletionRepository extends JpaRepository<UserSeriesCompletionEntity, Long> {

    Optional<UserSeriesCompletionEntity> findByUserIdAndSeriesId(Long userId, Long seriesId);

    List<UserSeriesCompletionEntity> findByUserId(Long userId);
}