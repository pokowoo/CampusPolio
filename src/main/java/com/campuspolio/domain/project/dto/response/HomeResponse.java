package com.campuspolio.domain.project.dto.response;

import java.util.List;

public record HomeResponse(

        List<HomeProjectResponse> popularProjects,

        List<HomeCategoryResponse> categories

) {
}