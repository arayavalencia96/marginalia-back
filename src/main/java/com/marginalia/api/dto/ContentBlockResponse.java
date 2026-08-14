package com.marginalia.api.dto;

import com.marginalia.api.domain.ContentBlockType;

import java.util.UUID;

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
