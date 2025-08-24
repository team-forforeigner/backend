// JWT 생성, 검증, 파싱 등 관련 유틸리티 클래스
package com.codingrecipe.board.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtil {

    private final Key secretKey; // JWT 서명에 사용할 비밀 키
    private final long expirationMs; // JWT 만료 시간

    /**
     * application.yml에서 JWT 비밀키와 만료 시간을 주입받아 초기화
     */
    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration-ms:86400000}") long expirationMs) {
        // 문자열 형태의 비밀키를 Key 객체로 변환
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        log.info("JWT Secret Key가 성공적으로 로드되었습니다");
    }

    /**
     * 토큰에서 사용자 이메일(subject)을 추출
     */
    public String getEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 토큰의 만료 여부를 확인
     */
    public boolean isTokenExpired(String token) {
        // 만료 시간이 현재 시간보다 이전인지 확인
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * 토큰의 남은 유효 시간을 밀리초 단위로 반환
     */
    public long getExpiration(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        long now = new Date().getTime();
        return expiration.getTime() - now;
    }

    /**
     * HttpServletRequest의 헤더에서 "Bearer " 토큰을 추출
     */
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 주어진 이메일로 새로운 JWT 토큰을 생성
     */
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email) // 토큰의 주체로 이메일 설정
                .claim("role", role) // 역할 정보 추가. DB에서 역할 정보 조회 후 설정
                .setIssuedAt(new Date(System.currentTimeMillis())) // 발급 시간 설정
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs)) // 만료 시간 설정
                .signWith(secretKey, SignatureAlgorithm.HS256) // 비밀키로 서명
                .compact(); // 토큰 생성
    }

    /**
     * 토큰을 파싱하여 모든 클레임(정보)을 추출
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey) // 서명 키 설정
                .build()
                .parseClaimsJws(token) // 토큰 검증 및 파싱
                .getBody();
    }

    /**
     * 특정 클레임 하나를 추출하는 범용 메서드
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
}
