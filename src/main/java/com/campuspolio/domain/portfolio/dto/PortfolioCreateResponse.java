package com.campuspolio.domain.portfolio.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "포트폴리오 생성 응답")
public record PortfolioCreateResponse(

        @Schema(description = "포트폴리오 ID", example = "3")
        Long portfolioId,

        @Schema(description = "포트폴리오 slug", example = "contest-portfolio")
        String slug

) {
}