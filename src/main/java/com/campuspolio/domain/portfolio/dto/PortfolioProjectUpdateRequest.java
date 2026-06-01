package com.campuspolio.domain.portfolio.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "포트폴리오 프로젝트 추가/제거 요청")
public record PortfolioProjectUpdateRequest(

        @Schema(
                description = "추가할 프로젝트 ID 목록",
                example = "[1, 2]"
        )
        List<Long> add,

        @Schema(
                description = "제거할 프로젝트 ID 목록",
                example = "[3]"
        )
        List<Long> remove

) {
}