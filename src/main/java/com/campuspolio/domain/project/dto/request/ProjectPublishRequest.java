package com.campuspolio.domain.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProjectPublishRequest(

        @NotBlank(message = "제목은 필수입니다.")
        @Size(
                max = 255,
                message = "제목은 255자 이하로 입력해주세요."
        )
        String title,

        @Size(
                max = 1000,
                message = "설명은 1000자 이하로 입력해주세요."
        )
        String description,

        @NotBlank(message = "내용은 필수입니다.")
        @Size(
                max = 50000,
                message = "내용은 50000자 이하로 입력해주세요."
        )
        String content,

        @NotNull(message = "태그 목록은 필수입니다.")
        @Size(
                max = 10,
                message = "태그는 최대 10개까지 가능합니다."
        )
        List<
                @Size(
                        max = 10,
                        message = "태그는 10자 이하만 가능합니다."
                )
                        String
                > tags

) {
}