package com.marginalia.api.repository;

import com.marginalia.api.domain.ContentBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ContentBlockRepository extends JpaRepository<ContentBlock, UUID> {

    List<ContentBlock> findAllByChapterIdOrderByOrderIndexAsc(UUID chapterId);

    @Query("select block from ContentBlock block "
            + "where block.chapterId in "
            + "(select chapter.id from Chapter chapter where chapter.bookId = :bookId) "
            + "order by block.chapterId, block.orderIndex, block.id")
    List<ContentBlock> findAllByBookId(@Param("bookId") UUID bookId);

    @Query("select count(block) from ContentBlock block "
            + "where block.chapterId in "
            + "(select chapter.id from Chapter chapter where chapter.bookId = :bookId)")
    long countByBookId(@Param("bookId") UUID bookId);
}
