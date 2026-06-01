package com.campuspolio.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Google OAuth 로그인 요청")
public record LoginRequest(

        @Schema(
                description = "프론트에서 Google OAuth 로그인 후 받은 Google ID Token",
                example = "eyJhbGciOiJSUzI1NiIsImtpZCI6..."
        )
        @NotBlank(message = "Google idToken은 필수입니다.")
        String idToken

) {
}