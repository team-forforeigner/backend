// 신고 데이터베이스 처리를 위한 리포지토리 인터페이스
package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<ReportEntity, Long> {
    // JpaRepository를 상속받아 기본적인 CRUD(Create, Read, Update, Delete) 기능을 자동 생성
}
