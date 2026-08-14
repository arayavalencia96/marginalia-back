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

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final ResourceOwnershipService resourceOwnershipService;

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

    @Transactional(readOnly = true)
    public List<BookResponse> findAll(UUID userId) {
        return bookRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookResponse findById(UUID id, UUID userId) {
        return toResponse(findOwnedBook(id, userId));
    }

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
