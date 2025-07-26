package com.codingrecipe.tip.exception;

/**
 * 설명 : 팁을 찾을 수 없을 때 발생하는 커스텀 예외 클래스입니다.
 * RuntimeException을 상속받는 이유 : 클라이언트 요청의 문제고, 시스템이 try-catch로 처리할 필요 없는 비즈니스 예외이기 때문.
 */

public class TipNotFoundException extends RuntimeException {
    public TipNotFoundException(Long id) {
        super("Tip not found with id: " + id);
    }
}
