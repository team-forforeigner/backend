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
        this.statusCode = status.value();
        this.message = message;
        this.result = result;
    }

    // 실패 응답을 위한 생성자
    private ApiResponseDto(ErrorCode errorCode) {
        this.statusCode = errorCode.getStatus().value();
        this.message = errorCode.getMessage();
        this.result = null;
    }

    // JwtFilter에서 사용할 동적 에러 메시지를 위한 private 생성자
    private ApiResponseDto(ErrorCode errorCode, String message) {
        this.statusCode = errorCode.getStatus().value();
        this.message = message;
        this.result = null;
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

    /**
     * 컴파일 에러 해결을 위해 동적 에러 메시지를 받는 error 팩토리 메서드를 추가합니다.
     */
    public static <T> ApiResponseDto<T> error(ErrorCode errorCode, String message) {
        return new ApiResponseDto<>(errorCode, message);
    }
}
