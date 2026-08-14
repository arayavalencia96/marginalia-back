package com.marginalia.api.service;

import com.marginalia.api.domain.Attachment;
import com.marginalia.api.domain.Book;
import com.marginalia.api.domain.Chapter;
import com.marginalia.api.domain.ContentBlock;
import com.marginalia.api.domain.ContentBlockStep;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record BookExportData(
        Book book,
        List<Chapter> chapters,
        Map<UUID, List<ContentBlock>> blocksByChapter,
        Map<UUID, List<ContentBlockStep>> stepsByBlock,
        Map<UUID, List<Attachment>> attachmentsByBlock
) {
}
