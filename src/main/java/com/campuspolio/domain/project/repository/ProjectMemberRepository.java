package com.campuspolio.domain.project.repository;

import com.campuspolio.domain.project.entity.Project;
import com.campuspolio.domain.project.entity.ProjectMember;
import com.campuspolio.domain.project.entity.ProjectRole;
import com.campuspolio.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository
        extends JpaRepository<ProjectMember, Long> {

    boolean existsByProjectAndUser(
            Project project,
            User user
    );

    boolean existsByProjectAndUserAndRole(
            Project project,
            User user,
            ProjectRole role
    );

    Optional<ProjectMember> findByProjectAndUser(
            Project project,
            User user
    );

    List<ProjectMember> findAllByProject(Project project);
}