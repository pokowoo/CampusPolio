package com.campuspolio.domain.portfolio.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "포트폴리오 삭제 응답")
public record PortfolioDeleteResponse(

        @Schema(
                description = "응답 메시지",
                example = "포트폴리오 삭제 완료"
        )
        String message

) {
}