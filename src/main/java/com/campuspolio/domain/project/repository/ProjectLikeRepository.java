package com.campuspolio.domain.project.repository;

import com.campuspolio.domain.project.entity.Project;
import com.campuspolio.domain.project.entity.ProjectLike;
import com.campuspolio.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectLikeRepository
        extends JpaRepository<ProjectLike, Long> {

    long countByProject(
            Project project
    );

    boolean existsByUserAndProject(
            User user,
            Project project
    );
}