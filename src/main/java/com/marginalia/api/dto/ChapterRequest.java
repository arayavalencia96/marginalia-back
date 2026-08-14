package com.marginalia.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ChapterRequest(
        @NotBlank @Size(max = 255) String title,
        UUID parentChapterId,
        @Min(0) int orderIndex
) {
}
