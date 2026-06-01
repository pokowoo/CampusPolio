package com.campuspolio.domain.portfolio.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "포트폴리오 상세 프로젝트 항목 응답")
public record PortfolioDetailProjectResponse(

        @Schema(description = "프로젝트 ID", example = "1")
        Long projectId,

        @Schema(description = "프로젝트 제목", example = "AI 추천 시스템")
        String title,

        @Schema(description = "프로젝트 설명", example = "사용자 행동 기반 추천")
        String description,

        @Schema(description = "썸네일 URL", example = "https://s3.amazonaws.com/project.png")
        String thumbnailUrl,

        @Schema(description = "정렬 순서", example = "0")
        int displayOrder

) {
}