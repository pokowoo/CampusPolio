package com.campuspolio.domain.portfolio.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "포트폴리오 프로젝트 추가/제거 응답")
public record PortfolioProjectUpdateResponse(

        @Schema(description = "응답 메시지", example = "포트폴리오 프로젝트 수정 완료")
        String message

) {
}