package com.campuspolio.domain.profile.controller;

import com.campuspolio.domain.profile.dto.ProfileCreateRequest;
import com.campuspolio.domain.profile.dto.ProfileCreateResponse;
import com.campuspolio.domain.profile.dto.ProfileResponse;
import com.campuspolio.domain.profile.dto.ProfileUpdateRequest;
import com.campuspolio.domain.profile.dto.ProfileUpdateResponse;
import com.campuspolio.domain.profile.service.ProfileService;
import com.campuspolio.global.response.ApiResponse;
import com.campuspolio.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Profile", description = "프로필 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    @Operation(
            summary = "내 프로필 조회",
            description = "로그인 세션에 저장된 사용자 ID를 기준으로 내 프로필을 조회합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "프로필 조회 성공"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "로그인이 필요함"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "사용자 또는 프로필 없음"
    )
    public ApiResponse<ProfileResponse> getMyProfile(
            @AuthenticatedUser Long userId
    ) {
        ProfileResponse response = profileService.getMyProfile(userId);
        return ApiResponse.success(response);
    }

    @PostMapping
    @Operation(
            summary = "프로필 생성",
            description = "현재 로그인한 사용자의 프로필을 생성합니다. 한 사용자당 하나의 프로필만 생성할 수 있습니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "프로필 생성 성공"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 입력값"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "로그인이 필요함"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "이미 프로필 존재"
    )
    public ApiResponse<ProfileCreateResponse> createProfile(
            @AuthenticatedUser Long userId,
            @Valid @RequestBody ProfileCreateRequest request
    ) {
        ProfileCreateResponse response = profileService.createProfile(userId, request);
        return ApiResponse.success(response);
    }

    @PatchMapping
    @Operation(
            summary = "프로필 수정",
            description = "현재 로그인한 사용자의 프로필 정보를 수정합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "프로필 수정 성공"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 입력값"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "로그인이 필요함"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "사용자 또는 프로필 없음"
    )
    public ApiResponse<ProfileUpdateResponse> updateProfile(
            @AuthenticatedUser Long userId,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        ProfileUpdateResponse response = profileService.updateProfile(userId, request);
        return ApiResponse.success(response);
    }
}