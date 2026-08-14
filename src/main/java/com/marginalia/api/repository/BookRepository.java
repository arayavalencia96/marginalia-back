package com.marginalia.api.repository;

import com.marginalia.api.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> {

    List<Book> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Book> findByIdAndUserId(UUID id, UUID userId);
}
