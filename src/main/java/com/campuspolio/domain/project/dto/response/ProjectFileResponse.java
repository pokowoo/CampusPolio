package com.campuspolio.domain.project.dto.response;

public record ProjectFileResponse(

        Long fileId,

        String originalName,

        String fileUrl,

        String contentType,

        Long fileSize

) {
}