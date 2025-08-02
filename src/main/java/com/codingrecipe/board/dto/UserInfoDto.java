package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.Member;
import lombok.Getter;

@Getter
public class UserInfoDto {
    private String nickname;
    private String email;

    public UserInfoDto(Member member) {
        this.nickname = member.getNickname();
        this.email = member.getEmail();
    }
}