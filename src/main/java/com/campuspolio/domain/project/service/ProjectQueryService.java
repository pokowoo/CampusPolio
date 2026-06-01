package com.campuspolio.domain.project.service;

import com.campuspolio.domain.project.dto.request.ProjectSearchCondition;
import com.campuspolio.domain.project.dto.response.ProjectSearchPageResponse;

public interface ProjectQueryService {

    ProjectSearchPageResponse search(
            Long loginUserId,
            ProjectSearchCondition condition
    );
}