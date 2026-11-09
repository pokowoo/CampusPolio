
package com.campuspolio.domain.auth.controller;

import com.campuspolio.domain.auth.dto.AuthTokenResult;
import com.campuspolio.domain.auth.dto.LoginRequest;
import com.campuspolio.domain.auth.dto.LoginResponse;
import com.campuspolio.domain.auth.service.AuthService;
import com.campuspolio.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@Tag(name = "Auth", description = "로그인/로그아웃 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Operation(
            summary = "Google OAuth 로그인",
            description = "Google ID Token을 검증하고 JWT를 HttpOnly 쿠키로 발급합니다."
    )
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AuthTokenResult result = authService.login(request);

        // Access Token 쿠키
        ResponseCookie accessCookie = ResponseCookie.from(
                        "accessToken", result.accessToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofMillis(accessTokenExpiration))
                .build();

        // Refresh Token 쿠키
        ResponseCookie refreshCookie = ResponseCookie.from(
                        "refreshToken", result.refreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofMillis(refreshTokenExpiration))
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        return ApiResponse.success(result.loginResponse());
    }

    @Operation(
            summary = "로그아웃",
            description = "Refresh Token을 폐기하고 인증 쿠키를 삭제합니다."
    )
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = resolveCookie(request, "refreshToken");

        // DB에 저장된 Refresh Token 폐기
        authService.logout(refreshToken);

        // 브라우저 쿠키 삭제
        response.addHeader("Set-Cookie",
                deleteCookie("accessToken").toString());

        response.addHeader("Set-Cookie",
                deleteCookie("refreshToken").toString());

        return ApiResponse.success(null);
    }

    private String resolveCookie(
            HttpServletRequest request,
            String cookieName
    ) {
        if (request.getCookies() == null) {
            return null;
        }

        for (var cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private ResponseCookie deleteCookie(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();
    }

    @Operation(
            summary = "토큰 재발급",
            description = "Refresh Token을 검증하고 새로운 Access Token과 Refresh Token을 발급합니다."
    )
    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // 기존 Refresh Token 쿠키 추출
        String refreshToken = resolveCookie(request, "refreshToken");

        // 새로운 토큰 발급 및 Rotation
        AuthTokenResult result = authService.refresh(refreshToken);

        // 새로운 Access Token 쿠키
        ResponseCookie accessCookie = ResponseCookie.from(
                        "accessToken", result.accessToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofMillis(accessTokenExpiration))
                .build();

        // 새로운 Refresh Token 쿠키
        ResponseCookie refreshCookie = ResponseCookie.from(
                        "refreshToken", result.refreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofMillis(refreshTokenExpiration))
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        return ApiResponse.success(result.loginResponse());
    }

}
