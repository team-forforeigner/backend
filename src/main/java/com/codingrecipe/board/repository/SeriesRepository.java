package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.SeriesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeriesRepository extends JpaRepository<SeriesEntity, Long> {
    List<SeriesEntity> findByCategory_CategoryId(Long categoryId);
}