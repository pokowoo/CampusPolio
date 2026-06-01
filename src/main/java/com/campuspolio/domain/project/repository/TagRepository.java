package com.campuspolio.domain.project.repository;

import com.campuspolio.domain.project.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository
        extends JpaRepository<Tag, Long> {

    Optional<Tag> findByTagName(
            String tagName
    );
}