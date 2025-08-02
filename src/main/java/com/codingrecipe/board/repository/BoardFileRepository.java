package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.BoardFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardFileRepository extends JpaRepository<BoardFileEntity, Long> {
}