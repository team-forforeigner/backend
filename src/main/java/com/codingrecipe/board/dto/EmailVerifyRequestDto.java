package com.codingrecipe.board.dto;

import lombok.Getter;
import lombok.Setter;

// 이메일과 인증번호를 서버로 보내 이메일 소유권을 확인할 때 사용되는 DTO.
@Getter
@Setter
public class EmailVerifyRequestDto {
    private String email;
    private String code;
}