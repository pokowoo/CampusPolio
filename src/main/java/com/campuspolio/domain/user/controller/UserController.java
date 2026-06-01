package com.campuspolio.domain.user.controller;

import com.campuspolio.domain.user.dto.UserMeResponse;
import com.campuspolio.domain.user.service.UserService;
import com.campuspolio.global.response.ApiResponse;
import com.campuspolio.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "사용자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(
            summary = "내 정보 조회",
            description = "로그인 세션에 저장된 사용자 ID를 기준으로 내 정보를 조회합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "내 정보 조회 성공"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "인증 실패"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "사용자 없음"
    )
    public ApiResponse<UserMeResponse> getMe(
            @AuthenticatedUser Long userId
    ) {
        UserMeResponse response = userService.getMe(userId);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "회원 탈퇴",
            description = "로그인 세션에 저장된 사용자 ID를 기준으로 회원을 탈퇴 처리하고 세션을 무효화합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "204",
            description = "회원 탈퇴 성공"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "인증 실패"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "사용자 없음"
    )
    public void withdraw(
            @AuthenticatedUser Long userId,
            HttpSession session
    ) {
        userService.withdraw(userId);
        session.invalidate();
    }
}