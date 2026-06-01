package com.campuspolio.domain.portfolio.repository;

import com.campuspolio.domain.portfolio.entity.Portfolio;
import com.campuspolio.domain.portfolio.entity.PortfolioProject;
import com.campuspolio.domain.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioProjectRepository extends JpaRepository<PortfolioProject, Long> {

    List<PortfolioProject> findAllByPortfolioOrderByDisplayOrderAsc(Portfolio portfolio);

    Optional<PortfolioProject> findByPortfolioAndProject(
            Portfolio portfolio,
            Project project
    );

    boolean existsByPortfolioAndProject(
            Portfolio portfolio,
            Project project
    );

    long countByPortfolio(Portfolio portfolio);

    void deleteByPortfolioAndProject(
            Portfolio portfolio,
            Project project
    );

    /*
     * 포트폴리오 삭제 시 프로젝트 자체는 유지하고,
     * 포트폴리오-프로젝트 연결만 제거한다.
     */
    void deleteAllByPortfolio(Portfolio portfolio);
}