package com.codingrecipe.tip.exception;

/**
 * 설명 : 비즈니스 로직에서 발생하는 예외를 처리하기 위한 추상 클래스입니다.
 */

public abstract class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
