package com.marginalia.api.dto;

import java.time.Instant;
import java.util.UUID;

public record AttachmentResponse(
        UUID id,
        UUID contentBlockId,
        String url,
        long sizeBytes,
        Instant createdAt
) {
}
