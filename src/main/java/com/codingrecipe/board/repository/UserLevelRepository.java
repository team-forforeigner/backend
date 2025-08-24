package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.domain.UserLevelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLevelRepository extends JpaRepository<UserLevelEntity, Long> {
    Optional<UserLevelEntity> findByMember(Member member);
    List<UserLevelEntity> findAllByOrderByCompletedSeriesCountDesc();
}