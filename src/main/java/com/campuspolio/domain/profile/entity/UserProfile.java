package com.campuspolio.domain.profile.entity;

import com.campuspolio.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "user_profiles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_profiles_user_id", columnNames = "user_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(length = 500)
    private String bio;

    @Column(length = 100)
    private String major;

    private Integer grade;

    @Column(name = "profile_image", length = 1000)
    private String profileImage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private UserProfile(User user, String nickname, String bio) {
        this.user = user;
        this.nickname = nickname;
        this.bio = bio;
    }

    public static UserProfile create(User user, String nickname, String bio) {
        return new UserProfile(user, nickname, bio);
    }

    public void update(
            String name,
            String nickname,
            String bio,
            String major,
            Integer grade,
            String profileImage
    ) {
        this.name = name;
        this.nickname = nickname;
        this.bio = bio;
        this.major = major;
        this.grade = grade;
        this.profileImage = profileImage;
        this.updatedAt = LocalDateTime.now();
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