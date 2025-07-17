package com.codingrecipe.board.dto;

import lombok.Getter;
import lombok.Setter;

// 비밀번호 재설정(찾기) API 요청 시, 본인 확인을 위해 아이디, 이름, 이메일을 담아 보내는 DTO
@Getter
@Setter
public class ResetPasswordRequestDto {
    private String userId;
    private String name;
    private String email;
}