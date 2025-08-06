package com.codingrecipe.tip.exception;

/**
 * 설명 : 잘못된 카테고리 값이 들어왔을 때 발생하는 커스텀 예외 클래스입니다.
 * 클라이언트 요청의 문제고, 시스템이 try-catch로 처리할 필요 없는 비즈니스 예외입니다.
 */

public class InvalidCategoryException extends BusinessException {
    public InvalidCategoryException(String message) {
        super(message);
    }
}
