package com.marginalia.api.dto;

import com.marginalia.api.domain.BookTopic;

import java.time.Instant;
import java.util.UUID;

/**
 * Returns a book owned by a user.
 *
 * @param id book identifier
 * @param title book title
 * @param author book author
 * @param topic book topic category
 * @param userId owner identifier
 * @param createdAt book creation time
 */
public record BookResponse(
        UUID id,
        String title,
        String author,
        BookTopic topic,
        UUID userId,
        Instant createdAt
) {
}
