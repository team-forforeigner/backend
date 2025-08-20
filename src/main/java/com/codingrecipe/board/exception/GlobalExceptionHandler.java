package com.codingrecipe.board.exception;

import com.codingrecipe.board.dto.ApiResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import com.codingrecipe.board.dto.ApiResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;

/**
 * 설명 : 전역 예외 처리 핸들러 클래스입니다.
*/

@Slf4j
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
     * 우리가 직접 정의한 CustomException을 처리하는 핸들러
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleCustomException(CustomException e) {
        log.error("CustomException: {}", e.getErrorCode().getMessage());
        // ErrorResponse 대신 ApiResponseDto.error()를 사용하여 응답을 생성
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ApiResponseDto.error(e.getErrorCode()));
    }

    /**
     * @Valid 어노테이션을 사용한 DTO 유효성 검사 실패 시 발생하는 예외를 처리하는 핸들러
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        // 유효성 검사 실패 시 첫 번째 에러 메시지를 가져옵니다.
        String errorMessage = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        log.error("Validation Exception: {}", errorMessage);

        // INVALID_ARGUMENT ErrorCode를 사용하여 응답을 생성
        return ResponseEntity
                .status(ErrorCode.INVALID_ARGUMENT.getStatus())
                .body(ApiResponseDto.error(ErrorCode.INVALID_ARGUMENT));
    }


    /**
     * 위에서 처리하지 못한 모든 예외를 처리하는 핸들러 (최후의 보루)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleException(Exception e) {
        log.error("Unhandled Exception: {}", e.getMessage(), e); // 스택 트레이스도 함께 로깅
        // INTERNAL_SERVER_ERROR ErrorCode를 사용하여 응답을 생성
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ApiResponseDto.error(ErrorCode.INTERNAL_SERVER_ERROR));
    }


}