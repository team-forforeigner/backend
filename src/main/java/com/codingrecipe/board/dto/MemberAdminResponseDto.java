package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.domain.Role;
import lombok.Getter;

@Getter
public class MemberAdminResponseDto {
    private Long id;
    private String email;
    private String nickname;
    private Role role;
    private boolean emailVerified;
    private int level;

    public MemberAdminResponseDto(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.nickname = member.getNickname();
        this.role = member.getRole();
        this.emailVerified = member.isEmailVerified();
        this.level = member.getLevel();
    }
}