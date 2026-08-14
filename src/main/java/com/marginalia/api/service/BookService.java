package com.marginalia.api.service;

import com.marginalia.api.domain.Book;
import com.marginalia.api.dto.BookRequest;
import com.marginalia.api.dto.BookResponse;
import com.marginalia.api.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Implements owned book creation, retrieval, listing, and deletion. */
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final ResourceOwnershipService resourceOwnershipService;

    /**
     * Creates a book belonging to a user.
     *
     * @param request book data
     * @param userId identifier of the owner
     * @return the created book
     */
    @Transactional
    public BookResponse create(BookRequest request, UUID userId) {
        Book book = Book.builder()
                .title(request.title())
                .author(request.author())
                .topic(request.topic())
                .userId(userId)
                .build();

        return toResponse(bookRepository.save(book));
    }

    /**
     * Lists a user's books in reverse creation order.
     *
     * @param userId identifier of the owner
     * @return the user's books
     */
    @Transactional(readOnly = true)
    public List<BookResponse> findAll(UUID userId) {
        return bookRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Retrieves an owned book.
     *
     * @param id identifier of the book
     * @param userId identifier of the expected owner
     * @return the requested book
     * @throws com.marginalia.api.exception.BookNotFoundException if the book does not exist
     * @throws com.marginalia.api.exception.ResourceAccessDeniedException if the user does not own the book
     */
    @Transactional(readOnly = true)
    public BookResponse findById(UUID id, UUID userId) {
        return toResponse(findOwnedBook(id, userId));
    }

    /**
     * Deletes an owned book and its database-cascaded children.
     *
     * @param id identifier of the book
     * @param userId identifier of the expected owner
     * @throws com.marginalia.api.exception.BookNotFoundException if the book does not exist
     * @throws com.marginalia.api.exception.ResourceAccessDeniedException if the user does not own the book
     */
    @Transactional
    public void delete(UUID id, UUID userId) {
        bookRepository.delete(findOwnedBook(id, userId));
    }

    private Book findOwnedBook(UUID id, UUID userId) {
        return resourceOwnershipService.requireOwnedBook(id, userId);
    }

    private BookResponse toResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getTopic(),
                book.getUserId(),
                book.getCreatedAt()
        );
    }
}
