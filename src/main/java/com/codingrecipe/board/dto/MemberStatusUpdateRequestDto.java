package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.MemberStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberStatusUpdateRequestDto {
    private MemberStatus status;
}