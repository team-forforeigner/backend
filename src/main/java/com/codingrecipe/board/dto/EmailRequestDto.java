package com.codingrecipe.board.dto;

import lombok.Getter;
import lombok.Setter;

// 이메일 인증번호 발송과 같이, 이메일 주소만 필요한 API 요청에 사용되는 데이터 전송 객체(DTO).
@Getter
@Setter
public class EmailRequestDto {
    private String email;
}