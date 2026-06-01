package com.campuspolio.domain.portfolio.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "포트폴리오 프로젝트 순서 변경 응답")
public record PortfolioOrderUpdateResponse(

        @Schema(description = "응답 메시지", example = "프로젝트 순서 변경 완료")
        String message

) {
}