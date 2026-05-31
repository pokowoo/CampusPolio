package com.campuspolio.domain.project.repository;

import com.campuspolio.domain.project.entity.Project;
import com.campuspolio.domain.project.entity.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByStatus(ProjectStatus status);

    Optional<Project> findByProjectIdAndDeletedAtIsNull(Long projectId);

    List<Project> findByStatusAndDeletedAtIsNull(ProjectStatus status);
}