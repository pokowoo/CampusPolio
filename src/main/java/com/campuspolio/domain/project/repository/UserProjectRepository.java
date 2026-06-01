package com.campuspolio.domain.project.repository;

import com.campuspolio.domain.project.entity.Project;
import com.campuspolio.domain.project.entity.UserProject;
import com.campuspolio.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserProjectRepository
        extends JpaRepository<UserProject, Long> {

    Optional<UserProject> findByUserAndProject(
            User user,
            Project project
    );

    List<UserProject> findAllByUser_Id(
            Long userId
    );
}