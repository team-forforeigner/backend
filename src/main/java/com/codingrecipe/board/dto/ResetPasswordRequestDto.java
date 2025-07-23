package com.codingrecipe.board.dto;

import lombok.Getter;
import lombok.Setter;

// 비밀번호 재설정(임시 비밀번호 발급) 요청 시, 이메일만 담아 보내는 DTO (수정됨)
@Getter
@Setter
public class ResetPasswordRequestDto {
    private String email;
}