package com.campuspolio.domain.project.controller;

import com.campuspolio.domain.project.dto.request.ProjectSearchCondition;
import com.campuspolio.domain.project.dto.response.ProjectSearchPageResponse;
import com.campuspolio.domain.project.entity.ProjectFilterType;
import com.campuspolio.domain.project.service.ProjectQueryService;
import com.campuspolio.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
public class ProjectQueryController {

    private final ProjectQueryService projectQueryService;

    @GetMapping
    public ApiResponse<ProjectSearchPageResponse> search(

            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            List<String> tags,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "9")
            int size,

            @RequestParam(defaultValue = "LATEST")
            ProjectFilterType filterType

    ) {

        ProjectSearchCondition condition =
                new ProjectSearchCondition(
                        keyword,
                        tags,
                        page,
                        size,
                        filterType
                );

        return ApiResponse.success(
                projectQueryService.search(
                        null,
                        condition
                )
        );
    }
}