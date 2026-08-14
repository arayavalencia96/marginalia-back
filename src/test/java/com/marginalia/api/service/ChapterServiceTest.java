package com.marginalia.api.service;

import com.marginalia.api.domain.Book;
import com.marginalia.api.domain.Chapter;
import com.marginalia.api.dto.ChapterRequest;
import com.marginalia.api.exception.InvalidChapterParentException;
import com.marginalia.api.repository.ChapterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChapterServiceTest {

    @Mock
    private ChapterRepository repository;

    @Mock
    private ResourceOwnershipService ownershipService;

    @InjectMocks
    private ChapterService service;

    @Test
    void createsRootChapterInOwnedBook() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(ownershipService.requireOwnedBook(bookId, userId)).thenReturn(Book.builder().id(bookId).userId(userId).build());
        when(repository.save(any(Chapter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.create(bookId, new ChapterRequest("Introduction", null, 0), userId);

        assertThat(result.bookId()).isEqualTo(bookId);
        assertThat(result.title()).isEqualTo("Introduction");
    }

    @Test
    void createsChildWhenParentBelongsToBook() {
        UUID bookId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(repository.findByIdAndBookId(parentId, bookId))
                .thenReturn(Optional.of(Chapter.builder().id(parentId).bookId(bookId).build()));
        when(repository.save(any(Chapter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.create(bookId, new ChapterRequest("Child", parentId, 1), userId);

        assertThat(result.parentChapterId()).isEqualTo(parentId);
    }

    @Test
    void rejectsParentFromAnotherBook() {
        UUID bookId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        when(repository.findByIdAndBookId(parentId, bookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(bookId, new ChapterRequest("Child", parentId, 0), UUID.randomUUID()))
                .isInstanceOf(InvalidChapterParentException.class);
    }

    @Test
    void rejectsParentCycleDuringUpdate() {
        UUID id = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(ownershipService.requireOwnedChapter(id, userId))
                .thenReturn(Chapter.builder().id(id).bookId(bookId).build());

        assertThatThrownBy(() -> service.update(id, new ChapterRequest("Cycle", id, 0), userId))
                .isInstanceOf(InvalidChapterParentException.class);
    }

    @Test
    void listsAndDeletesOwnedChapters() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID chapterId = UUID.randomUUID();
        Chapter chapter = Chapter.builder().id(chapterId).bookId(bookId).title("Chapter").build();
        when(repository.findAllByBookIdOrderByOrderIndexAsc(bookId)).thenReturn(List.of(chapter));
        when(ownershipService.requireOwnedChapter(chapterId, userId)).thenReturn(chapter);

        assertThat(service.findAll(bookId, userId)).extracting(result -> result.id()).containsExactly(chapterId);
        service.delete(chapterId, userId);

        verify(repository).delete(chapter);
    }
}
