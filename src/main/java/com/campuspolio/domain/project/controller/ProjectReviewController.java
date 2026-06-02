package com.campuspolio.domain.project.controller;

import com.campuspolio.domain.project.dto.response.ProjectReviewResponse;
import com.campuspolio.domain.project.service.ProjectReviewService;
import com.campuspolio.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
@Tag(name = "Project Review", description = "AI 코드 리뷰")
public class ProjectReviewController {

    private final ProjectReviewService projectReviewService;

    @Operation(summary = "AI 코드 리뷰")
    @PostMapping("/{projectId}/review")
    public ApiResponse<ProjectReviewResponse> reviewProject(

            @PathVariable
            Long projectId
    ) {

        return ApiResponse.success(
                projectReviewService.reviewProject(
                        projectId
                )
        );
    }
}