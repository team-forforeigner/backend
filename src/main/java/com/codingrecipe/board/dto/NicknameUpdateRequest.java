// 닉네임 변경 요청을 위한 데이터 전송 객체(DTO)
package com.codingrecipe.board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NicknameUpdateRequest {
    private String nickname; // 변경할 새로운 닉네임
}
