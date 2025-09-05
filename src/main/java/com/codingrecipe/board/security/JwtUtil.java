package com.codingrecipe.board.security;

import com.codingrecipe.board.domain.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.function.Function;
import java.util.stream.Collectors;

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
     * 토큰에서 사용자 이메일을 추출
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
     * Member 엔티티의 정보를 받아 JWT 토큰을 생성
     * 토큰에는 id, nickname, role 정보가 추가로 포함
     */
    public String generateToken(Member member) {
        Claims claims = Jwts.claims();
        claims.put("id", member.getId());
        claims.put("nickname", member.getNickname());
        claims.put("role", member.getRoleKey()); // "ROLE_USER" 또는 "ROLE_ADMIN"

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(member.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact(); // 토큰 생성
    }

    /**
     * 토큰에서 UserPrincipal 객체를 파싱하여 반환합
     */
    public UserPrincipal parseToken(String token) {
        Claims claims = extractAllClaims(token);

        Long id = claims.get("id", Long.class);
        String email = claims.getSubject();
        String nickname = claims.get("nickname", String.class);

        String roleStr = claims.get("role", String.class);
        Collection<? extends GrantedAuthority> authorities = Arrays.stream(roleStr.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new UserPrincipal(id, email, nickname, authorities);
    }

    /**
     * 토큰을 파싱하여 모든 정보를 추출
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

