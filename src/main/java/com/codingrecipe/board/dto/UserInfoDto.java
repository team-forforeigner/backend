package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.Member;
import lombok.Getter;

@Getter
public class UserInfoDto {
    private String name;
    private String email;
    private String userId;

    // Member 엔티티를 받아서 DTO를 생성하는 생성자
    public UserInfoDto(Member member) {
        this.name = member.getName();
        this.email = member.getEmail();
        this.userId = member.getUserId();
    }
}