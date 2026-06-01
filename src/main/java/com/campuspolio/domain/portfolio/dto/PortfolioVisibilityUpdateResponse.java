package com.campuspolio.domain.portfolio.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "포트폴리오 공개 여부 변경 응답")
public record PortfolioVisibilityUpdateResponse(

        @Schema(
                description = "포트폴리오 ID",
                example = "1"
        )
        Long portfolioId,

        @Schema(
                description = "공개 여부",
                example = "true"
        )
        boolean isPublic,

        @Schema(
                description = "응답 메시지",
                example = "포트폴리오 공개 상태 변경 완료"
        )
        String message

) {
}