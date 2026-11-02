
package com.campuspolio.domain.project.service;

import com.campuspolio.domain.project.dto.response.ProjectFileResponse;
import com.campuspolio.domain.project.dto.response.ProjectFileUploadResponse;
import com.campuspolio.domain.project.entity.Project;
import com.campuspolio.domain.project.entity.ProjectFile;
import com.campuspolio.domain.project.entity.UserProject;
import com.campuspolio.domain.project.repository.ProjectFileRepository;
import com.campuspolio.domain.project.repository.ProjectRepository;
import com.campuspolio.domain.project.repository.UserProjectRepository;
import com.campuspolio.domain.user.entity.User;
import com.campuspolio.domain.user.repository.UserRepository;
import com.campuspolio.global.exception.CustomException;
import com.campuspolio.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectFileService {

    private final ProjectRepository projectRepository;
    private final ProjectFileRepository projectFileRepository;
    private final UserRepository userRepository;
    private final UserProjectRepository userProjectRepository;
    private final S3Uploader s3Uploader;

    // 파일 업로드
    public ProjectFileUploadResponse uploadFile(
            Long loginUserId,
            Long projectId,
            MultipartFile file
    ) {

        User user = userRepository.findById(loginUserId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.USER_NOT_FOUND)
                );

        Project project = projectRepository
                .findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.PROJECT_NOT_FOUND)
                );

        UserProject userProject =
                userProjectRepository.findByUserAndProject(user, project)
                        .orElseThrow(() ->
                                new CustomException(ErrorCode.PROJECT_FORBIDDEN)
                        );

        // S3에 파일 업로드 후 객체 Key 반환
        String s3Key = s3Uploader.upload(projectId, file);

        // 객체 Key를 DB에 저장
        ProjectFile projectFile = ProjectFile.create(
                project,
                file.getOriginalFilename(),
                s3Key,
                file.getContentType(),
                file.getSize()
        );

        projectFileRepository.save(projectFile);

        // 클라이언트에는 Presigned URL 반환
        String fileUrl = s3Uploader.getPresignedUrl(s3Key);

        return new ProjectFileUploadResponse(
                projectFile.getId(),
                fileUrl
        );
    }

    // 프로젝트 파일 목록 조회
    @Transactional(readOnly = true)
    public List<ProjectFileResponse> getFiles(Long projectId) {

        Project project = projectRepository
                .findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.PROJECT_NOT_FOUND)
                );

        return projectFileRepository
                .findAllByProjectAndDeletedAtIsNull(project)
                .stream()
                .map(file -> new ProjectFileResponse(
                        file.getId(),
                        file.getOriginalName(),
                        s3Uploader.getPresignedUrl(file.getS3Key()),
                        file.getContentType(),
                        file.getFileSize()
                ))
                .toList();
    }

    // 파일 삭제
    public void deleteFile(Long fileId) {

        ProjectFile file = projectFileRepository
                .findByIdAndDeletedAtIsNull(fileId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.PROJECT_FILE_NOT_FOUND)
                );

        // S3 실제 객체 삭제
        s3Uploader.delete(file.getS3Key());

        // DB Soft Delete
        file.delete();
    }
}
