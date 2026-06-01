package com.campuspolio.domain.portfolio.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "포트폴리오 정보 수정 응답")
public record PortfolioUpdateResponse(

        @Schema(
                description = "포트폴리오 ID",
                example = "1"
        )
        Long portfolioId,

        @Schema(
                description = "수정일",
                example = "2026-05-31T17:30:00"
        )
        LocalDateTime updatedAt,

        @Schema(
                description = "응답 메시지",
                example = "포트폴리오 수정 완료"
        )
        String message

) {
}