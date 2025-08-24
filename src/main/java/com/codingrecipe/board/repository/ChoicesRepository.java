package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.ChoicesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChoicesRepository extends JpaRepository<ChoicesEntity, Long> {
    Optional<ChoicesEntity> findByChoiceId(Long choiceId);
}