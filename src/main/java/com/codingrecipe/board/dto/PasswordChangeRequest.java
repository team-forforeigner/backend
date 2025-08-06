// 비밀번호 변경 요청을 위한 데이터 전송 객체(DTO)
package com.codingrecipe.board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeRequest {
    private String currentPassword;  // 현재 비밀번호
    private String newPassword;      // 새로운 비밀번호
    private String newPasswordCheck; // 새로운 비밀번호 확인
}
