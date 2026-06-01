package com.campuspolio.domain.project.dto.response;

import java.util.List;

public record HomeCategoryResponse(

        String tag,

        List<HomeProjectResponse> projects

) {
}