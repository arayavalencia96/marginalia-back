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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceOwnershipService {

    private final BookRepository bookRepository;
    private final ChapterRepository chapterRepository;
    private final ContentBlockRepository contentBlockRepository;

    public Book requireOwnedBook(UUID bookId, UUID userId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
        if (!book.getUserId().equals(userId)) {
            throw new ResourceAccessDeniedException();
        }
        return book;
    }

    public Chapter requireOwnedChapter(UUID chapterId, UUID userId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ChapterNotFoundException(chapterId));
        requireOwnedBook(chapter.getBookId(), userId);
        return chapter;
    }

    public ContentBlock requireOwnedContentBlock(UUID blockId, UUID userId) {
        ContentBlock contentBlock = contentBlockRepository.findById(blockId)
                .orElseThrow(() -> new ContentBlockNotFoundException(blockId));
        requireOwnedChapter(contentBlock.getChapterId(), userId);
        return contentBlock;
    }
}
