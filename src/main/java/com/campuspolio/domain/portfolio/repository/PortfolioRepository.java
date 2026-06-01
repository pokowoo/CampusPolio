package com.campuspolio.domain.portfolio.repository;

import com.campuspolio.domain.portfolio.entity.Portfolio;
import com.campuspolio.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    Optional<Portfolio> findByPortfolioIdAndDeletedAtIsNull(Long portfolioId);

    Optional<Portfolio> findBySlugAndDeletedAtIsNull(String slug);

    Page<Portfolio> findAllByUserAndDeletedAtIsNull(
            User user,
            Pageable pageable
    );

    Page<Portfolio> findAllByUserAndIsPublicAndDeletedAtIsNull(
            User user,
            boolean isPublic,
            Pageable pageable
    );

    /*
     * 정책:
     * 삭제된 slug도 재사용 불가.
     * 따라서 deletedAt 여부와 관계없이 slug 존재 여부를 검사한다.
     */
    boolean existsBySlug(String slug);
}