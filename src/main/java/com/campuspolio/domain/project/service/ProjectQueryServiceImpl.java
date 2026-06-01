package com.campuspolio.domain.project.service;

import com.campuspolio.domain.project.dto.request.ProjectSearchCondition;
import com.campuspolio.domain.project.dto.response.ProjectSearchPageResponse;
import com.campuspolio.domain.project.dto.response.ProjectSearchResponse;
import com.campuspolio.domain.project.entity.Project;
import com.campuspolio.domain.project.entity.ProjectFilterType;
import com.campuspolio.domain.project.repository.ProjectRepository;
import com.campuspolio.domain.project.specification.ProjectSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.campuspolio.domain.profile.entity.UserProfile;
import com.campuspolio.domain.profile.repository.UserProfileRepository;
import com.campuspolio.domain.project.dto.response.ProjectUserResponse;
import com.campuspolio.domain.project.entity.UserProject;
import com.campuspolio.domain.project.repository.ProjectLikeRepository;
import com.campuspolio.domain.project.repository.ProjectTagRepository;
import com.campuspolio.domain.project.repository.UserProjectRepository;
import com.campuspolio.domain.user.entity.User;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectQueryServiceImpl
        implements ProjectQueryService {
    private final ProjectTagRepository projectTagRepository;
    private final UserProjectRepository userProjectRepository;
    private final UserProfileRepository userProfileRepository;
    private final ProjectLikeRepository projectLikeRepository;
    private final ProjectRepository projectRepository;

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
                                            .countByProject(project);

                            boolean isLiked = false;

                            return new ProjectSearchResponse(
                                    project.getId(),
                                    project.getTitle(),
                                    project.getDescription(),
                                    project.getThumbnail(),
                                    tags,
                                    users,
                                    project.getViewCount(),
                                    likeCount,
                                    isLiked
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
}