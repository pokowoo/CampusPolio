package com.campuspolio.domain.portfolio.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "내 포트폴리오 목록 항목 응답")
public record MyPortfolioResponse(

        @Schema(description = "포트폴리오 ID", example = "1")
        Long portfolioId,

        @Schema(description = "포트폴리오 제목", example = "AI 프로젝트 모음")
        String title,

        @Schema(description = "포트폴리오 slug", example = "ai-projects")
        String slug,

        @Schema(description = "포트폴리오 설명", example = "AI 관련 프로젝트")
        String description,

        @Schema(description = "썸네일 URL", example = "https://s3.amazonaws.com/xxx.png")
        String thumbnailUrl,

        @Schema(description = "공개 여부", example = "false")
        boolean isPublic,

        @Schema(description = "포함된 프로젝트 수", example = "5")
        long projectCount,

        @Schema(description = "생성일")
        LocalDateTime createdAt,

        @Schema(description = "수정일")
        LocalDateTime updatedAt

) {
}