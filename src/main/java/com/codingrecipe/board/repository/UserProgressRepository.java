package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.UserProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgressEntity, Long> {

    @Query("SELECT up FROM UserProgressEntity up " +
            "JOIN FETCH up.episode e " +
            "JOIN FETCH up.choice c " +
            "WHERE up.member.id = :userId AND up.series.seriesId = :seriesId " +
            "ORDER BY up.playedAt ASC")
    List<UserProgressEntity> findHistory(@Param("userId") Long userId, @Param("seriesId") Long seriesId);

    void deleteByMember_IdAndSeries_SeriesId(Long userId, Long seriesId);
}