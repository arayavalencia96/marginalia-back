package com.marginalia.api.repository;

import com.marginalia.api.domain.ContentBlockStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ContentBlockStepRepository extends JpaRepository<ContentBlockStep, UUID> {

    List<ContentBlockStep> findAllByContentBlockIdOrderByStepOrderAsc(UUID contentBlockId);

    List<ContentBlockStep> findAllByContentBlockIdInOrderByContentBlockIdAscStepOrderAsc(
            Collection<UUID> contentBlockIds
    );

    @Modifying
    @Query("delete from ContentBlockStep step where step.contentBlockId = :contentBlockId")
    void deleteAllByContentBlockId(@Param("contentBlockId") UUID contentBlockId);
}
