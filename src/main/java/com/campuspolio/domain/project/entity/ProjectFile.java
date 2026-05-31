package com.campuspolio.domain.project.entity;

import com.campuspolio.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "project_files")
public class ProjectFile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_file_id")
    private Long projectFileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    // orphan cleanup 용도
    @Column(nullable = false)
    private boolean connected;

    // =========================
    // 생성 메서드
    // =========================

    public static ProjectFile create(
            String fileUrl,
            String originalFileName,
            String contentType,
            Long fileSize
    ) {
        return ProjectFile.builder()
                .fileUrl(fileUrl)
                .originalFileName(originalFileName)
                .contentType(contentType)
                .fileSize(fileSize)
                .connected(false)
                .build();
    }

    // =========================
    // 비즈니스 로직
    // =========================

    public void connect(Project project) {
        this.project = project;
        this.connected = true;
    }
}