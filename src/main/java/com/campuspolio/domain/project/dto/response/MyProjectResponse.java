package com.campuspolio.domain.project.dto.response;

import com.campuspolio.domain.project.entity.ProjectStatus;
import com.campuspolio.domain.project.entity.UserProjectRole;

import java.time.LocalDateTime;

public record MyProjectResponse(
        Long projectId,
        String title,
        String thumbnailUrl,
        LocalDateTime updatedAt,
        ProjectStatus status,
        UserProjectRole role
) {
}