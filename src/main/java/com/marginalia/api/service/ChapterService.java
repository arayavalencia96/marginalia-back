package com.marginalia.api.service;

import com.marginalia.api.domain.Book;
import com.marginalia.api.domain.Chapter;
import com.marginalia.api.dto.ChapterRequest;
import com.marginalia.api.dto.ChapterResponse;
import com.marginalia.api.exception.BookNotFoundException;
import com.marginalia.api.exception.ChapterNotFoundException;
import com.marginalia.api.exception.InvalidChapterParentException;
import com.marginalia.api.repository.BookRepository;
import com.marginalia.api.repository.ChapterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChapterService {

    private final ChapterRepository chapterRepository;
    private final BookRepository bookRepository;

    @Transactional
    public ChapterResponse create(UUID bookId, ChapterRequest request, UUID userId) {
        requireOwnedBook(bookId, userId);
        validateParent(bookId, request.parentChapterId(), null);

        Chapter chapter = Chapter.builder()
                .bookId(bookId)
                .title(request.title())
                .parentChapterId(request.parentChapterId())
                .orderIndex(request.orderIndex())
                .build();

        return toResponse(chapterRepository.save(chapter));
    }

    @Transactional(readOnly = true)
    public List<ChapterResponse> findAll(UUID bookId, UUID userId) {
        requireOwnedBook(bookId, userId);

        return chapterRepository.findAllByBookIdOrderByOrderIndexAsc(bookId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ChapterResponse update(UUID id, ChapterRequest request, UUID userId) {
        Chapter chapter = findOwnedChapter(id, userId);
        validateParent(chapter.getBookId(), request.parentChapterId(), id);

        chapter.setTitle(request.title());
        chapter.setParentChapterId(request.parentChapterId());
        chapter.setOrderIndex(request.orderIndex());

        return toResponse(chapterRepository.save(chapter));
    }

    @Transactional
    public void delete(UUID id, UUID userId) {
        chapterRepository.delete(findOwnedChapter(id, userId));
    }

    private Chapter findOwnedChapter(UUID id, UUID userId) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new ChapterNotFoundException(id));
        requireOwnedBook(chapter.getBookId(), userId);
        return chapter;
    }

    private Book requireOwnedBook(UUID bookId, UUID userId) {
        return bookRepository.findByIdAndUserId(bookId, userId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
    }

    private void validateParent(UUID bookId, UUID parentChapterId, UUID chapterId) {
        if (parentChapterId == null) {
            return;
        }

        Set<UUID> visitedChapterIds = new HashSet<>();
        UUID currentChapterId = parentChapterId;

        while (currentChapterId != null) {
            if (currentChapterId.equals(chapterId) || !visitedChapterIds.add(currentChapterId)) {
                throw new InvalidChapterParentException("The parent chapter creates a cycle");
            }

            Chapter currentChapter = chapterRepository.findByIdAndBookId(currentChapterId, bookId)
                    .orElseThrow(() -> new InvalidChapterParentException(
                            "Parent chapter must belong to the same book"
                    ));
            currentChapterId = currentChapter.getParentChapterId();
        }
    }

    private ChapterResponse toResponse(Chapter chapter) {
        return new ChapterResponse(
                chapter.getId(),
                chapter.getBookId(),
                chapter.getTitle(),
                chapter.getParentChapterId(),
                chapter.getOrderIndex()
        );
    }
}
