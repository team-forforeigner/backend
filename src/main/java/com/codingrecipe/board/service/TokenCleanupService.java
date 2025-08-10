package com.codingrecipe.board.service;

import com.codingrecipe.board.repository.LoggedOutTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * DB에 저장된 만료된 로그아웃 토큰을 주기적으로 삭제하는 스케줄러 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {

    private final LoggedOutTokenRepository loggedOutTokenRepository;

    // 매일 새벽 4시에 실행 (cron = "초 분 시 일 월 요일")
    @Scheduled(cron = "0 0 4 * * *")
    public void cleanupExpiredTokens() {
        log.info("[스케줄러] 만료된 로그아웃 토큰 삭제 작업을 시작합니다.");
        LocalDateTime now = LocalDateTime.now();
        loggedOutTokenRepository.deleteByExpiryAtBefore(now);
        log.info("[스케줄러] 만료된 로그아웃 토큰 삭제 작업을 완료했습니다.");
    }
}
