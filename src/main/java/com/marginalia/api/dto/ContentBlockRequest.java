package com.marginalia.api.dto;

import com.marginalia.api.domain.ContentBlockType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ContentBlockRequest(
        @NotNull ContentBlockType type,
        String content,
        @Size(max = 50) String codeLanguage,
        @Min(0) int orderIndex,
        @Valid StepListBlockRequest stepList
) {
}
