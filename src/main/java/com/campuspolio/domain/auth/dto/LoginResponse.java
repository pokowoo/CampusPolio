package com.campuspolio.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답")
public record LoginResponse(

        @Schema(description = "사용자 ID", example = "1")
        Long id,

        @Schema(description = "사용자 이메일 (Google OAuth 계정 이메일)", example = "user@gmail.com")
        String email,

        @Schema(description = "대학 인증 완료 여부 (ac.kr 인증)", example = "false")
        boolean universityVerified

) {
}