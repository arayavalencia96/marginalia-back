package com.marginalia.api.repository;

import com.marginalia.api.domain.ContentBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContentBlockRepository extends JpaRepository<ContentBlock, UUID> {

    List<ContentBlock> findAllByChapterIdOrderByOrderIndexAsc(UUID chapterId);
}
