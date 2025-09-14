package com.codingrecipe.board.event;

import com.codingrecipe.board.domain.Member;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TempPasswordEvent {
    private final Member member;
    private final String tempPassword;
}
