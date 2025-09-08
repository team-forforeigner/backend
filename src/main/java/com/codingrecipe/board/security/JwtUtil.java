package com.codingrecipe.board.security;

import com.codingrecipe.board.domain.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtil {

    private final Key secretKey;
    private final long expirationMs;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration-ms:86400000}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        log.info("JWT Secret Key가 성공적으로 로드되었습니다");
    }

    /**
     * Member 객체의 정보를 담아 새로운 JWT 토큰을 생성합니다.
     */
    public String generateToken(Member member) {
        Claims claims = Jwts.claims();
        claims.put("id", member.getId());
        claims.put("email", member.getEmail());
        claims.put("nickname", member.getNickname());
        claims.put("role", member.getRoleKey());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 이메일 인증용 토큰을 생성합니다. (Subject에 이메일만 포함)
     */
    public String generateVerificationToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs)) // 유효 기간은 동일하게 설정
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }


    /**
     * 토큰을 파싱하여 사용자 정보를 담은 UserPrincipal 객체를 반환합니다.
     */
    public UserPrincipal parseToken(String token) {
        Claims claims = extractAllClaims(token);
        Long id = ((Number) claims.get("id")).longValue();
        String email = (String) claims.get("email");
        String nickname = (String) claims.get("nickname");
        String role = (String) claims.get("role");

        Collection<? extends GrantedAuthority> authorities =
                Collections.singletonList(new SimpleGrantedAuthority(role));

        return new UserPrincipal(id, email, nickname, authorities);
    }

    /**
     * 이메일 인증용 토큰에서 이메일만 간단히 추출합니다.
     */
    public String getEmailFromVerificationToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }


    /**
     * 토큰의 만료 여부를 확인합니다.
     */
    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * 토큰을 파싱하여 모든 정보를 추출합니다.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 특정 클레임 하나를 추출하는 범용 메서드입니다.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
}

