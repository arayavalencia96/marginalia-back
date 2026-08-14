package com.marginalia.api.service;

import com.marginalia.api.domain.Book;
import com.marginalia.api.domain.Chapter;
import com.marginalia.api.domain.ContentBlock;
import com.marginalia.api.exception.BookNotFoundException;
import com.marginalia.api.exception.ChapterNotFoundException;
import com.marginalia.api.exception.ContentBlockNotFoundException;
import com.marginalia.api.exception.ResourceAccessDeniedException;
import com.marginalia.api.repository.BookRepository;
import com.marginalia.api.repository.ChapterRepository;
import com.marginalia.api.repository.ContentBlockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceOwnershipServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private ContentBlockRepository contentBlockRepository;

    @InjectMocks
    private ResourceOwnershipService service;

    @Test
    void returnsOwnedBook() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Book book = Book.builder().id(bookId).userId(userId).build();
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        assertThat(service.requireOwnedBook(bookId, userId)).isSameAs(book);
    }

    @Test
    void rejectsBookOwnedByAnotherUser() {
        UUID bookId = UUID.randomUUID();
        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(Book.builder().id(bookId).userId(UUID.randomUUID()).build()));

        assertThatThrownBy(() -> service.requireOwnedBook(bookId, UUID.randomUUID()))
                .isInstanceOf(ResourceAccessDeniedException.class);
    }

    @Test
    void rejectsMissingBook() {
        UUID bookId = UUID.randomUUID();
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.requireOwnedBook(bookId, UUID.randomUUID()))
                .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    void resolvesChapterThroughOwnedBook() {
        UUID chapterId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Chapter chapter = Chapter.builder().id(chapterId).bookId(bookId).build();
        when(chapterRepository.findById(chapterId)).thenReturn(Optional.of(chapter));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(Book.builder().id(bookId).userId(userId).build()));

        assertThat(service.requireOwnedChapter(chapterId, userId)).isSameAs(chapter);
    }

    @Test
    void rejectsMissingChapter() {
        UUID chapterId = UUID.randomUUID();
        when(chapterRepository.findById(chapterId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.requireOwnedChapter(chapterId, UUID.randomUUID()))
                .isInstanceOf(ChapterNotFoundException.class);
    }

    @Test
    void resolvesBlockThroughOwnedHierarchy() {
        UUID blockId = UUID.randomUUID();
        UUID chapterId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ContentBlock block = ContentBlock.builder().id(blockId).chapterId(chapterId).build();
        when(contentBlockRepository.findById(blockId)).thenReturn(Optional.of(block));
        when(chapterRepository.findById(chapterId)).thenReturn(Optional.of(Chapter.builder().id(chapterId).bookId(bookId).build()));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(Book.builder().id(bookId).userId(userId).build()));

        assertThat(service.requireOwnedContentBlock(blockId, userId)).isSameAs(block);
    }

    @Test
    void rejectsMissingContentBlock() {
        UUID blockId = UUID.randomUUID();
        when(contentBlockRepository.findById(blockId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.requireOwnedContentBlock(blockId, UUID.randomUUID()))
                .isInstanceOf(ContentBlockNotFoundException.class);
    }
}
