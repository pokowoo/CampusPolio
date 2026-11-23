package com.campuspolio.global.security;

import java.lang.annotation.*;

/**
 * JWT 인증 정보에서 로그인한 사용자 ID(Long)를 주입한다.
 *
 * required = true(기본값): 비로그인이면 401(UNAUTHORIZED)
 * required = false: 비로그인이면 null 주입 (공개 조회 API에서 로그인 여부만 확인할 때 사용)
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthenticatedUser {

    boolean required() default true;
}
