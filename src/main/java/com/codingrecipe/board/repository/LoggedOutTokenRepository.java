package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.LoggedOutToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface LoggedOutTokenRepository extends JpaRepository<LoggedOutToken, Long> {

    /**
     * 주어진 토큰이 DB(블랙리스트)에 존재하는지 확인
     * @param token 확인할 JWT
     * @return 존재하면 true, 아니면 false
     */
    boolean existsByToken(String token);

    /**
     * 주어진 시간 이전에 만료된 모든 토큰을 DB에서 삭제
     * @param now 현재 시간
     */
    @Transactional
    void deleteByExpiryAtBefore(LocalDateTime now);
}
