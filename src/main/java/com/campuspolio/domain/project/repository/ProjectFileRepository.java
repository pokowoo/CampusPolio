package com.campuspolio.domain.project.repository;

import com.campuspolio.domain.project.entity.ProjectFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectFileRepository
        extends JpaRepository<ProjectFile, Long> {

    List<ProjectFile> findAllByIdIn(
            List<Long> fileIds
    );
}