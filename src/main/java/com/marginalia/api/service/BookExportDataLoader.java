package com.marginalia.api.service;

import com.marginalia.api.domain.Attachment;
import com.marginalia.api.domain.Book;
import com.marginalia.api.domain.Chapter;
import com.marginalia.api.domain.ContentBlock;
import com.marginalia.api.domain.ContentBlockStep;
import com.marginalia.api.repository.AttachmentRepository;
import com.marginalia.api.repository.ChapterRepository;
import com.marginalia.api.repository.ContentBlockRepository;
import com.marginalia.api.repository.ContentBlockStepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/** Loads and groups the complete owned book graph needed by the PDF renderer. */
@Service
@RequiredArgsConstructor
public class BookExportDataLoader {

    private final ResourceOwnershipService resourceOwnershipService;
    private final ChapterRepository chapterRepository;
    private final ContentBlockRepository contentBlockRepository;
    private final ContentBlockStepRepository contentBlockStepRepository;
    private final AttachmentRepository attachmentRepository;

    /**
     * Loads an owned book with its chapters, blocks, steps, and attachments.
     *
     * @param bookId identifier of the book to load
     * @param userId identifier of the expected owner
     * @return grouped export data for the book
     * @throws com.marginalia.api.exception.BookNotFoundException if the book does not exist
     * @throws com.marginalia.api.exception.ResourceAccessDeniedException if the user does not own the book
     */
    @Transactional(readOnly = true)
    public BookExportData load(UUID bookId, UUID userId) {
        Book book = resourceOwnershipService.requireOwnedBook(bookId, userId);
        List<Chapter> chapters = new ArrayList<>(chapterRepository.findAllByBookIdOrderByOrderIndexAsc(bookId));
        chapters.sort(Comparator.comparingInt(Chapter::getOrderIndex).thenComparing(Chapter::getId));
        List<ContentBlock> blocks = contentBlockRepository.findAllByBookId(bookId);
        List<UUID> blockIds = blocks.stream().map(ContentBlock::getId).toList();

        Map<UUID, List<ContentBlock>> blocksByChapter = blocks.stream()
                .collect(Collectors.groupingBy(ContentBlock::getChapterId));
        return new BookExportData(
                book,
                chapters,
                blocksByChapter,
                loadSteps(blockIds),
                loadAttachments(blockIds)
        );
    }

    private Map<UUID, List<ContentBlockStep>> loadSteps(List<UUID> blockIds) {
        if (blockIds.isEmpty()) {
            return Map.of();
        }
        return contentBlockStepRepository
                .findAllByContentBlockIdInOrderByContentBlockIdAscStepOrderAsc(blockIds)
                .stream()
                .collect(Collectors.groupingBy(ContentBlockStep::getContentBlockId));
    }

    private Map<UUID, List<Attachment>> loadAttachments(List<UUID> blockIds) {
        if (blockIds.isEmpty()) {
            return Map.of();
        }
        return attachmentRepository.findAllByContentBlockIdIn(blockIds).stream()
                .sorted(Comparator.comparing(Attachment::getCreatedAt).thenComparing(Attachment::getId))
                .collect(Collectors.groupingBy(Attachment::getContentBlockId));
    }
}
