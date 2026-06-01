package com.campuspolio.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이메일 인증 응답")
public record EmailAuthResponse(

        @Schema(description = "응답 메시지", example = "인증번호가 발송되었습니다.")
        String message

) {
}