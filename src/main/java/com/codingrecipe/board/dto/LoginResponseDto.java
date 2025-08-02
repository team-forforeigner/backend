package com.codingrecipe.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 로그인 성공 시, 서버가 클라이언트에게 JWT 토큰을 담아 응답으로 보내주는 DTO
@Getter
@AllArgsConstructor
public class LoginResponseDto {
    private String token;
}