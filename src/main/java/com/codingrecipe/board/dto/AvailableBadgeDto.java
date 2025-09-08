package com.codingrecipe.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 사용자가 선택 가능한 배지(캐릭터) 정보를 담는 DTO
@Getter
@AllArgsConstructor
public class AvailableBadgeDto {
    private final String name; // 캐릭터 이름 (예: "아기 까치")
    private final String imageUrl; // 캐릭터 이미지 URL
}
