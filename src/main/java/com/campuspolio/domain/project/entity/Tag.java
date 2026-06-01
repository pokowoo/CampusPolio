package com.campuspolio.domain.project.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "tag")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long id;

    @Column(
            name = "tag_name",
            nullable = false,
            unique = true,
            length = 10
    )
    private String tagName;

    private Tag(String tagName) {
        this.tagName = tagName;
    }

    public static Tag create(String tagName) {
        return new Tag(tagName);
    }
}