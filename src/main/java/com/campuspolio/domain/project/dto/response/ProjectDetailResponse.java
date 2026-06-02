package com.campuspolio.domain.project.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectDetailResponse(

        Long projectId,

        String title,

        String description,

        String content,

        String thumbnailUrl,

        List<String> tags,

        List<ProjectDetailUserResponse> users,

        List<ProjectDetailFileResponse> files,

        int viewCount,

        long likeCount,

        boolean isLiked,

        LocalDateTime createdAt

) {
}