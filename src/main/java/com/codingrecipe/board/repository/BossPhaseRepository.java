// 보스 페이즈 데이터베이스 처리를 위한 리포지토리 인터페이스
package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.BossPhase;
import com.codingrecipe.board.domain.BossStage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BossPhaseRepository extends JpaRepository<BossPhase, Long> {
    // 특정 보스의 특정 페이즈 번호로 조회
    Optional<BossPhase> findByBossStageAndPhaseNumber(BossStage bossStage, int phaseNumber);
}
