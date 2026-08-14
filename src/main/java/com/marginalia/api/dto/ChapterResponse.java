package com.marginalia.api.dto;

import java.util.UUID;

public record ChapterResponse(
        UUID id,
        UUID bookId,
        String title,
        UUID parentChapterId,
        int orderIndex
) {
}
