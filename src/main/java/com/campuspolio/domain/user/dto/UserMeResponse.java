package com.campuspolio.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 사용자 정보 응답")
public record UserMeResponse(

        @Schema(description = "사용자 ID", example = "1")
        Long id,

        @Schema(description = "구글 계정 이메일", example = "user@gmail.com")
        String email,

        @Schema(description = "대학 인증 완료 여부", example = "true")
        boolean universityVerified

) {
}