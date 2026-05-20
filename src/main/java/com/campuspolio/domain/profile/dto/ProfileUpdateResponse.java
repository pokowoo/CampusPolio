package com.campuspolio.domain.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "프로필 수정 응답")
public record ProfileUpdateResponse(

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "수정 시각", example = "2026-05-05T14:00:00")
        LocalDateTime updatedAt,

        @Schema(description = "응답 메시지", example = "프로필 수정 완료")
        String message

) {
}