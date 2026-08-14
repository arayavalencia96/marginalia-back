package com.marginalia.api.dto;

import java.util.UUID;

/**
 * Returns a chapter in flat form for client-side tree construction.
 *
 * @param id chapter identifier
 * @param bookId owning book identifier
 * @param title chapter title
 * @param parentChapterId optional parent chapter identifier
 * @param orderIndex display order among sibling chapters
 */
public record ChapterResponse(
        UUID id,
        UUID bookId,
        String title,
        UUID parentChapterId,
        int orderIndex
) {
}
