package com.campuspolio.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_users_google_id", columnNames = "google_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "google_id", nullable = false, length = 255)
    private String googleId;

    // ⭐ 대학 인증 여부 (핵심)
    @Column(name = "university_verified", nullable = false)
    private boolean universityVerified;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole role;

    // soft delete
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // audit
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ======================
    // 생성자 / 팩토리
    // ======================

    private User(String email, String googleId) {
        this.email = email;
        this.googleId = googleId;
        this.universityVerified = false;
        this.role = UserRole.USER;
    }

    public static User createGoogleUser(String email, String googleId) {
        return new User(email, googleId);
    }

    // ======================
    // 비즈니스 로직
    // ======================

    public void verifyUniversity() {
        this.universityVerified = true;
    }

    public void withdraw() {
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.deletedAt = null;
    }

    public boolean isActive() {
        return this.deletedAt == null;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    // ======================
    // JPA lifecycle
    // ======================

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