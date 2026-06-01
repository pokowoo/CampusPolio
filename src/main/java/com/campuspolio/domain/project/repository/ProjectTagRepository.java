package com.campuspolio.domain.project.repository;

import com.campuspolio.domain.project.entity.Project;
import com.campuspolio.domain.project.entity.ProjectTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectTagRepository
        extends JpaRepository<ProjectTag, Long> {

    List<ProjectTag> findAllByProject(Project project);

    void deleteAllByProject(Project project);

    boolean existsByProject_IdAndTag_Id(
            Long projectId,
            Long tagId
    );
}