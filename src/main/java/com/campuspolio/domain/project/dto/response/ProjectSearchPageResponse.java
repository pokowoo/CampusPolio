package com.campuspolio.domain.project.dto.response;

import java.util.List;

public record ProjectSearchPageResponse(

        List<ProjectSearchResponse> content,

        int page,

        int size,

        long totalElements,

        int totalPages

) {
}