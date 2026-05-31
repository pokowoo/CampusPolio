package com.campuspolio.domain.portfolio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "포트폴리오 정보 수정 요청")
public record PortfolioUpdateRequest(

        @Schema(
                description = "포트폴리오 제목. null이면 기존 값 유지",
                example = "수정된 공모전 포트폴리오"
        )
        @Size(max = 100, message = "포트폴리오 제목은 100자 이하로 입력해주세요.")
        String title,

        @Schema(
                description = "포트폴리오 설명. null이면 기존 값 유지",
                example = "공모전 프로젝트들을 모아둔 포트폴리오입니다."
        )
        @Size(max = 500, message = "포트폴리오 설명은 500자 이하로 입력해주세요.")
        String description,

        @Schema(
                description = "포트폴리오 썸네일 URL. null이면 기존 값 유지",
                example = "https://s3.amazonaws.com/portfolio-thumbnail.png"
        )
        @Size(max = 500, message = "썸네일 URL은 500자 이하로 입력해주세요.")
        String thumbnailUrl

) {
}