package com.campuspolio.domain.auth.controller;

import com.campuspolio.domain.auth.dto.LoginRequest;
import com.campuspolio.domain.auth.dto.LoginResponse;
import com.campuspolio.domain.auth.service.AuthService;
import com.campuspolio.global.response.ApiResponse;
import com.campuspolio.global.security.SessionConst;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpSession session
    ) {
        LoginResponse response = authService.login(request);

        session.setAttribute(SessionConst.LOGIN_USER_ID, response.id());

        return ApiResponse.success(response);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpSession session) {
        session.invalidate();

        return ApiResponse.success(null);
    }
}