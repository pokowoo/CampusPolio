package com.campuspolio.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로필 생성 응답")
public record ProfileCreateResponse(

        @Schema(description = "프로필 ID", example = "1")
        Long profileId,

        @Schema(description = "응답 메시지", example = "프로필 생성 완료")
        String message

) {
}