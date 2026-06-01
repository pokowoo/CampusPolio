package com.campuspolio.domain.project.specification;

import com.campuspolio.domain.project.entity.Project;
import com.campuspolio.domain.project.entity.ProjectStatus;
import com.campuspolio.domain.project.entity.ProjectTag;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

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

    // ==========================
    // 추가
    // ==========================

    public static Specification<Project> hasTags(
            List<String> tags
    ) {

        return (root, query, cb) -> {

            if (tags == null || tags.isEmpty()) {
                return cb.conjunction();
            }

            Join<Project, ProjectTag> projectTagJoin =
                    root.join("projectTags");

            query.distinct(true);

            return projectTagJoin
                    .get("tag")
                    .get("tagName")
                    .in(tags);
        };
    }
}