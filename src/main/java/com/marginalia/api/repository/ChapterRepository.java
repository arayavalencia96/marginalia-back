package com.marginalia.api.repository;

import com.marginalia.api.domain.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Provides persistence and ordered hierarchy queries for chapters. */
public interface ChapterRepository extends JpaRepository<Chapter, UUID> {

    /**
     * Lists a book's chapters ordered by their sibling order index.
     *
     * @param bookId book identifier
     * @return ordered chapters
     */
    List<Chapter> findAllByBookIdOrderByOrderIndexAsc(UUID bookId);

    /**
     * Finds a chapter only when it belongs to the supplied book.
     *
     * @param id chapter identifier
     * @param bookId book identifier
     * @return matching chapter, if present
     */
    Optional<Chapter> findByIdAndBookId(UUID id, UUID bookId);
}
