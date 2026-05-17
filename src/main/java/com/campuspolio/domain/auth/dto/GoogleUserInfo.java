package com.campuspolio.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleUserInfo(
        String sub,
        String email,

        @JsonProperty("email_verified")
        Boolean emailVerified,

        String name,
        String picture
) {
}