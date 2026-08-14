package com.marginalia.api.service;

import com.marginalia.api.domain.Attachment;
import com.marginalia.api.domain.Book;
import com.marginalia.api.domain.Chapter;
import com.marginalia.api.domain.ContentBlock;
import com.marginalia.api.domain.ContentBlockStep;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Aggregates all owned book data required to render a PDF export.
 *
 * @param book book being exported
 * @param chapters ordered chapters in the book
 * @param blocksByChapter content blocks grouped by chapter identifier
 * @param stepsByBlock step-list entries grouped by block identifier
 * @param attachmentsByBlock image attachments grouped by block identifier
 */
public record BookExportData(
        Book book,
        List<Chapter> chapters,
        Map<UUID, List<ContentBlock>> blocksByChapter,
        Map<UUID, List<ContentBlockStep>> stepsByBlock,
        Map<UUID, List<Attachment>> attachmentsByBlock
) {
}
