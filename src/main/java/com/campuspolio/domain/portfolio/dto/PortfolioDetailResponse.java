package com.campuspolio.domain.portfolio.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "포트폴리오 상세 조회 응답")
public record PortfolioDetailResponse(

        @Schema(description = "포트폴리오 ID", example = "1")
        Long portfolioId,

        @Schema(description = "포트폴리오 제목", example = "팀 포트폴리오")
        String title,

        @Schema(description = "포트폴리오 slug", example = "team-portfolio")
        String slug,

        @Schema(description = "포트폴리오 설명", example = "팀 프로젝트 모음")
        String description,

        @Schema(description = "썸네일 URL", example = "https://s3.amazonaws.com/portfolio.png")
        String thumbnailUrl,

        @Schema(description = "공개 여부", example = "true")
        boolean isPublic,

        @Schema(description = "포트폴리오에 포함된 프로젝트 목록")
        List<PortfolioDetailProjectResponse> projects

) {
}