package com.campuspolio.domain.project.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "project")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(length = 500)
    private String thumbnail;

    @Lob
    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    private Project(String title, String description) {
        this.title = title;
        this.description = description;
        this.status = ProjectStatus.DRAFT;
        this.isPublic = false;
        this.viewCount = 0;
    }

    public static Project createDraft(String title, String description) {
        return new Project(title, description);
    }

    public void update(
            String title,
            String description,
            String content,
            String thumbnail
    ) {
        this.title = title;
        this.description = description;
        this.content = content;
        this.thumbnail = thumbnail;
    }

    public void publish() {
        this.status = ProjectStatus.PUBLISHED;
        this.isPublic = true;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
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