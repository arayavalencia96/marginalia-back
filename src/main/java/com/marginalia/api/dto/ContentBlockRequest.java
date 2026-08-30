package com.marginalia.api.dto;

import com.marginalia.api.domain.ContentBlockType;
import com.marginalia.api.domain.HeadingLevel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Accepts type-specific data used to create or replace a content block.
 *
 * @param type content-block type
 * @param content text, heading, code, formula, exercise, or question when applicable
 * @param answer answer for a question-and-answer block
 * @param description optional context for code, formula, exercise, or image blocks
 * @param headingLevel title or subtitle level for heading blocks
 * @param codeLanguage programming language for CODE blocks
 * @param orderIndex display order within the chapter
 * @param stepList nested list data for STEP_LIST blocks
 */
public record ContentBlockRequest(
        @NotNull ContentBlockType type,
        String content,
        String answer,
        @Size(max = 2000) String description,
        HeadingLevel headingLevel,
        @Size(max = 50) String codeLanguage,
        @Min(0) int orderIndex,
        @Valid StepListBlockRequest stepList
) {
}
