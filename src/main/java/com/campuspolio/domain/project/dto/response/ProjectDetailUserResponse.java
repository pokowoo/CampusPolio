package com.campuspolio.domain.project.dto.response;

import com.campuspolio.domain.project.entity.UserProjectRole;

public record ProjectDetailUserResponse(

        Long userId,

        String name,

        UserProjectRole role

) {
}