package com.codingrecipe.board.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nickname; // 변경: 'name' -> 'nickname', 초기값은 이메일, 이후 사용자가 변경

    @Column(nullable = false, unique = true)
    private String email; // 회원 식별 및 로그인을 위한 주요 키

    private String picture;

    @Column(unique = true)
    private String userId; // 삭제

    private String password;

    @Builder.Default
    @Column(nullable = false)
    private boolean emailVerified = false;

    private String nationality;
    private LocalDate birth;
    private String visa;
    private String phone;
    private String korName;

    // -- 소셜 로그인 정보 --
    private String provider;
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // 변경: 소셜 로그인 시 닉네임, 사진 업데이트
    public Member update(String nickname, String picture) {
        this.nickname = nickname;
        this.picture = picture;
        return this;
    }

    public String getRoleKey() {
        return this.role.getKey();
    }
}