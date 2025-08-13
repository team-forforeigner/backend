package com.survival.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// 리소스(엔티티)를 찾을 수 없을 때 발생하는 예외 클래스
// HTTP 404 NOT FOUND 상태 코드를 반환
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
