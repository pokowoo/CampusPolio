package com.campuspolio.domain.auth.controller;

import com.campuspolio.domain.auth.dto.EmailAuthResponse;
import com.campuspolio.domain.auth.dto.EmailSendRequest;
import com.campuspolio.domain.auth.dto.EmailVerifyRequest;
import com.campuspolio.domain.auth.service.EmailAuthService;
import com.campuspolio.global.response.ApiResponse;
import com.campuspolio.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Email Auth", description = "학교 이메일 인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/email")
public class EmailAuthController {

    private final EmailAuthService emailAuthService;

    @Operation(
            summary = "이메일 인증번호 발송",
            description = """
                    로그인한 사용자의 이메일로 인증번호를 발송합니다.
                    현재 구현에서는 실제 SMTP 발송 대신 서버 로그에 인증번호를 출력합니다.
                    대학 이메일(.ac.kr)만 인증할 수 있습니다.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "인증번호 발송 성공"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "이메일 불일치, 대학 이메일 아님, 이미 인증됨"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "로그인이 필요함"
    )
    @PostMapping("/send")
    public ApiResponse<EmailAuthResponse> send(
            @AuthenticatedUser Long userId,
            @Valid @RequestBody EmailSendRequest request
    ) {
        EmailAuthResponse response = emailAuthService.sendCode(userId, request);

        return ApiResponse.success(response);
    }

    @Operation(
            summary = "이메일 인증번호 검증",
            description = """
                    이메일과 6자리 인증번호를 검증합니다.
                    검증 성공 시 User의 verified 값이 true로 변경됩니다.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "이메일 인증 성공"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "인증번호 없음, 만료, 불일치"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "로그인이 필요함"
    )
    @PostMapping("/verify")
    public ApiResponse<EmailAuthResponse> verify(
            @AuthenticatedUser Long userId,
            @Valid @RequestBody EmailVerifyRequest request
    ) {
        EmailAuthResponse response = emailAuthService.verifyCode(userId, request);

        return ApiResponse.success(response);
    }

    @Operation(
            summary = "이메일 인증번호 재발송",
            description = "인증번호를 다시 생성하여 발송합니다. 현재는 서버 로그에 인증번호를 출력합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "인증번호 재발송 성공"
    )
    @PostMapping("/resend")
    public ApiResponse<EmailAuthResponse> resend(
            @AuthenticatedUser Long userId,
            @Valid @RequestBody EmailSendRequest request
    ) {
        EmailAuthResponse response = emailAuthService.sendCode(userId, request);

        return ApiResponse.success(response);
    }
}