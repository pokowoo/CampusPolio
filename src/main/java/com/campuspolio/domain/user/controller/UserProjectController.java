package com.campuspolio.domain.user.controller;

import com.campuspolio.domain.project.dto.response.MyProjectResponse;
import com.campuspolio.domain.project.service.ProjectService;
import com.campuspolio.global.exception.CustomException;
import com.campuspolio.global.exception.ErrorCode;
import com.campuspolio.global.response.ApiResponse;
import com.campuspolio.global.security.SessionConst;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
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
            HttpSession session
    ) {

        Long loginUserId =
                (Long) session.getAttribute(
                        SessionConst.LOGIN_USER_ID
                );

        if (loginUserId == null) {
            throw new CustomException(
                    ErrorCode.UNAUTHORIZED
            );
        }

        List<MyProjectResponse> response =
                projectService.getMyProjects(
                        loginUserId
                );

        return ApiResponse.success(response);
    }
}