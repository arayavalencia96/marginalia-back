package com.marginalia.api.repository;

import com.marginalia.api.domain.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/** Provides persistence operations for image attachment metadata. */
public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {

    /**
     * Finds attachments belonging to any supplied content block.
     *
     * @param contentBlockIds content-block identifiers
     * @return matching attachments
     */
    List<Attachment> findAllByContentBlockIdIn(Collection<UUID> contentBlockIds);
}
