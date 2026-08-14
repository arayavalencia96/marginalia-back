package com.marginalia.api.repository;

import com.marginalia.api.domain.ContentBlockStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/** Provides persistence and ordered queries for step-list entries. */
public interface ContentBlockStepRepository extends JpaRepository<ContentBlockStep, UUID> {

    /**
     * Lists a block's steps in display order.
     *
     * @param contentBlockId content-block identifier
     * @return ordered steps
     */
    List<ContentBlockStep> findAllByContentBlockIdOrderByStepOrderAsc(UUID contentBlockId);

    /**
     * Lists steps for multiple blocks, grouped by block order and step order.
     *
     * @param contentBlockIds content-block identifiers
     * @return ordered matching steps
     */
    List<ContentBlockStep> findAllByContentBlockIdInOrderByContentBlockIdAscStepOrderAsc(
            Collection<UUID> contentBlockIds
    );

    /**
     * Deletes every step belonging to a content block.
     *
     * @param contentBlockId content-block identifier
     */
    @Modifying
    @Query("delete from ContentBlockStep step where step.contentBlockId = :contentBlockId")
    void deleteAllByContentBlockId(@Param("contentBlockId") UUID contentBlockId);
}
