package com.marginalia.api.dto;

import com.marginalia.api.domain.ContentBlockType;

import java.util.UUID;

/**
 * Returns a typed content block and its applicable type-specific data.
 *
 * @param id content-block identifier
 * @param chapterId owning chapter identifier
 * @param type content-block type
 * @param content stored textual content
 * @param codeLanguage programming language for CODE blocks
 * @param resolved completion state for EXERCISE blocks
 * @param orderIndex display order within the chapter
 * @param stepList nested data for STEP_LIST blocks
 */
public record ContentBlockResponse(
        UUID id,
        UUID chapterId,
        ContentBlockType type,
        String content,
        String codeLanguage,
        boolean resolved,
        int orderIndex,
        StepListBlockResponse stepList
) {
}
