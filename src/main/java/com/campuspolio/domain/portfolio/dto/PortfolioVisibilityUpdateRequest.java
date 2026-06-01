package com.campuspolio.domain.portfolio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "포트폴리오 공개 여부 변경 요청")
public record PortfolioVisibilityUpdateRequest(

        @Schema(
                description = "공개 여부",
                example = "true"
        )
        @JsonProperty("isPublic")
        @NotNull(message = "공개 여부는 필수입니다.")
        Boolean isPublic

) {
}