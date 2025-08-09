// 로그인 API 요청에 사용되는 데이터 전송 객체(DTO)
package com.codingrecipe.board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {
    private String email;    // 사용자 이메일
    private String password; // 사용자 비밀번호
}
