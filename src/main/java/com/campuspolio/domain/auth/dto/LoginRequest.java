package com.campuspolio.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Google accessToken은 필수입니다.")
        String accessToken
) {
}