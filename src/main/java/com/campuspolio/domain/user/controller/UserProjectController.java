package com.campuspolio.domain.user.controller;

import com.campuspolio.domain.project.dto.response.MyProjectResponse;
import com.campuspolio.domain.project.service.ProjectService;
import com.campuspolio.global.response.ApiResponse;
import com.campuspolio.global.security.SessionConst;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "User Project",
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
                    현재 로그인한 사용자가
                    OWNER 또는 MEMBER로 참여 중인
                    프로젝트 목록을 조회합니다.
                    
                    기본 size = 9
                    """
    )
    @GetMapping("/projects")
    public ApiResponse<Page<MyProjectResponse>> getMyProjects(
            @RequestParam(defaultValue = "0") int page,
            HttpSession session
    ) {

        Long loginUserId =
                (Long) session.getAttribute(
                        SessionConst.LOGIN_USER_ID
                );

        Page<MyProjectResponse> response =
                projectService.getMyProjects(
                        loginUserId,
                        page
                );

        return ApiResponse.success(response);
    }
}