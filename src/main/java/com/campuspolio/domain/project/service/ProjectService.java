package com.campuspolio.domain.project.service;

import com.campuspolio.domain.project.dto.request.ProjectCreateRequest;
import com.campuspolio.domain.project.dto.response.ProjectCreateResponse;
import com.campuspolio.domain.project.entity.Project;
import com.campuspolio.domain.project.entity.UserProject;
import com.campuspolio.domain.project.repository.ProjectRepository;
import com.campuspolio.domain.project.repository.UserProjectRepository;
import com.campuspolio.domain.user.entity.User;
import com.campuspolio.domain.user.repository.UserRepository;
import com.campuspolio.global.exception.CustomException;
import com.campuspolio.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.campuspolio.domain.project.dto.response.MyProjectResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UserProjectRepository userProjectRepository;

    public ProjectCreateResponse createProject(
            Long userId,
            ProjectCreateRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.USER_NOT_FOUND)
                );

        Project project = Project.createDraft(
                request.title(),
                request.description()
        );

        projectRepository.save(project);

        UserProject owner = UserProject.owner(user, project);

        userProjectRepository.save(owner);

        return new ProjectCreateResponse(
                project.getId(),
                project.getStatus()
        );
    }

    public void publishProject(Long userId, Long projectId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.USER_NOT_FOUND)
                );

        Project project = projectRepository
                .findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.PROJECT_NOT_FOUND)
                );

        UserProject userProject = userProjectRepository
                .findByUserAndProject(user, project)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.PROJECT_FORBIDDEN)
                );

        if (!userProject.isOwner()) {
            throw new CustomException(ErrorCode.PROJECT_FORBIDDEN);
        }

        if (!user.isUniversityVerified()) {
            throw new CustomException(
                    ErrorCode.PROJECT_PUBLISH_FORBIDDEN
            );
        }

        if (project.getThumbnail() == null ||
                project.getThumbnail().isBlank()) {
            throw new CustomException(
                    ErrorCode.PROJECT_THUMBNAIL_REQUIRED
            );
        }

        if (project.getContent() == null ||
                project.getContent().length() < 30) {
            throw new CustomException(
                    ErrorCode.PROJECT_CONTENT_TOO_SHORT
            );
        }

        project.publish();
    }
    @Transactional(readOnly = true)
    public Page<MyProjectResponse> getMyProjects(
            Long userId,
            int page
    ) {

        Pageable pageable = PageRequest.of(
                page,
                9
        );

        return userProjectRepository.findMyProjects(
                userId,
                pageable
        );
    }
}