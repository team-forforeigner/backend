package com.codingrecipe.tip.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 설명 : API 응답 DTO
 */

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class ApiResponse<T> {

    private boolean success; // 요청 성공 여부
    private T data;          // 성공 데이터
    private ApiError error;  // 실패 정보

    @Data
    @AllArgsConstructor(staticName = "of")
    @NoArgsConstructor
    public static class ApiError {
        private String code;     // 예: TIP_ALREADY_EXISTS
        private String message;  // 예: 이미 등록된 질문입니다.
    }

    // 성공 응답 헬퍼
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.of(true, data, null);
    }

    // 실패 응답 헬퍼
    public static <T> ApiResponse<T> failure(String code, String message) {
        return ApiResponse.of(false, null, ApiError.of(code, message));
    }

}
