package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.domain.MemberStatus;
import com.codingrecipe.board.domain.Role;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MemberAdminResponseDto {
    private final Long id;
    private final String email;
    private final String nickname;
    private final Role role;
    private final MemberStatus status;
    private final LocalDateTime suspendedUntil;
    private final boolean emailVerified;
    private final int level;
    private final String nationality;
    private final LocalDateTime createdAt;
    private final LocalDateTime lastLoginAt;

    public MemberAdminResponseDto(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.nickname = member.getNickname();
        this.role = member.getRole();
        this.status = member.isSuspended() ? MemberStatus.SUSPENDED : MemberStatus.ACTIVE;
        this.suspendedUntil = member.getSuspendedUntil();
        this.emailVerified = member.isEmailVerified();
        this.level = member.getLevel();
        this.nationality = member.getNationality();
        this.createdAt = member.getCreatedTime();
        this.lastLoginAt = member.getLastLoginAt();
    }
}

