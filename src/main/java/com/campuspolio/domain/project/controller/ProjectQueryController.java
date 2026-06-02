package com.campuspolio.domain.project.controller;

import com.campuspolio.domain.project.dto.request.ProjectSearchCondition;
import com.campuspolio.domain.project.dto.response.ProjectDetailResponse;
import com.campuspolio.domain.project.dto.response.ProjectSearchPageResponse;
import com.campuspolio.domain.project.entity.ProjectFilterType;
import com.campuspolio.domain.project.service.ProjectQueryService;
import com.campuspolio.global.response.ApiResponse;
import com.campuspolio.global.security.SessionConst;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Project Query",
        description = "프로젝트 조회 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
public class ProjectQueryController {

    private final ProjectQueryService projectQueryService;

    @Operation(
            summary = "프로젝트 검색",
            description = "키워드, 태그, 정렬 조건을 이용하여 공개된 프로젝트를 검색합니다."
    )
    @GetMapping
    public ApiResponse<ProjectSearchPageResponse> search(

            @Parameter(description = "검색 키워드")
            @RequestParam(required = false)
            String keyword,

            @Parameter(description = "태그 목록")
            @RequestParam(required = false)
            List<String> tags,

            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0")
            int page,

            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "9")
            int size,

            @Parameter(description = "정렬 조건 (LATEST, POPULAR)")
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

    @Operation(
            summary = "프로젝트 상세 조회",
            description = "프로젝트 ID를 이용하여 프로젝트 상세 정보를 조회합니다."
    )
    @GetMapping("/{projectId}")
    public ApiResponse<ProjectDetailResponse> getProjectDetail(

            @Parameter(description = "조회할 프로젝트 ID", example = "1")
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