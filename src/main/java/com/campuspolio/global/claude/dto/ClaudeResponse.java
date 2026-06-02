package com.campuspolio.global.claude.dto;

import java.util.List;

public record ClaudeResponse(

        String id,
        String type,
        String role,
        List<ClaudeContent> content

) {
}