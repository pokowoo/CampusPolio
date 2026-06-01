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

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectQueryServiceImpl
        implements ProjectQueryService {

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
                Specification
                        .where(
                                ProjectSpecification.publishedOnly()
                        )
                        .and(
                                ProjectSpecification.titleContains(
                                        condition.keyword()
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
                        .map(project ->
                                new ProjectSearchResponse(
                                        project.getId(),
                                        project.getTitle(),
                                        project.getDescription(),
                                        project.getThumbnail(),
                                        List.of(),
                                        List.of(),
                                        project.getViewCount(),
                                        0,
                                        false
                                )
                        )
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