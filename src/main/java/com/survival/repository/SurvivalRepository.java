package com.survival.repository;

import org.springframework.stereotype.Repository;

// 기타 까다로운 쿼리나 로직 저장
// 미완성 상태이나 작동에는 무리 없음

@Repository
public interface SurvivalRepository {
    // ex) 특정 유저의 특정 시리즈 진행 기록에 대한 통계를 조회하는 메서드
    // List<SurvivalHistoryStats> findUserSeriesStats(Long userId, Long seriesId);
}
