package com.campuspolio.domain.auth.controller;

import com.campuspolio.domain.auth.dto.LoginRequest;
import com.campuspolio.domain.auth.dto.LoginResponse;
import com.campuspolio.domain.auth.service.AuthService;
import com.campuspolio.global.response.ApiResponse;
import com.campuspolio.global.security.SessionConst;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "로그인/로그아웃 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Google OAuth 로그인",
            description = """
                    프론트에서 Google 로그인 후 받은 accessToken을 서버로 전달합니다.
                    서버는 Google 사용자 정보를 검증한 뒤 User를 조회/생성하고 세션에 userId를 저장합니다.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "로그인 성공"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 또는 이메일 정보 없음"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "유효하지 않은 Google 토큰"
    )
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpSession session
    ) {
        LoginResponse response = authService.login(request);

        session.setAttribute(SessionConst.LOGIN_USER_ID, response.id());

        return ApiResponse.success(response);
    }

    @Operation(
            summary = "로그아웃",
            description = "현재 세션을 만료시켜 로그아웃합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "로그아웃 성공"
    )
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpSession session) {
        session.invalidate();

        return ApiResponse.success(null);
    }
}