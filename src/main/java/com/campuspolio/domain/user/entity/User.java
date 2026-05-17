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

    @Column(name = "domain_valid", nullable = false)
    private boolean domainValid;

    @Column(nullable = false)
    private boolean verified;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private User(
            String email,
            String googleId,
            boolean domainValid
    ) {
        this.email = email;
        this.googleId = googleId;
        this.domainValid = domainValid;
        this.verified = false;
        this.role = UserRole.USER;
        this.status = UserStatus.ACTIVE;
    }

    public static User createGoogleUser(
            String email,
            String googleId,
            boolean domainValid
    ) {
        return new User(email, googleId, domainValid);
    }

    public void updateDomainValid(boolean domainValid) {
        this.domainValid = domainValid;
    }

    public void verifyEmail() {
        this.verified = true;
    }

    public void withdraw() {
        this.status = UserStatus.DELETED;
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
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