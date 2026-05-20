package com.campuspolio.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 사용자 정보 응답")
public record UserMeResponse(

        @Schema(description = "사용자 ID", example = "1")
        Long id,

        @Schema(description = "사용자 이메일", example = "user@korea.ac.kr")
        String email,

        @Schema(description = "대학 이메일 도메인 여부", example = "true")
        boolean isDomainValid,

        @Schema(description = "학교 이메일 인증 완료 여부", example = "true")
        boolean isVerified

) {
}