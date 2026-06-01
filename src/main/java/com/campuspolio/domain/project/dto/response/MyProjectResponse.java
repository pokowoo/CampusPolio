package com.campuspolio.domain.project.dto.response;

import com.campuspolio.domain.project.entity.ProjectStatus;
import com.campuspolio.domain.project.entity.UserProjectRole;

import java.time.LocalDateTime;

public record MyProjectResponse(

        Long projectId,

        String title,

        String thumbnail,

        ProjectStatus status,

        UserProjectRole role,

        LocalDateTime updatedAt

) {
}