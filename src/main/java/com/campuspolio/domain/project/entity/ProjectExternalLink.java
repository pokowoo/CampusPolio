package com.campuspolio.domain.project.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "project_external_link")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectExternalLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ex_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "project_id",
            nullable = false
    )
    private Project project;

    @Column(
            name = "ex_url",
            nullable = false,
            length = 1000
    )
    private String url;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private ProjectExternalLink(
            Project project,
            String url
    ) {
        this.project = project;
        this.url = url;
    }

    public static ProjectExternalLink create(
            Project project,
            String url
    ) {
        return new ProjectExternalLink(
                project,
                url
        );
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}