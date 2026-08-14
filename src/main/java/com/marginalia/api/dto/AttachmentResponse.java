package com.marginalia.api.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Returns persisted image attachment metadata.
 *
 * @param id attachment identifier
 * @param contentBlockId owning content-block identifier
 * @param url secure image URL
 * @param sizeBytes uploaded image size in bytes
 * @param createdAt attachment creation time
 */
public record AttachmentResponse(
        UUID id,
        UUID contentBlockId,
        String url,
        long sizeBytes,
        Instant createdAt
) {
}
