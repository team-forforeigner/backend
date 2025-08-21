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
@Table(name = "member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nickname;

    private String password;
    private String nationality;

    @Builder.Default
    @Column(nullable = false)
    private boolean emailVerified = false;

    private String provider;
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MemberStatus status = MemberStatus.ACTIVE;

    @Builder.Default
    private int level = 1;
    @Builder.Default
    private int experience = 0;
    @Builder.Default
    private int playCount = 0;
    @Builder.Default
    private int quizSetCount = 0;

    private String title;
    private String badge;
    private String profileImageUrl;
    private String backgroundImageUrl;

    @Builder.Default
    @Column(nullable = false)
    private boolean hintEnabled = true;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("member-attempt")
    @Builder.Default
    private List<QuizAttempt> quizAttempts = new ArrayList<>();

    private LocalDate birth;
    private String visa;
    private String phone;
    private String korName;

    public Member update(String nickname, String picture) {
        this.nickname = nickname;
        this.profileImageUrl = picture;
        return this;
    }

    public String getRoleKey() {
        return this.role.getKey();
    }
}