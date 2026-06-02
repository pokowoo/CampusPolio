package com.campuspolio.domain.project.service;

import com.campuspolio.domain.profile.entity.UserProfile;
import com.campuspolio.domain.profile.repository.UserProfileRepository;
import com.campuspolio.domain.project.dto.request.ProjectSearchCondition;
import com.campuspolio.domain.project.dto.response.*;
import com.campuspolio.domain.project.entity.Project;
import com.campuspolio.domain.project.entity.ProjectFilterType;
import com.campuspolio.domain.project.entity.UserProject;
import com.campuspolio.domain.project.repository.*;
import com.campuspolio.domain.project.specification.ProjectSpecification;
import com.campuspolio.domain.user.entity.User;
import com.campuspolio.global.exception.CustomException;
import com.campuspolio.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectQueryServiceImpl
        implements ProjectQueryService {

    private final ProjectRepository projectRepository;
    private final ProjectTagRepository projectTagRepository;
    private final UserProjectRepository userProjectRepository;
    private final UserProfileRepository userProfileRepository;
    private final ProjectLikeRepository projectLikeRepository;
    private final ProjectFileRepository projectFileRepository;

    @Override
    public ProjectSearchPageResponse search(
            Long loginUserId,
            ProjectSearchCondition condition
    ) {

        Sort sort;

        if (condition.filterType()
                == ProjectFilterType.VIEW_COUNT) {

            sort = Sort.by(
                    Sort.Direction.DESC,
                    "viewCount"
            );

        } else {

            sort = Sort.by(
                    Sort.Direction.DESC,
                    "createdAt"
            );
        }

        Pageable pageable =
                PageRequest.of(
                        condition.page(),
                        condition.size(),
                        sort
                );

        Specification<Project> spec =
                Specification.allOf(
                        ProjectSpecification.publishedOnly(),
                        ProjectSpecification.titleContains(
                                condition.keyword()
                        ),
                        ProjectSpecification.hasTags(
                                condition.tags()
                        )
                );

        Page<Project> result =
                projectRepository.findAll(
                        spec,
                        pageable
                );

        List<ProjectSearchResponse> content =
                result.getContent()
                        .stream()
                        .map(project -> {

                            List<String> tags =
                                    projectTagRepository
                                            .findAllByProject(project)
                                            .stream()
                                            .map(projectTag ->
                                                    projectTag.getTag().getTagName()
                                            )
                                            .toList();

                            List<ProjectUserResponse> users =
                                    userProjectRepository
                                            .findAllByProject(project)
                                            .stream()
                                            .map(userProject -> {

                                                User user =
                                                        userProject.getUser();

                                                String name =
                                                        userProfileRepository
                                                                .findByUser(user)
                                                                .map(UserProfile::getName)
                                                                .orElse("이름없음");

                                                return new ProjectUserResponse(
                                                        user.getId(),
                                                        name,
                                                        userProject.getRole()
                                                );
                                            })
                                            .toList();

                            long likeCount =
                                    projectLikeRepository
                                            .countByProject_Id(
                                                    project.getId()
                                            );

                            return new ProjectSearchResponse(
                                    project.getId(),
                                    project.getTitle(),
                                    project.getDescription(),
                                    project.getThumbnail(),
                                    tags,
                                    users,
                                    project.getViewCount(),
                                    likeCount,
                                    false
                            );
                        })
                        .toList();

        return new ProjectSearchPageResponse(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Override
    @Transactional
    public ProjectDetailResponse getProjectDetail(
            Long loginUserId,
            Long projectId
    ) {

        Project project =
                projectRepository
                        .findByIdAndDeletedAtIsNull(
                                projectId
                        )
                        .orElseThrow(() ->
                                new CustomException(
                                        ErrorCode.PROJECT_NOT_FOUND
                                )
                        );

        project.increaseViewCount();

        List<String> tags =
                projectTagRepository
                        .findAllByProject(project)
                        .stream()
                        .map(projectTag ->
                                projectTag.getTag()
                                        .getTagName()
                        )
                        .toList();

        List<ProjectDetailUserResponse> users =
                userProjectRepository
                        .findAllByProject(project)
                        .stream()
                        .map(userProject -> {

                            UserProfile profile =
                                    userProfileRepository
                                            .findByUserId(
                                                    userProject
                                                            .getUser()
                                                            .getId()
                                            )
                                            .orElse(null);

                            return new ProjectDetailUserResponse(
                                    userProject.getUser().getId(),
                                    profile != null
                                            ? profile.getName()
                                            : "알 수 없음",
                                    userProject.getRole()
                            );
                        })
                        .toList();

        List<ProjectDetailFileResponse> files =
                projectFileRepository
                        .findAllByProjectAndDeletedAtIsNull(
                                project
                        )
                        .stream()
                        .map(file ->
                                new ProjectDetailFileResponse(
                                        file.getId(),
                                        file.getOriginalName(),
                                        file.getFileUrl()
                                )
                        )
                        .toList();

        long likeCount =
                projectLikeRepository
                        .countByProject_Id(
                                projectId
                        );

        boolean isLiked =
                loginUserId != null
                        && projectLikeRepository
                        .existsByUser_IdAndProject_Id(
                                loginUserId,
                                projectId
                        );

        return new ProjectDetailResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getContent(),
                project.getThumbnail(),
                tags,
                users,
                files,
                project.getViewCount(),
                likeCount,
                isLiked,
                project.getCreatedAt()
        );
    }
}