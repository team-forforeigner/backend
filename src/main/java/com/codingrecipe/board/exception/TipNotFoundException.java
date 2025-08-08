package com.codingrecipe.board.exception;

/**
 * 설명 : 팁을 찾을 수 없을 때 발생하는 커스텀 예외 클래스입니다.
 * 클라이언트 요청의 문제고, 시스템이 try-catch로 처리할 필요 없는 비즈니스 예외입니다.
 */

public class TipNotFoundException extends BusinessException {
    public TipNotFoundException(Long id) {
        super("해당 ID의 팁을 찾을 수 없습니다: " + id);
    }
}
