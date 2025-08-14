package com.codingrecipe.board.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
    public ErrorResponse(ErrorCode errorCode, String path) {
        this(
                LocalDateTime.now(),
                errorCode.getStatus().value(),
                errorCode.getStatus().name(),
                errorCode.getMessage(),
                path
        );
    }
}