package com.codingrecipe.board.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

// 회원가입 API 요청 시, 가입에 필요한 사용자 정보를 담아 보내는 DTO
@Getter
@Setter
public class SignUpRequestDto {
    private String lastName;
    private String firstName;
    private String userId;
    private String password;
    private String nationality;
    private String email;
}