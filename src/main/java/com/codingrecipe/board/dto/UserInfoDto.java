package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.Member;
import lombok.Getter;

@Getter
public class UserInfoDto {
    private String nickname; // 변경: name -> nickname
    private String email;

    public UserInfoDto(Member member) {
        // 변경: getName() -> getNickname()
        this.nickname = member.getNickname();
        this.email = member.getEmail();
    }
}