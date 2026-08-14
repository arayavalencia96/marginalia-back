package com.marginalia.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record AttachmentRequest(
        @NotBlank @Size(max = 2048) String url,
        @PositiveOrZero long sizeBytes
) {
}
