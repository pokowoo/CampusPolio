package com.campuspolio.domain.project.service;

import com.campuspolio.domain.project.dto.request.ProjectCreateRequest;
import com.campuspolio.domain.project.dto.request.ProjectPublishRequest;
import com.campuspolio.domain.project.dto.response.MyProjectResponse;
import com.campuspolio.domain.project.dto.response.ProjectCreateResponse;
import com.campuspolio.domain.project.entity.*;
import com.campuspolio.domain.project.repository.*;
import com.campuspolio.domain.user.entity.User;
import com.campuspolio.domain.user.repository.UserRepository;
import com.campuspolio.global.exception.CustomException;
import com.campuspolio.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UserProjectRepository userProjectRepository;
    private final TagRepository tagRepository;
    private final ProjectTagRepository projectTagRepository;

    /**
     * Draft 생성
     */
    public ProjectCreateResponse createProject(
            Long userId,
            ProjectCreateRequest request
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.USER_NOT_FOUND));

        Project project = Project.createDraft(
                request.title(),
                request.description()
        );

        projectRepository.save(project);

        UserProject owner =
                UserProject.owner(user, project);

        userProjectRepository.save(owner);

        return new ProjectCreateResponse(
                project.getId()
        );
    }

    /**
     * 내 프로젝트 조회
     */
    @Transactional(readOnly = true)
    public List<MyProjectResponse> getMyProjects(
            Long userId
    ) {

        List<UserProject> userProjects =
                userProjectRepository.findAllByUser_Id(userId);

        List<MyProjectResponse> result =
                new ArrayList<>();

        for (UserProject userProject : userProjects) {

            Project project =
                    userProject.getProject();

            if (project.isDeleted()) {
                continue;
            }

            result.add(
                    new MyProjectResponse(
                            project.getId(),
                            project.getTitle(),
                            project.getThumbnail(),
                            project.getStatus(),
                            userProject.getRole(),
                            project.getUpdatedAt()
                    )
            );
        }

        return result;
    }

    /**
     * 프로젝트 발행
     */
    public void publishProject(
            Long userId,
            Long projectId,
            ProjectPublishRequest request
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.USER_NOT_FOUND));

        Project project =
                projectRepository.findByIdAndDeletedAtIsNull(projectId)
                        .orElseThrow(() ->
                                new CustomException(
                                        ErrorCode.PROJECT_NOT_FOUND
                                ));

        UserProject userProject =
                userProjectRepository
                        .findByUserAndProject(user, project)
                        .orElseThrow(() ->
                                new CustomException(
                                        ErrorCode.PROJECT_FORBIDDEN
                                ));

        if (!userProject.isOwner()) {
            throw new CustomException(
                    ErrorCode.PROJECT_FORBIDDEN
            );
        }

        if (!user.isUniversityVerified()) {
            throw new CustomException(
                    ErrorCode.UNAUTHORIZED
            );
        }

        project.update(
                request.title(),
                request.description(),
                request.content(),
                project.getThumbnail()
        );

        saveTags(
                project,
                request.tags()
        );

        project.publish();
    }

    private void saveTags(
            Project project,
            List<String> tagNames
    ) {

        projectTagRepository.deleteAllByProject(project);

        if (tagNames == null) {
            return;
        }

        for (String tagName : tagNames) {

            Tag tag = tagRepository
                    .findByTagName(tagName)
                    .orElseGet(() ->
                            tagRepository.save(
                                    Tag.create(tagName)
                            ));

            projectTagRepository.save(
                    ProjectTag.create(
                            tag,
                            project
                    )
            );
        }
    }
}