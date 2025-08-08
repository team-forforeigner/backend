package com.codingrecipe.board.exception;

import com.codingrecipe.board.dto.ApiResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;

/**
 * 설명 : 전역 예외 처리 핸들러 클래스입니다.
*/

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 409 Conflict
    @ExceptionHandler(TipAlreadyExistsException.class)
    public ResponseEntity<String> handleTipAlreadyExistsException(TipAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    // 404 Not Found
    @ExceptionHandler(TipNotFoundException.class)
    public ResponseEntity<String> handleTipNotFoundException(TipNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    // 400 Bad Request
    @ExceptionHandler(InvalidCategoryException.class)
    public ResponseEntity<String> handleInvalidCategoryException(InvalidCategoryException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    // 비즈니스 예외 (모든 서비스 공통)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    // JSON 파싱 실패 (예: enum 변환 불가)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife && ife.getTargetType() != null && ife.getTargetType().isEnum()) {
            String allowedValues = Arrays.toString(ife.getTargetType().getEnumConstants());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(HttpStatus.BAD_REQUEST.value(),
                            "잘못된 값입니다. 허용 값: " + allowedValues));
        }
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(HttpStatus.BAD_REQUEST.value(), "요청 데이터 형식이 올바르지 않습니다."));
    }

    // 그 외 예상 못한 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception ex) {
        // 로그 남기기
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버에서 오류가 발생했습니다."));
    }

}
