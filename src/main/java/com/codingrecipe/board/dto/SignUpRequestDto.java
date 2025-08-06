// 회원가입 요청 데이터를 담는 데이터 전송 객체(DTO)
package com.codingrecipe.board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequestDto {
    private String email;         // 사용자 이메일
    private String password;      // 사용자 비밀번호
    private String passwordCheck; // 비밀번호 확인
    private String nationality;   // 국적
}
