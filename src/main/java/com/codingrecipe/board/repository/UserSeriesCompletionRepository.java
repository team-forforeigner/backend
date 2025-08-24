package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.domain.UserSeriesCompletionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSeriesCompletionRepository extends JpaRepository<UserSeriesCompletionEntity, Long> {
    Optional<UserSeriesCompletionEntity> findByMemberAndSeries_SeriesId(Member member, Long seriesId);
    List<UserSeriesCompletionEntity> findByMember(Member member);
}