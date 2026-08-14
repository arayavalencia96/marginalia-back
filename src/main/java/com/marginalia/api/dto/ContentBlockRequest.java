package com.marginalia.api.dto;

import com.marginalia.api.domain.ContentBlockType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Accepts type-specific data used to create or replace a content block.
 *
 * @param type content-block type
 * @param content text, code, formula, or exercise content when applicable
 * @param codeLanguage programming language for CODE blocks
 * @param orderIndex display order within the chapter
 * @param stepList nested list data for STEP_LIST blocks
 */
public record ContentBlockRequest(
        @NotNull ContentBlockType type,
        String content,
        @Size(max = 50) String codeLanguage,
        @Min(0) int orderIndex,
        @Valid StepListBlockRequest stepList
) {
}
