package com.marginalia.api.repository;

import com.marginalia.api.domain.ContentBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/** Provides persistence and ordered aggregate queries for content blocks. */
public interface ContentBlockRepository extends JpaRepository<ContentBlock, UUID> {

    @Modifying
    @Query("update ContentBlock block set block.orderIndex = block.orderIndex + 1 "
            + "where block.chapterId = :chapterId and block.orderIndex >= :orderIndex")
    void shiftOrderIndexesForInsert(
            @Param("chapterId") UUID chapterId,
            @Param("orderIndex") int orderIndex
    );

    /**
     * Lists a chapter's blocks in display order.
     *
     * @param chapterId chapter identifier
     * @return ordered content blocks
     */
    List<ContentBlock> findAllByChapterIdOrderByOrderIndexAsc(UUID chapterId);

    /**
     * Lists all blocks in a book ordered by chapter and block position.
     *
     * @param bookId book identifier
     * @return ordered content blocks across the book
     */
    @Query("select block from ContentBlock block "
            + "where block.chapterId in "
            + "(select chapter.id from Chapter chapter where chapter.bookId = :bookId) "
            + "order by block.chapterId, block.orderIndex, block.id")
    List<ContentBlock> findAllByBookId(@Param("bookId") UUID bookId);

    /**
     * Counts every content block in a book.
     *
     * @param bookId book identifier
     * @return total content-block count
     */
    @Query("select count(block) from ContentBlock block "
            + "where block.chapterId in "
            + "(select chapter.id from Chapter chapter where chapter.bookId = :bookId)")
    long countByBookId(@Param("bookId") UUID bookId);
}
