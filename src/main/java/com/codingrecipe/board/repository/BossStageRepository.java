// 보스 스테이지 데이터베이스 처리를 위한 리포지토리 인터페이스
package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.BossStage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BossStageRepository extends JpaRepository<BossStage, Long> {
}
