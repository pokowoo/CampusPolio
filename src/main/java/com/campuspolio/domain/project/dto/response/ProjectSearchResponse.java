package com.campuspolio.domain.project.dto.response;

import java.util.List;

public record ProjectSearchResponse(

        Long projectId,

        String title,

        String description,

        String thumbnailUrl,

        List<String> tags,

        List<ProjectUserResponse> users,

        int viewCount,

        long likeCount,

        boolean isLiked

) {
}