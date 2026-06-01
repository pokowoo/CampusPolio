package com.campuspolio.domain.project.repository;

import com.campuspolio.domain.project.dto.request.ProjectSearchCondition;
import com.campuspolio.domain.project.entity.Project;
import org.springframework.data.domain.Page;

public interface ProjectRepositoryCustom {

    Page<Project> search(
            ProjectSearchCondition condition
    );

}