package com.codingrecipe.board.dto;

import lombok.Getter;
import lombok.Setter;

// 로그인 API 요청 시, 사용자의 이메일과 비밀번호를 담아 보내는 DTO (수정됨)
@Getter
@Setter
public class LoginRequestDto {
    private String email; // 변경: userId -> email
    private String password;
}