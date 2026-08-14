package com.marginalia.api.repository;

import com.marginalia.api.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Provides persistence and ownership-oriented queries for books. */
public interface BookRepository extends JpaRepository<Book, UUID> {

    /**
     * Lists a user's books in reverse creation order.
     *
     * @param userId owner identifier
     * @return owned books
     */
    List<Book> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Finds a book only when it belongs to the supplied user.
     *
     * @param id book identifier
     * @param userId owner identifier
     * @return matching book, if present
     */
    Optional<Book> findByIdAndUserId(UUID id, UUID userId);
}
