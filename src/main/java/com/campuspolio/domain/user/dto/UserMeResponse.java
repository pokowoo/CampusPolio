package com.campuspolio.domain.user.dto;

public record UserMeResponse(
        Long id,
        String email,
        boolean isDomainValid,
        boolean isVerified
) {
}