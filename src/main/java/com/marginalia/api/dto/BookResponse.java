package com.marginalia.api.dto;

import com.marginalia.api.domain.BookTopic;

import java.time.Instant;
import java.util.UUID;

public record BookResponse(
        UUID id,
        String title,
        String author,
        BookTopic topic,
        UUID userId,
        Instant createdAt
) {
}
