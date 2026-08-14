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

/** Resolves books, chapters, and content blocks while enforcing ownership through their aggregate hierarchy. */
@Service
@RequiredArgsConstructor
public class ResourceOwnershipService {

    private final BookRepository bookRepository;
    private final ChapterRepository chapterRepository;
    private final ContentBlockRepository contentBlockRepository;

    /**
     * Retrieves a book and verifies its owner.
     *
     * @param bookId identifier of the book
     * @param userId identifier of the expected owner
     * @return the owned book
     * @throws BookNotFoundException if the book does not exist
     * @throws ResourceAccessDeniedException if the user does not own the book
     */
    public Book requireOwnedBook(UUID bookId, UUID userId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
        if (!book.getUserId().equals(userId)) {
            throw new ResourceAccessDeniedException();
        }
        return book;
    }

    /**
     * Retrieves a chapter and verifies ownership through its book.
     *
     * @param chapterId identifier of the chapter
     * @param userId identifier of the expected owner
     * @return the owned chapter
     * @throws ChapterNotFoundException if the chapter does not exist
     * @throws ResourceAccessDeniedException if the user does not own its book
     */
    public Chapter requireOwnedChapter(UUID chapterId, UUID userId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ChapterNotFoundException(chapterId));
        requireOwnedBook(chapter.getBookId(), userId);
        return chapter;
    }

    /**
     * Retrieves a content block and verifies ownership through its chapter and book.
     *
     * @param blockId identifier of the content block
     * @param userId identifier of the expected owner
     * @return the owned content block
     * @throws ContentBlockNotFoundException if the block does not exist
     * @throws ResourceAccessDeniedException if the user does not own its book
     */
    public ContentBlock requireOwnedContentBlock(UUID blockId, UUID userId) {
        ContentBlock contentBlock = contentBlockRepository.findById(blockId)
                .orElseThrow(() -> new ContentBlockNotFoundException(blockId));
        requireOwnedChapter(contentBlock.getChapterId(), userId);
        return contentBlock;
    }
}
