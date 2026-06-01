package com.campuspolio.domain.project.entity;

import com.campuspolio.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "project_like",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_project_like",
                        columnNames = {
                                "user_id",
                                "project_id"
                        }
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "project_id",
            nullable = false
    )
    private Project project;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private ProjectLike(
            User user,
            Project project
    ) {
        this.user = user;
        this.project = project;
    }

    public static ProjectLike create(
            User user,
            Project project
    ) {
        return new ProjectLike(
                user,
                project
        );
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}