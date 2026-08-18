package com.marginalia.api.service;

import com.marginalia.api.domain.Book;
import com.marginalia.api.domain.BookTopic;
import com.marginalia.api.dto.BookRequest;
import com.marginalia.api.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ResourceOwnershipService resourceOwnershipService;

    @InjectMocks
    private BookService bookService;

    @Test
    void createPersistsOwnedBook() {
        UUID userId = UUID.randomUUID();
        Book saved = book(UUID.randomUUID(), userId);
        when(bookRepository.save(org.mockito.ArgumentMatchers.any(Book.class))).thenReturn(saved);

        var response = bookService.create(new BookRequest("Effective Java", "Joshua Bloch", BookTopic.PROGRAMMING), userId);

        assertThat(response.id()).isEqualTo(saved.getId());
        assertThat(response.userId()).isEqualTo(userId);
        verify(bookRepository).save(org.mockito.ArgumentMatchers.argThat(book -> book.getUserId().equals(userId)));
    }

    @Test
    void findAllMapsOwnedBooks() {
        UUID userId = UUID.randomUUID();
        when(bookRepository.findAllByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(book(UUID.randomUUID(), userId)));

        assertThat(bookService.findAll(userId)).hasSize(1).allMatch(book -> book.userId().equals(userId));
    }

    @Test
    void findByIdUsesOwnershipGuard() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(resourceOwnershipService.requireOwnedBook(id, userId)).thenReturn(book(id, userId));

        assertThat(bookService.findById(id, userId).id()).isEqualTo(id);
    }

    @Test
    void updateChangesOwnedBook() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Book ownedBook = book(id, userId);
        when(resourceOwnershipService.requireOwnedBook(id, userId)).thenReturn(ownedBook);

        var response = bookService.update(
                id,
                new BookRequest("The Psychology of Money", "Morgan Housel", BookTopic.FINANCE_INVESTING),
                userId
        );

        assertThat(response.title()).isEqualTo("The Psychology of Money");
        assertThat(response.author()).isEqualTo("Morgan Housel");
        assertThat(response.topic()).isEqualTo(BookTopic.FINANCE_INVESTING);
    }

    @Test
    void deleteRemovesOwnedBook() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Book book = book(id, userId);
        when(resourceOwnershipService.requireOwnedBook(id, userId)).thenReturn(book);

        bookService.delete(id, userId);

        verify(bookRepository).delete(book);
    }

    private Book book(UUID id, UUID userId) {
        return Book.builder()
                .id(id)
                .title("Effective Java")
                .author("Joshua Bloch")
                .topic(BookTopic.PROGRAMMING)
                .userId(userId)
                .createdAt(Instant.now())
                .build();
    }
}
