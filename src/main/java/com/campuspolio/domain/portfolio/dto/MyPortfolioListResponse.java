package com.campuspolio.domain.portfolio.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "내 포트폴리오 목록 조회 응답")
public record MyPortfolioListResponse(

        @Schema(description = "포트폴리오 목록")
        List<MyPortfolioResponse> content,

        @Schema(description = "현재 페이지", example = "0")
        int page,

        @Schema(description = "페이지 크기", example = "6")
        int size,

        @Schema(description = "전체 데이터 수", example = "10")
        long totalElements,

        @Schema(description = "전체 페이지 수", example = "2")
        int totalPages

) {
}