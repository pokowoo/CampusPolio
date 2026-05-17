package com.campuspolio.domain.auth.controller;

import com.campuspolio.domain.auth.dto.EmailAuthResponse;
import com.campuspolio.domain.auth.dto.EmailSendRequest;
import com.campuspolio.domain.auth.dto.EmailVerifyRequest;
import com.campuspolio.domain.auth.service.EmailAuthService;
import com.campuspolio.global.response.ApiResponse;
import com.campuspolio.global.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/email")
public class EmailAuthController {

    private final EmailAuthService emailAuthService;

    @PostMapping("/send")
    public ApiResponse<EmailAuthResponse> send(
            @AuthenticatedUser Long userId,
            @Valid @RequestBody EmailSendRequest request
    ) {
        EmailAuthResponse response = emailAuthService.sendCode(userId, request);

        return ApiResponse.success(response);
    }

    @PostMapping("/verify")
    public ApiResponse<EmailAuthResponse> verify(
            @AuthenticatedUser Long userId,
            @Valid @RequestBody EmailVerifyRequest request
    ) {
        EmailAuthResponse response = emailAuthService.verifyCode(userId, request);

        return ApiResponse.success(response);
    }

    @PostMapping("/resend")
    public ApiResponse<EmailAuthResponse> resend(
            @AuthenticatedUser Long userId,
            @Valid @RequestBody EmailSendRequest request
    ) {
        EmailAuthResponse response = emailAuthService.sendCode(userId, request);

        return ApiResponse.success(response);
    }
}