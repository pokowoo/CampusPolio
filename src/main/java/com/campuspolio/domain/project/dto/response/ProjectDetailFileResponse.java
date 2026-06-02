package com.campuspolio.domain.project.dto.response;

public record ProjectDetailFileResponse(

        Long fileId,

        String originalName,

        String fileUrl

) {
}