package com.codingrecipe.tip.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 설명 : API 응답 DTO
 */

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Builder
public class ApiResponse<T> {

    private int statusCode; // HTTP 상태 코드 (200, 201, 400, ...)
    private String message; // 설명 메시지
    private T result;       // 실제 데이터

    public static <T> ApiResponse<T> success(T result, String message, int statusCode) {
        return ApiResponse.<T>builder()
                .statusCode(statusCode)
                .message(message)
                .result(result)
                .build();
    }

    public static <T> ApiResponse<T> fail(int statusCode, String message) {
        return ApiResponse.<T>builder()
                .statusCode(statusCode)
                .message(message)
                .result(null)
                .build();
    }
}