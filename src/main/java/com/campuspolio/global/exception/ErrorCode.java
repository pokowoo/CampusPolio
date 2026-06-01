package com.campuspolio.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // ======================
    // AUTH
    // ======================

    INVALID_GOOGLE_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "유효하지 않은 Google 토큰입니다."
    ),

    GOOGLE_EMAIL_NOT_FOUND(
            HttpStatus.BAD_REQUEST,
            "Google 계정에서 이메일을 가져올 수 없습니다."
    ),

    UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            "로그인이 필요합니다."
    ),

    // ======================
    // USER
    // ======================

    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "사용자를 찾을 수 없습니다."
    ),

    // ======================
    // EMAIL
    // ======================

    EMAIL_NOT_UNIVERSITY_DOMAIN(
            HttpStatus.BAD_REQUEST,
            "대학 이메일(.ac.kr)만 인증할 수 있습니다."
    ),

    EMAIL_VERIFICATION_NOT_FOUND(
            HttpStatus.BAD_REQUEST,
            "인증번호 발송 내역이 없습니다."
    ),

    EMAIL_VERIFICATION_EXPIRED(
            HttpStatus.BAD_REQUEST,
            "인증번호가 만료되었습니다."
    ),

    EMAIL_VERIFICATION_CODE_MISMATCH(
            HttpStatus.BAD_REQUEST,
            "인증번호가 일치하지 않습니다."
    ),

    EMAIL_ALREADY_VERIFIED(
            HttpStatus.BAD_REQUEST,
            "이미 대학 인증이 완료되었습니다."
    ),

    // ======================
    // PROFILE
    // ======================

    PROFILE_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "이미 프로필이 존재합니다."
    ),

    PROFILE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "프로필을 찾을 수 없습니다."
    ),

    // ======================
    // PROJECT
    // ======================

    PROJECT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "프로젝트를 찾을 수 없습니다."
    ),

    PROJECT_FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "프로젝트 권한이 없습니다."
    ),

    PROJECT_TITLE_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "프로젝트 제목은 필수입니다."
    ),

    PROJECT_CONTENT_TOO_SHORT(
            HttpStatus.BAD_REQUEST,
            "프로젝트 내용은 최소 30자 이상이어야 합니다."
    ),

    PROJECT_THUMBNAIL_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "프로젝트 썸네일은 필수입니다."
    ),

    PROJECT_ALREADY_DELETED(
            HttpStatus.BAD_REQUEST,
            "이미 삭제된 프로젝트입니다."
    ),

    PROJECT_NOT_PUBLISHED(
            HttpStatus.BAD_REQUEST,
            "프로젝트 발행 조건을 만족하지 않습니다."
    ),

    // ======================
    // FILE
    // ======================

    INVALID_FILE_TYPE(
            HttpStatus.BAD_REQUEST,
            "허용되지 않는 파일 형식입니다."
    ),

    FILE_SIZE_EXCEEDED(
            HttpStatus.BAD_REQUEST,
            "파일 크기 제한을 초과했습니다."
    ),
    PROJECT_PUBLISH_FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "대학 인증 사용자만 프로젝트를 발행할 수 있습니다."
    ),

    // ======================
    // COMMON
    // ======================

    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "서버 오류가 발생했습니다."
    );

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}