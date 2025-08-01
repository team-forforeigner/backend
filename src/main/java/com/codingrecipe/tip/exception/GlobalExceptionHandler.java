package com.codingrecipe.tip.exception;

import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
/**
 * 설명 : 전역 예외 처리 핸들러 클래스입니다.
*/

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

}
