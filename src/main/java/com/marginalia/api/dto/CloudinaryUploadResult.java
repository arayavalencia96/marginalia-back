package com.marginalia.api.dto;

public record CloudinaryUploadResult(
        String secureUrl,
        long sizeBytes
) {
}
