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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookExportDataLoaderTest {

    @Mock
    private ResourceOwnershipService ownershipService;

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private ContentBlockRepository blockRepository;

    @Mock
    private ContentBlockStepRepository stepRepository;

    @Mock
    private AttachmentRepository attachmentRepository;

    @InjectMocks
    private BookExportDataLoader loader;

    @Test
    void loadsAndGroupsCompleteBookData() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID chapterId = UUID.randomUUID();
        UUID blockId = UUID.randomUUID();
        Book book = Book.builder().id(bookId).userId(userId).build();
        Chapter chapter = Chapter.builder().id(chapterId).bookId(bookId).orderIndex(0).build();
        ContentBlock block = ContentBlock.builder().id(blockId).chapterId(chapterId).orderIndex(0).build();
        ContentBlockStep step = ContentBlockStep.builder().contentBlockId(blockId).stepOrder(0).build();
        Attachment attachment = Attachment.builder()
                .id(UUID.randomUUID()).contentBlockId(blockId).createdAt(Instant.now()).build();
        when(ownershipService.requireOwnedBook(bookId, userId)).thenReturn(book);
        when(chapterRepository.findAllByBookIdOrderByOrderIndexAsc(bookId)).thenReturn(List.of(chapter));
        when(blockRepository.findAllByBookId(bookId)).thenReturn(List.of(block));
        when(stepRepository.findAllByContentBlockIdInOrderByContentBlockIdAscStepOrderAsc(List.of(blockId)))
                .thenReturn(List.of(step));
        when(attachmentRepository.findAllByContentBlockIdIn(List.of(blockId))).thenReturn(List.of(attachment));

        BookExportData data = loader.load(bookId, userId);

        assertThat(data.book()).isSameAs(book);
        assertThat(data.blocksByChapter().get(chapterId)).containsExactly(block);
        assertThat(data.stepsByBlock().get(blockId)).containsExactly(step);
        assertThat(data.attachmentsByBlock().get(blockId)).containsExactly(attachment);
    }

    @Test
    void skipsChildQueriesWhenBookHasNoBlocks() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(ownershipService.requireOwnedBook(bookId, userId)).thenReturn(Book.builder().id(bookId).build());
        when(chapterRepository.findAllByBookIdOrderByOrderIndexAsc(bookId)).thenReturn(List.of());
        when(blockRepository.findAllByBookId(bookId)).thenReturn(List.of());

        BookExportData data = loader.load(bookId, userId);

        assertThat(data.stepsByBlock()).isEmpty();
        assertThat(data.attachmentsByBlock()).isEmpty();
        verifyNoInteractions(stepRepository, attachmentRepository);
    }
}
