package com.campuspolio.domain.portfolio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "포트폴리오 프로젝트 순서 변경 요청")
public record PortfolioOrderUpdateRequest(

        @Schema(
                description = "변경할 프로젝트 ID 순서",
                example = "[2, 1, 3]"
        )
        @NotEmpty(message = "프로젝트 순서 배열은 필수입니다.")
        List<Long> projectOrder

) {
}