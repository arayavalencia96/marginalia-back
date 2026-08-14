package com.marginalia.api.repository;

import com.marginalia.api.domain.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {

    List<Attachment> findAllByContentBlockIdIn(Collection<UUID> contentBlockIds);
}
