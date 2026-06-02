package com.campuspolio.domain.project.dto.response;

public record HomeProjectResponse(

        Long projectId,

        String title,

        String thumbnailUrl,

        String tag,

        String authorName,

        long likeCount,

        int viewCount

) {
}