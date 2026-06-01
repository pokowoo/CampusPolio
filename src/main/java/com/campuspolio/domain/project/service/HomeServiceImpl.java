package com.campuspolio.domain.project.service;

import com.campuspolio.domain.profile.entity.UserProfile;
import com.campuspolio.domain.profile.repository.UserProfileRepository;
import com.campuspolio.domain.project.dto.response.HomeCategoryResponse;
import com.campuspolio.domain.project.dto.response.HomeProjectResponse;
import com.campuspolio.domain.project.dto.response.HomeResponse;
import com.campuspolio.domain.project.entity.Project;
import com.campuspolio.domain.project.entity.ProjectStatus;
import com.campuspolio.domain.project.entity.ProjectTag;
import com.campuspolio.domain.project.entity.UserProject;
import com.campuspolio.domain.project.repository.ProjectLikeRepository;
import com.campuspolio.domain.project.repository.ProjectRepository;
import com.campuspolio.domain.project.repository.ProjectTagRepository;
import com.campuspolio.domain.project.repository.UserProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeServiceImpl implements HomeService {

    private final ProjectRepository projectRepository;
    private final ProjectTagRepository projectTagRepository;
    private final UserProjectRepository userProjectRepository;
    private final UserProfileRepository userProfileRepository;
    private final ProjectLikeRepository projectLikeRepository;

    private static final List<String> MAIN_TAGS =
            List.of(
                    "건축",
                    "도예",
                    "AI",
                    "UX/UI",
                    "Capstone"
            );

    @Override
    public HomeResponse getHome() {

        List<HomeProjectResponse> popularProjects =
                projectRepository
                        .findTop10ByStatusAndIsPublicTrueAndDeletedAtIsNullOrderByViewCountDesc(
                                ProjectStatus.PUBLISHED
                        )
                        .stream()
                        .map(this::toProjectResponse)
                        .toList();

        List<HomeCategoryResponse> categories =
                MAIN_TAGS.stream()
                        .map(tag -> {

                            List<HomeProjectResponse> projects =
                                    projectTagRepository
                                            .findAllByTag_TagName(tag)
                                            .stream()
                                            .map(ProjectTag::getProject)
                                            .filter(project ->
                                                    project.getStatus()
                                                            == ProjectStatus.PUBLISHED
                                            )
                                            .limit(10)
                                            .map(this::toProjectResponse)
                                            .toList();

                            return new HomeCategoryResponse(
                                    tag,
                                    projects
                            );
                        })
                        .toList();

        return new HomeResponse(
                popularProjects,
                categories
        );
    }

    private HomeProjectResponse toProjectResponse(
            Project project
    ) {

        String tag =
                projectTagRepository
                        .findAllByProject(project)
                        .stream()
                        .findFirst()
                        .map(projectTag ->
                                projectTag.getTag().getTagName()
                        )
                        .orElse("");

        String authorName =
                userProjectRepository
                        .findAllByProject(project)
                        .stream()
                        .filter(UserProject::isOwner)
                        .findFirst()
                        .map(UserProject::getUser)
                        .flatMap(userProfileRepository::findByUser)
                        .map(UserProfile::getName)
                        .orElse("알 수 없음");

        long likeCount =
                projectLikeRepository
                        .countByProject(project);

        return new HomeProjectResponse(
                project.getId(),
                project.getTitle(),
                project.getThumbnail(),
                tag,
                authorName,
                likeCount,
                project.getViewCount()
        );
    }
}