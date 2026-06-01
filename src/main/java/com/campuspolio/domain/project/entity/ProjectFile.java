package com.campuspolio.domain.project.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "project_file")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "is_connected", nullable = false)
    private boolean isConnected;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    private ProjectFile(
            Project project,
            String originalName,
            String fileUrl,
            String contentType,
            Long fileSize
    ) {
        this.project = project;
        this.originalName = originalName;
        this.fileUrl = fileUrl;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.isConnected = true;
    }

    public static ProjectFile create(
            Project project,
            String originalName,
            String fileUrl,
            String contentType,
            Long fileSize
    ) {
        return new ProjectFile(
                project,
                originalName,
                fileUrl,
                contentType,
                fileSize
        );
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }
}