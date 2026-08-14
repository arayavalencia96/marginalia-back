package com.marginalia.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Accepts data used to create or replace a chapter.
 *
 * @param title chapter title
 * @param parentChapterId optional parent chapter identifier
 * @param orderIndex display order among sibling chapters
 */
public record ChapterRequest(
        @NotBlank @Size(max = 255) String title,
        UUID parentChapterId,
        @Min(0) int orderIndex
) {
}
