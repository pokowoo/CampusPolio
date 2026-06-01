package com.campuspolio.domain.project.repository;

import com.campuspolio.domain.project.dto.response.MyProjectResponse;
import com.campuspolio.domain.project.entity.Project;
import com.campuspolio.domain.project.entity.UserProject;
import com.campuspolio.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserProjectRepository
        extends JpaRepository<UserProject, Long> {

    Optional<UserProject> findByUserAndProject(
            User user,
            Project project
    );

    @Query("""
        select new com.campuspolio.domain.project.dto.response.MyProjectResponse(
            p.id,
            p.title,
            p.thumbnail,
            p.updatedAt,
            p.status,
            up.role
        )
        from UserProject up
        join up.project p
        where up.user.id = :userId
        and p.deletedAt is null
        order by p.updatedAt desc
    """)
    Page<MyProjectResponse> findMyProjects(
            Long userId,
            Pageable pageable
    );
}