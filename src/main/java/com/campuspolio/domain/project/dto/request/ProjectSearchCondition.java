package com.campuspolio.domain.project.dto.request;

import com.campuspolio.domain.project.entity.ProjectFilterType;

import java.util.List;

public record ProjectSearchCondition(

        String keyword,

        List<String> tags,

        int page,

        int size,

        ProjectFilterType filterType

) {
}