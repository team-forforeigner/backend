package com.codingrecipe.tip.exception;

import com.codingrecipe.tip.domain.TipCategory;
import com.codingrecipe.tip.dto.ApiResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    /**
     * 1. Enum 변환 실패 (잘못된 카테고리 등)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) cause;
            if (ife.getTargetType() != null && ife.getTargetType().isEnum()) {
                String allowedValues = Arrays.toString(TipCategory.values());
                return ResponseEntity.badRequest()
                        .body(ApiResponse.fail(
                                HttpStatus.BAD_REQUEST.value(),
                                "잘못된 카테고리 값입니다. 허용 값: " + allowedValues
                        ));
            }
        }
        // 그 외 메시지 변환 오류
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(
                        HttpStatus.BAD_REQUEST.value(),
                        "요청 데이터 형식이 올바르지 않습니다."
                ));
    }

    /**
     * 2. @Valid 유효성 검사 실패
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .findFirst()
                .map(err -> err.getDefaultMessage())
                .orElse("유효하지 않은 요청입니다.");

        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(
                        HttpStatus.BAD_REQUEST.value(),
                        errorMessage
                ));
    }

    /**
     * 3. 그 외 모든 예외 (서버 오류)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGenericException(Exception ex) {
        ex.printStackTrace(); // 서버 로그 확인용
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "서버 오류가 발생했습니다."
                ));
    }

}
