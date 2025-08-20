package com.codingrecipe.board.dto;

import com.codingrecipe.board.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiResponseDto<T> {
    private final int statusCode;
    private final String message;
    private final T result;

    // 성공 응답을 위한 생성자
    private ApiResponseDto(HttpStatus status, String message, T result) {
        this.statusCode = status.value(); // 200, 201 등
        this.message = message;
        this.result = result;
    }

    // 실패 응답을 위한 생성자
    private ApiResponseDto(ErrorCode errorCode) {
        this.statusCode = errorCode.getStatus().value(); // 400, 404 등
        this.message = errorCode.getMessage();
        this.result = null; // 실패 시 result는 null
    }

    // 성공 시 사용할 static 팩토리 메서드 (데이터 포함)
    public static <T> ApiResponseDto<T> success(T result) {
        return new ApiResponseDto<>(HttpStatus.OK, "성공적으로 처리되었습니다.", result);
    }

    // 성공 시 사용할 static 팩토리 메서드 (데이터 미포함, 메시지만)
    public static <T> ApiResponseDto<T> success(String message) {
        return new ApiResponseDto<>(HttpStatus.OK, message, null);
    }

    // 실패 시 사용할 static 팩토리 메서드
    public static <T> ApiResponseDto<T> error(ErrorCode errorCode) {
        return new ApiResponseDto<>(errorCode);
    }
}