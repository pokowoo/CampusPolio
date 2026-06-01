package com.campuspolio.domain.portfolio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "포트폴리오 생성 요청")
public record PortfolioCreateRequest(

        @Schema(
                description = "포트폴리오 제목",
                example = "공모전 포트폴리오"
        )
        @NotBlank(message = "포트폴리오 제목은 필수입니다.")
        @Size(max = 100, message = "포트폴리오 제목은 100자 이하로 입력해주세요.")
        String title

) {
}