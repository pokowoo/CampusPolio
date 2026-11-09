
package com.campuspolio.domain.auth.dto;

public record AuthTokenResult(
        LoginResponse loginResponse,
        String accessToken,
        String refreshToken
) {
}
