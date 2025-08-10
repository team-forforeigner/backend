package com.codingrecipe.board.service;

import com.codingrecipe.board.domain.LoggedOutToken;
import com.codingrecipe.board.repository.LoggedOutTokenRepository;
import com.codingrecipe.board.security.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class LogoutService {

    private final LoggedOutTokenRepository loggedOutTokenRepository;
    private final JwtUtil jwtUtil; // 토큰에서 만료 시간을 가져오기 위해 주입

    /**
     * 로그아웃 요청 시 전달된 토큰을 DB 기반의 블랙리스트에 추가
     * @param token 로그아웃할 JWT
     */
    @Transactional
    public void logout(String token) {
        // 1. 토큰이 유효한지 먼저 확인 (선택적이지만, 불필요한 DB 저장을 막을 수 있음)
        if (jwtUtil.isTokenExpired(token)) {
            return; // 이미 만료된 토큰은 처리할 필요 없음
        }

        // 2. 토큰의 만료 시간을 LocalDateTime으로 변환
        Date expirationDate = jwtUtil.extractClaim(token, Claims::getExpiration);
        LocalDateTime expiryAt = expirationDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        // 3. LoggedOutToken 엔티티를 생성하여 DB에 저장
        LoggedOutToken loggedOutToken = new LoggedOutToken(token, expiryAt);
        loggedOutTokenRepository.save(loggedOutToken);
    }
}
