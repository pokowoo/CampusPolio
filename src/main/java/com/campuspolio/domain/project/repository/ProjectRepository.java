package com.campuspolio.domain.project.repository;

import com.campuspolio.domain.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRepository
        extends JpaRepository<Project, Long> {

    Optional<Project> findByIdAndDeletedAtIsNull(
            Long projectId
    );
}