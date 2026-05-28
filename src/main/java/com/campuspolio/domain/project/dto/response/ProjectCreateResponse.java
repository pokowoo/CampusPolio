package com.campuspolio.domain.project.dto.response;

import com.campuspolio.domain.project.entity.ProjectStatus;

public record ProjectCreateResponse(

        Long projectId,

        ProjectStatus status
) {
}