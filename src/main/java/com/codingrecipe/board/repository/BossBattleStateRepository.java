// 보스전 상태 데이터베이스 처리를 위한 리포지토리 인터페이스
package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.BossBattleState;
import com.codingrecipe.board.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BossBattleStateRepository extends JpaRepository<BossBattleState, Long> {
    // 특정 사용자의 현재 보스전 상태 조회
    Optional<BossBattleState> findByMember(Member member);
}
