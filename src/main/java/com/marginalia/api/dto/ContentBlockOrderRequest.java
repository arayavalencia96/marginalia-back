package com.marginalia.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Identifies the final position of a content block within its chapter. */
public record ContentBlockOrderRequest(
        @NotNull UUID blockId,
        @Min(0) int orderIndex
) {
}
