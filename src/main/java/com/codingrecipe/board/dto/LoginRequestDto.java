package com.codingrecipe.board.dto;

import lombok.Getter;
import lombok.Setter;

// 로그인 API 요청 시, 사용자의 아이디와 비밀번호를 담아 보내는 DTO
@Getter
@Setter
public class LoginRequestDto {
    private String userId;
    private String password;
}