package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.domain.MemberStatus;
import com.codingrecipe.board.domain.Role;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MemberAdminResponseDto {
    private Long id;
    private String email;
    private String nickname;
    private Role role;
    private MemberStatus status;
    private boolean emailVerified;
    private int level;

    private String nationality;
    private LocalDateTime createdAt; // 가입일
    private LocalDateTime lastLoginAt; // 마지막 접속일

    public MemberAdminResponseDto(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.nickname = member.getNickname();
        this.role = member.getRole();
        this.status = member.getStatus();
        this.emailVerified = member.isEmailVerified();
        this.level = member.getLevel();

        this.nationality = member.getNationality();
        this.createdAt = member.getCreatedTime(); // BaseEntity로부터 상속받은 createdTime 사용
        this.lastLoginAt = member.getLastLoginAt();
    }
}