package com.campuspolio.domain.project.controller;

import com.campuspolio.domain.project.dto.request.ProjectCreateRequest;
import com.campuspolio.domain.project.dto.request.ProjectPublishRequest;
import com.campuspolio.domain.project.dto.response.ProjectCreateResponse;
import com.campuspolio.domain.project.service.ProjectService;
import com.campuspolio.global.exception.CustomException;
import com.campuspolio.global.exception.ErrorCode;
import com.campuspolio.global.response.ApiResponse;
import com.campuspolio.global.security.SessionConst;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Project", description = "프로젝트 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    @Operation(
            summary = "프로젝트 Draft 생성",
            description = """
                    프로젝트를 Draft 상태로 생성합니다.
                    로그인한 사용자만 가능합니다.
                    생성한 사용자는 OWNER 권한을 가집니다.
                    """
    )
    @PostMapping
    public ApiResponse<ProjectCreateResponse> createProject(
            @Valid @RequestBody ProjectCreateRequest request,
            HttpSession session
    ) {

        Long loginUserId = (Long) session.getAttribute(
                SessionConst.LOGIN_USER_ID
        );

        if (loginUserId == null) {
            throw new CustomException(
                    ErrorCode.UNAUTHORIZED
            );
        }

        ProjectCreateResponse response =
                projectService.createProject(
                        loginUserId,
                        request
                );

        return ApiResponse.success(response);
    }

    @Operation(
            summary = "프로젝트 발행",
            description = """
                    Draft 프로젝트를 발행(PUBLISHED) 상태로 변경합니다.
                    OWNER만 가능합니다.
                    """
    )
    @PostMapping("/{projectId}/publish")
    public ApiResponse<Void> publishProject(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectPublishRequest request,
            HttpSession session
    ) {

        Long loginUserId = (Long) session.getAttribute(
                SessionConst.LOGIN_USER_ID
        );

        if (loginUserId == null) {
            throw new CustomException(
                    ErrorCode.UNAUTHORIZED
            );
        }

        projectService.publishProject(
                loginUserId,
                projectId,
                request
        );

        return ApiResponse.success(null);
    }
}