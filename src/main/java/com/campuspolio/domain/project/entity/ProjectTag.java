package com.campuspolio.domain.project.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "project_tag",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_project_tag",
                        columnNames = {
                                "tag_id",
                                "project_id"
                        }
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_tag_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tag_id",
            nullable = false
    )
    private Tag tag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "project_id",
            nullable = false
    )
    private Project project;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private ProjectTag(
            Tag tag,
            Project project
    ) {
        this.tag = tag;
        this.project = project;
    }

    public static ProjectTag create(
            Tag tag,
            Project project
    ) {
        return new ProjectTag(
                tag,
                project
        );
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}