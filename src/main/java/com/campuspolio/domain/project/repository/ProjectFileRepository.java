package com.campuspolio.domain.project.repository;

import com.campuspolio.domain.project.entity.Project;
import com.campuspolio.domain.project.entity.ProjectFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectFileRepository
        extends JpaRepository<ProjectFile, Long> {

    List<ProjectFile> findAllByProjectAndDeletedAtIsNull(
            Project project
    );

    Optional<ProjectFile> findByIdAndDeletedAtIsNull(
            Long fileId
    );
}