package com.marginalia.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/** Represents an ordered, typed annotation block belonging to a chapter. */
@Entity
@Table(name = "content_blocks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "chapter_id", nullable = false)
    private UUID chapterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ContentBlockType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String answer;

    @Enumerated(EnumType.STRING)
    @Column(name = "heading_level", length = 20)
    private HeadingLevel headingLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_style", length = 20)
    private StepStyle stepStyle;

    @Column(name = "code_language", length = 50)
    private String codeLanguage;

    @Builder.Default
    @Column(nullable = false)
    private boolean resolved = false;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;
}
