// 사용자 정보(닉네임, 이메일)를 응답으로 보낼 때 사용하는 DTO
package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.Member;
import lombok.Getter;

@Getter
public class UserInfoDto {
    private String nickname; // 사용자 닉네임
    private String email;    // 사용자 이메일

    public UserInfoDto(Member member) {
        this.nickname = member.getNickname();
        this.email = member.getEmail();
    }
}
