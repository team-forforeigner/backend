package com.codingrecipe.board.dto;

import lombok.Getter;
import lombok.Setter;

// 아이디 찾기 API 요청 시, 사용자의 이름과 이메일을 담아 보내는 DTO
@Getter
@Setter
public class FindIdRequestDto {
    private String name;
    private String email;
}
