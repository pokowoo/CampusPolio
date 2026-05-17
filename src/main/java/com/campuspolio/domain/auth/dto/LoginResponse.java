package com.campuspolio.domain.auth.dto;

public record LoginResponse(
        Long id,
        String email,
        boolean isDomainValid,
        boolean isVerified
) {
}