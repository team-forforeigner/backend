// 퀴즈 힌트 설정(활성화/비활성화) 변경 요청을 위한 DTO
package com.codingrecipe.board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HintSettingRequest {
    private boolean enabled; // 힌트 활성화 여부 (true: 활성화, false: 비활성화)
}
