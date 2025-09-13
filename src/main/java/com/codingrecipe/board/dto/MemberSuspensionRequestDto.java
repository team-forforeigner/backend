package com.codingrecipe.board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberSuspensionRequestDto {
    // 정지할 기간(일). 0을 보내면 정지 해제로 간주합니다.
    private int suspensionDays;
}
