package com.campuspolio.domain.project.entity;

import com.campuspolio.domain.user.entity.User;
import com.campuspolio.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
        name = "project_members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_project_member",
                        columnNames = {"project_id", "user_id"}
                )
        }
)
public class ProjectMember extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_member_id")
    private Long projectMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectRole role;

    // =========================
    // 생성 메서드
    // =========================

    public static ProjectMember createOwner(
            Project project,
            User user
    ) {
        return ProjectMember.builder()
                .project(project)
                .user(user)
                .role(ProjectRole.OWNER)
                .build();
    }

    public static ProjectMember createMember(
            Project project,
            User user
    ) {
        return ProjectMember.builder()
                .project(project)
                .user(user)
                .role(ProjectRole.MEMBER)
                .build();
    }

    // =========================
    // 비즈니스 로직
    // =========================

    public boolean isOwner() {
        return this.role == ProjectRole.OWNER;
    }

    public boolean isMember() {
        return this.role == ProjectRole.MEMBER;
    }
}