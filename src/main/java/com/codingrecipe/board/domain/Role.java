// 사용자의 권한(일반 사용자, 관리자)을 정의
package com.codingrecipe.board.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    USER("ROLE_USER", "일반 사용자"), // 일반 사용자 권한
    ADMIN("ROLE_ADMIN", "관리자");   // 관리자 권한

    private final String key;   // Spring Security에서 사용하는 권한 키 (예: "ROLE_USER")
    private final String title; // 사용자에게 보여줄 권한 이름 (예: "일반 사용자")
}
