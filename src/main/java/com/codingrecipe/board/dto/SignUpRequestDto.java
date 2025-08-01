package com.codingrecipe.board.dto;

import lombok.Getter;
import lombok.Setter;

// 회원가입 API 요청 시, 가입에 필요한 사용자 정보를 담아 보내는 DTO
@Getter
@Setter
public class SignUpRequestDto {
    private String email;
    private String password;
    private String passwordCheck;
    private String nationality;
}