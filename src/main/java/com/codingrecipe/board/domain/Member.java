// 회원 정보와 관련된 모든 데이터를 관리
package com.codingrecipe.board.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "member") // 'member' 테이블과 매핑
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 회원 고유 식별자

    @Column(nullable = false, unique = true)
    private String email; // 로그인 ID로 사용되는 이메일

    @Column(nullable = false)
    private String nickname; // 사용자 별명

    private String password; // 일반 로그인 시 사용되는 암호화된 비밀번호

    private String nationality; // 국적

    @Builder.Default // 빌더 패턴 사용 시 기본값(false)으로 설정
    @Column(nullable = false)
    private boolean emailVerified = false; // 이메일 인증 완료 여부

    // --- 소셜 로그인 정보 ---
    private String provider; // 소셜 로그인 제공자 ("google")
    private String providerId; // 소셜 로그인 제공자의 사용자 고유 ID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // 사용자 권한 (USER, ADMIN)

    // --- 퀴즈 관련 정보 ---
    @Builder.Default
    private int level = 1; // 사용자 레벨
    @Builder.Default
    private int experience = 0; // 사용자 경험치
    @Builder.Default
    private int playCount = 0; // 퀴즈 참여 횟수

    // --- [보스전] 신규 필드 추가 ---
    @Builder.Default
    private int quizSetCount = 0; // 푼 퀴즈 세트 수 (보스전 등장 조건용)
    // --------------------------------

    // --- 프로필 꾸미기 정보 ---
    private String title; // 사용자가 설정한 칭호
    private String badge; // 사용자가 획득한 배지
    private String profileImageUrl; // 프로필 이미지 URL
    private String backgroundImageUrl; // 프로필 배경 이미지 URL

    @Builder.Default
    @Column(nullable = false)
    private boolean hintEnabled = true; // 힌트 기능 사용 여부

    // 회원이 삭제되면 연관된 퀴즈 시도 기록도 함께 삭제
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("member-attempt") // 순환 참조 방지를 위한 설정
    @Builder.Default
    private List<QuizAttempt> quizAttempts = new ArrayList<>();

    // --- 개인 정보 ---
    private LocalDate birth; // 생년월일
    private String visa; // 비자 정보
    private String phone; // 전화번호
    private String korName; // 한국어 이름

    // 소셜 로그인 시 닉네임, 프로필 이미지 업데이트
    public Member update(String nickname, String picture) {
        this.nickname = nickname;
        this.profileImageUrl = picture;
        return this;
    }

    // Spring Security에서 사용하는 권한 키("ROLE_USER" 등) 반환
    public String getRoleKey() {
        return this.role.getKey();
    }
}
