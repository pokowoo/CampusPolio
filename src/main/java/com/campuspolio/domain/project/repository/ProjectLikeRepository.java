package com.campuspolio.domain.project.repository;

import com.campuspolio.domain.project.entity.ProjectLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectLikeRepository
        extends JpaRepository<ProjectLike, Long> {

    long countByProject_Id(Long projectId);

    boolean existsByUser_IdAndProject_Id(
            Long userId,
            Long projectId
    );
}