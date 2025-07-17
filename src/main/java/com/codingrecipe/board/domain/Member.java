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
@Table(name = "member") // ⭐️ 테이블 이름을 명시적으로 지정하는 것이 좋습니다.
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 소셜 로그인의 이름과 자체 가입의 이름을 공통으로 사용

    @Column(nullable = false, unique = true)
    private String email;

    private String picture;

    @Column(unique = true)
    private String userId;

    private String password;

    private String lastName;
    private String firstName;

    @Builder.Default
    @Column(nullable = false)
    private boolean emailVerified = false;

    // private String emailAuthCode; // ⭐️ 더 이상 필요 없으므로 삭제 또는 주석 처리

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

    public Member update(String name, String picture) {
        this.name = name;
        this.picture = picture;
        return this;
    }

    public String getRoleKey() {
        return this.role.getKey();
    }
}