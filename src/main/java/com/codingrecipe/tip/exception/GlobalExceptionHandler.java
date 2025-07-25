package com.codingrecipe.tip.exception;

import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
/**
 * 설명 : 전역 예외 처리 핸들러 클래스입니다.
*/

public class GlobalExceptionHandler {

    // 409 Conflict 상태 코드를 반환
    @ExceptionHandler(TipAlreadyExistsException.class)
    public ResponseEntity<String> handlewTipAlreadyExistsException(TipAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}
