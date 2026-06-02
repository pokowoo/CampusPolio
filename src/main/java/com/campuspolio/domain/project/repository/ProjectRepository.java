package com.campuspolio.domain.project.repository;

import com.campuspolio.domain.project.entity.Project;
import com.campuspolio.domain.project.entity.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;

public interface ProjectRepository
        extends JpaRepository<Project, Long>,
        JpaSpecificationExecutor<Project> {

    Optional<Project> findByIdAndDeletedAtIsNull(
            Long projectId
    );
    List<Project>
    findTop10ByStatusAndIsPublicTrueAndDeletedAtIsNullOrderByViewCountDesc(
            ProjectStatus status
    );
}