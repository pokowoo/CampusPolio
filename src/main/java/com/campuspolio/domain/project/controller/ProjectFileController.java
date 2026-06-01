package com.campuspolio.domain.project.controller;

import com.campuspolio.domain.project.dto.response.ProjectFileResponse;
import com.campuspolio.domain.project.dto.response.ProjectFileUploadResponse;
import com.campuspolio.domain.project.service.ProjectFileService;
import com.campuspolio.global.exception.CustomException;
import com.campuspolio.global.exception.ErrorCode;
import com.campuspolio.global.response.ApiResponse;
import com.campuspolio.global.security.SessionConst;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
@Tag(name = "Project File", description = "프로젝트 파일 API")
public class ProjectFileController {

    private final ProjectFileService projectFileService;

    @Operation(summary = "프로젝트 파일 업로드")
    @PostMapping(
            value = "/{projectId}/files",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<ProjectFileUploadResponse> uploadFile(

            @PathVariable
            Long projectId,

            @RequestPart("file")
            MultipartFile file,

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

        return ApiResponse.success(
                projectFileService.uploadFile(
                        loginUserId,
                        projectId,
                        file
                )
        );
    }

    @Operation(summary = "프로젝트 파일 조회")
    @GetMapping("/{projectId}/files")
    public ApiResponse<List<ProjectFileResponse>> getFiles(

            @PathVariable
            Long projectId
    ) {

        return ApiResponse.success(
                projectFileService.getFiles(
                        projectId
                )
        );
    }

    @Operation(summary = "프로젝트 파일 삭제")
    @DeleteMapping("/files/{fileId}")
    public ApiResponse<Void> deleteFile(

            @PathVariable
            Long fileId
    ) {

        projectFileService.deleteFile(
                fileId
        );

        return ApiResponse.success(
                null
        );
    }
}