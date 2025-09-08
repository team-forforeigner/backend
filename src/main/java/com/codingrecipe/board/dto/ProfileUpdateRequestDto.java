package com.codingrecipe.board.dto;

import lombok.Getter;
import lombok.Setter;

// 클라이언트에서 JSON 형식으로 받을 요청 DTO
@Getter
@Setter
public class ProfileUpdateRequestDto {
    private String nickname;
    private String selectedTitle; // 사용자가 선택한 칭호
    private String selectedBadge; // 사용자가 선택한 캐릭터 이미지 URL
}
