package com.campuspolio.domain.project.dto.request;

import jakarta.validation.constraints.Size;

import java.util.List;

public record ProjectUpdateRequest(

        @Size(max = 255)
        String title,

        @Size(max = 1000)
        String description,

        String content,

        String thumbnail,

        List<String> tags

) {
}