package com.campuspolio.domain.portfolio.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "내 포트폴리오 목록 응답")
public record MyPortfolioResponse(

        @Schema(description = "포트폴리오 ID", example = "1")
        Long portfolioId,

        @Schema(description = "포트폴리오 제목", example = "AI 프로젝트 모음")
        String title,

        @Schema(description = "포트폴리오 slug", example = "ai-프로젝트-모음")
        String slug,

        @Schema(description = "포트폴리오 설명", example = "AI 관련 프로젝트를 모아둔 포트폴리오입니다.")
        String description,

        @Schema(description = "포트폴리오 썸네일 URL", example = "https://s3.amazonaws.com/thumbnail.png")
        String thumbnailUrl,

        @Schema(description = "공개 여부", example = "true")
        boolean isPublic,

        @Schema(description = "포트폴리오에 포함된 프로젝트 개수", example = "3")
        long projectCount,

        @Schema(description = "포트폴리오에 포함된 프로젝트 ID 목록", example = "[1, 2, 3]")
        List<Long> projectIds,

        @Schema(description = "생성일시", example = "2026-06-01T12:00:00")
        LocalDateTime createdAt,

        @Schema(description = "수정일시", example = "2026-06-01T12:30:00")
        LocalDateTime updatedAt

) {
}