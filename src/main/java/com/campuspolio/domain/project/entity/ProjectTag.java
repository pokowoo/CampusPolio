package com.campuspolio.domain.project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
        name = "project_tags",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_project_tag",
                        columnNames = {"project_id", "tag_id"}
                )
        }
)
public class ProjectTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_tag_id")
    private Long projectTagId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    public static ProjectTag create(
            Project project,
            Tag tag
    ) {
        return ProjectTag.builder()
                .project(project)
                .tag(tag)
                .build();
    }
}