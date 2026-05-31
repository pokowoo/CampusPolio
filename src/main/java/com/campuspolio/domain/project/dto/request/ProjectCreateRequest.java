package com.campuspolio.domain.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectCreateRequest(

        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 255, message = "제목은 255자 이하입니다.")
        String title,

        @Size(max = 500, message = "설명은 500자 이하입니다.")
        String description
) {
}