package com.campuspolio.domain.project.specification;

import com.campuspolio.domain.project.entity.Project;
import com.campuspolio.domain.project.entity.ProjectStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class ProjectSpecification {

    public static Specification<Project> publishedOnly() {

        return (root, query, cb) -> cb.and(
                cb.equal(
                        root.get("status"),
                        ProjectStatus.PUBLISHED
                ),
                cb.isTrue(
                        root.get("isPublic")
                ),
                cb.isNull(
                        root.get("deletedAt")
                )
        );
    }

    public static Specification<Project> titleContains(
            String keyword
    ) {

        return (root, query, cb) -> {

            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }

            return cb.like(
                    cb.lower(root.get("title")),
                    "%" + keyword.toLowerCase() + "%"
            );
        };
    }
}