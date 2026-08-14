package com.marginalia.api.repository;

import com.marginalia.api.domain.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChapterRepository extends JpaRepository<Chapter, UUID> {

    List<Chapter> findAllByBookIdOrderByOrderIndexAsc(UUID bookId);

    Optional<Chapter> findByIdAndBookId(UUID id, UUID bookId);
}
