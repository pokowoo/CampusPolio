package com.campuspolio.domain.project.controller;

import com.campuspolio.domain.project.dto.request.ProjectSearchCondition;
import com.campuspolio.domain.project.dto.response.ProjectDetailResponse;
import com.campuspolio.domain.project.dto.response.ProjectSearchPageResponse;
import com.campuspolio.domain.project.entity.ProjectFilterType;
import com.campuspolio.domain.project.service.ProjectQueryService;
import com.campuspolio.global.response.ApiResponse;
import com.campuspolio.global.security.SessionConst;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
public class ProjectQueryController {

    private final ProjectQueryService projectQueryService;

    @Operation(summary = "프로젝트 검색")
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

    @Operation(summary = "프로젝트 상세 조회")
    @GetMapping("/{projectId}")
    public ApiResponse<ProjectDetailResponse> getProjectDetail(

            @PathVariable
            Long projectId,

            HttpSession session
    ) {

        Long loginUserId = null;

        if (session != null) {
            loginUserId =
                    (Long) session.getAttribute(
                            SessionConst.LOGIN_USER_ID
                    );
        }

        return ApiResponse.success(
                projectQueryService.getProjectDetail(
                        loginUserId,
                        projectId
                )
        );
    }
}