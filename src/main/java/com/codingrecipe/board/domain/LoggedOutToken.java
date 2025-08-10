package com.codingrecipe.board.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 로그아웃 처리된 토큰을 저장하는 엔티티 (DB 기반의 JWT 블랙리스트)
 */
@Entity
@Getter
@NoArgsConstructor
@Table(name = "logged_out_token")
public class LoggedOutToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // JWT를 그대로 저장, 유니크 제약 조건 설정
    @Column(nullable = false, unique = true, length = 1024)
    private String token;

    // 토큰의 만료 시간을 저장. 이 시간이 지나면 DB에서 삭제 가능
    @Column(nullable = false)
    private LocalDateTime expiryAt;

    public LoggedOutToken(String token, LocalDateTime expiryAt) {
        this.token = token;
        this.expiryAt = expiryAt;
    }
}
