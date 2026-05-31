package com.campuspolio.domain.portfolio.entity;

import com.campuspolio.domain.user.entity.User;
import com.campuspolio.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
        name = "portfolios",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_portfolios_slug", columnNames = "slug")
        }
)
public class Portfolio extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_id")
    private Long portfolioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 150)
    private String slug;

    @Column(length = 500)
    private String description;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    // soft delete
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // =========================
    // 생성 메서드
    // =========================

    public static Portfolio create(
            User user,
            String title,
            String slug
    ) {
        return Portfolio.builder()
                .user(user)
                .title(title)
                .slug(slug)
                .description(null)
                .thumbnailUrl(null)
                .isPublic(false)
                .deletedAt(null)
                .build();
    }

    // =========================
    // 비즈니스 로직
    // =========================

    public void updateInfo(
            String title,
            String description,
            String thumbnailUrl
    ) {
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
    }

    public void makePublic() {
        this.isPublic = true;
    }

    public void makePrivate() {
        this.isPublic = false;
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

    public boolean isOwner(User user) {
        return this.user.getId().equals(user.getId());
    }
}