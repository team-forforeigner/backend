package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<ReportEntity, Long> {
}