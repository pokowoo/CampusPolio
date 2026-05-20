package com.campuspolio.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {


    INVALID_GOOGLE_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 Google 토큰입니다."),
    GOOGLE_EMAIL_NOT_FOUND(HttpStatus.BAD_REQUEST, "Google 계정에서 이메일을 가져올 수 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    EMAIL_NOT_UNIVERSITY_DOMAIN(HttpStatus.BAD_REQUEST, "대학 이메일(.ac.kr)만 인증할 수 있습니다."),
    EMAIL_NOT_MATCHED(HttpStatus.BAD_REQUEST, "로그인한 사용자 이메일과 인증 요청 이메일이 일치하지 않습니다."),
    EMAIL_VERIFICATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "인증번호 발송 내역이 없습니다."),
    EMAIL_VERIFICATION_EXPIRED(HttpStatus.BAD_REQUEST, "인증번호가 만료되었습니다."),
    EMAIL_VERIFICATION_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않습니다."),
    EMAIL_ALREADY_VERIFIED(HttpStatus.BAD_REQUEST, "이미 이메일 인증이 완료되었습니다."),

    PROFILE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 프로필이 존재합니다."),
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "프로필을 찾을 수 없습니다."),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");


    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}