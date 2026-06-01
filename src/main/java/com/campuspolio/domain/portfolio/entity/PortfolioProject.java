package com.campuspolio.domain.portfolio.entity;

import com.campuspolio.domain.project.entity.Project;
import com.campuspolio.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
        name = "portfolio_projects",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_portfolio_project",
                        columnNames = {"portfolio_id", "project_id"}
                )
        }
)
public class PortfolioProject extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_project_id")
    private Long portfolioProjectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    // =========================
    // 생성 메서드
    // =========================

    public static PortfolioProject create(
            Portfolio portfolio,
            Project project,
            int displayOrder
    ) {
        return PortfolioProject.builder()
                .portfolio(portfolio)
                .project(project)
                .displayOrder(displayOrder)
                .build();
    }

    // =========================
    // 비즈니스 로직
    // =========================

    public void changeOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}