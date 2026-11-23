package com.campuspolio.domain.user.controller;

import com.campuspolio.domain.project.dto.response.MyProjectResponse;
import com.campuspolio.domain.project.service.ProjectService;
import com.campuspolio.global.response.ApiResponse;
import com.campuspolio.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(
        name = "My Project",
        description = "내 프로젝트 조회 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me")
public class UserProjectController {

    private final ProjectService projectService;

    @Operation(
            summary = "내 프로젝트 목록 조회",
            description = """
                    내가 OWNER 또는 MEMBER로 참여중인
                    프로젝트 목록을 조회합니다.
                    """
    )
    @GetMapping("/projects")
    public ApiResponse<List<MyProjectResponse>> getMyProjects(
            @AuthenticatedUser Long loginUserId
    ) {

        List<MyProjectResponse> response =
                projectService.getMyProjects(
                        loginUserId
                );

        return ApiResponse.success(response);
    }
}