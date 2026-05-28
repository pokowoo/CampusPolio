package com.campuspolio.domain.project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long tagId;

    @Column(name = "tag_key", nullable = false, unique = true, length = 10)
    private String tagKey;

    public static Tag create(String normalizedTag) {
        return Tag.builder()
                .tagKey(normalizedTag)
                .build();
    }
}