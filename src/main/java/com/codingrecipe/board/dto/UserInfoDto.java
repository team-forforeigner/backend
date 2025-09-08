package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.Member;
import lombok.Getter;

@Getter
public class UserInfoDto {
    private final Long id; // 사용자 ID 필드를 추가합니다.
    private final String nickname;
    private final String email;

    /**
     * Member 엔티티 객체로부터 DTO를 생성하는 기존 생성자
     */
    public UserInfoDto(Member member) {
        this.id = member.getId();
        this.nickname = member.getNickname();
        this.email = member.getEmail();
    }

    /**
     * ID, 이메일, 닉네임 개별 필드로부터 DTO를 생성하는 새로운 생성자
     */
    public UserInfoDto(Long id, String email, String nickname) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
    }
}
