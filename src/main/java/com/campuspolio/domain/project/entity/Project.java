package com.campuspolio.domain.project.entity;

import com.campuspolio.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "projects")
public class Project extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long projectId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 500)
    private String description;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectStatus status;

    // soft delete
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // =========================
    // 생성 메서드
    // =========================

    public static Project createDraft(
            String title,
            String description
    ) {
        return Project.builder()
                .title(title)
                .description(description)
                .content("")
                .thumbnailUrl(null)
                .isPublic(false)
                .viewCount(0)
                .likeCount(0)
                .status(ProjectStatus.DRAFT)
                .deletedAt(null)
                .build();
    }

    // =========================
    // 비즈니스 로직
    // =========================

    public void update(
            String title,
            String description,
            String content,
            String thumbnailUrl
    ) {
        this.title = title;
        this.description = description;
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
    }

    public void publish() {
        this.status = ProjectStatus.PUBLISHED;
        this.isPublic = true;
    }

    public void makePublic() {
        this.isPublic = true;
    }

    public void makePrivate() {
        this.isPublic = false;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
        this.isPublic = false;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public boolean isActive() {
        return this.deletedAt == null;
    }
}