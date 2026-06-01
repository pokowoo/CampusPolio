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
        name = "user_project",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_user_project",
                        columnNames = {
                                "user_id",
                                "project_id"
                        }
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_project_id")
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserProjectRole role;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private UserProject(
            User user,
            Project project,
            UserProjectRole role
    ) {
        this.user = user;
        this.project = project;
        this.role = role;
    }

    public static UserProject owner(
            User user,
            Project project
    ) {
        return new UserProject(
                user,
                project,
                UserProjectRole.OWNER
        );
    }

    public static UserProject member(
            User user,
            Project project
    ) {
        return new UserProject(
                user,
                project,
                UserProjectRole.MEMBER
        );
    }

    public boolean isOwner() {
        return role == UserProjectRole.OWNER;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}