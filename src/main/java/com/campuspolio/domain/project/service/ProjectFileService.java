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

    public ProjectFileUploadResponse uploadFile(
            Long loginUserId,
            Long projectId,
            MultipartFile file
    ) {

        User user =
                userRepository.findById(loginUserId)
                        .orElseThrow(() ->
                                new CustomException(
                                        ErrorCode.USER_NOT_FOUND
                                )
                        );

        Project project =
                projectRepository.findByIdAndDeletedAtIsNull(projectId)
                        .orElseThrow(() ->
                                new CustomException(
                                        ErrorCode.PROJECT_NOT_FOUND
                                )
                        );

        UserProject userProject =
                userProjectRepository.findByUserAndProject(
                                user,
                                project
                        )
                        .orElseThrow(() ->
                                new CustomException(
                                        ErrorCode.PROJECT_FORBIDDEN
                                )
                        );

        String fileUrl =
                s3Uploader.upload(
                        projectId,
                        file
                );

        ProjectFile projectFile =
                ProjectFile.create(
                        project,
                        file.getOriginalFilename(),
                        fileUrl,
                        file.getContentType(),
                        file.getSize()
                );

        projectFileRepository.save(
                projectFile
        );

        return new ProjectFileUploadResponse(
                projectFile.getId(),
                projectFile.getFileUrl()
        );
    }

    @Transactional(readOnly = true)
    public List<ProjectFileResponse> getFiles(
            Long projectId
    ) {

        Project project =
                projectRepository.findByIdAndDeletedAtIsNull(projectId)
                        .orElseThrow(() ->
                                new CustomException(
                                        ErrorCode.PROJECT_NOT_FOUND
                                )
                        );

        return projectFileRepository
                .findAllByProjectAndDeletedAtIsNull(
                        project
                )
                .stream()
                .map(file ->
                        new ProjectFileResponse(
                                file.getId(),
                                file.getOriginalName(),
                                file.getFileUrl(),
                                file.getContentType(),
                                file.getFileSize()
                        )
                )
                .toList();
    }

    public void deleteFile(
            Long fileId
    ) {

        ProjectFile file =
                projectFileRepository
                        .findByIdAndDeletedAtIsNull(
                                fileId
                        )
                        .orElseThrow(() ->
                                new CustomException(
                                        ErrorCode.PROJECT_FILE_NOT_FOUND
                                )
                        );

        file.delete();
    }
}