package com.codingrecipe.board.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 400 Bad Request
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    INVALID_ARGUMENT(HttpStatus.BAD_REQUEST, "잘못된 인자값입니다."),
    ALREADY_LIKED_BOARD(HttpStatus.BAD_REQUEST, "이미 좋아요를 누른 게시글입니다."),
    ALREADY_SCRAPPED_BOARD(HttpStatus.BAD_REQUEST, "이미 스크랩한 게시글입니다."),
    LOGIN_FAILED(HttpStatus.BAD_REQUEST, "이메일 또는 비밀번호가 올바르지 않습니다."), // <<< 추가

    // 401 Unauthorized
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.UNAUTHORIZED, "이메일 인증이 완료되지 않았습니다."), // <<< 추가

    // 403 Forbidden
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // 404 Not Found
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
    BANNER_NOT_FOUND(HttpStatus.NOT_FOUND, "배너를 찾을 수 없습니다."),
    SCRAP_NOT_FOUND(HttpStatus.NOT_FOUND, "스크랩 정보를 찾을 수 없습니다."),
    LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "좋아요 정보를 찾을 수 없습니다."),

    // 409 Conflict
    ALREADY_EXIST_EMAIL(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),

    // 500 Internal Server Error
    S3_FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3 파일 업로드에 실패했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 오류가 발생했습니다.");


    private final HttpStatus status;
    private final String message;
}