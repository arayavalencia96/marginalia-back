package com.marginalia.api.service;

import com.marginalia.api.domain.Attachment;
import com.marginalia.api.domain.ContentBlock;
import com.marginalia.api.domain.ContentBlockType;
import com.marginalia.api.dto.AttachmentRequest;
import com.marginalia.api.dto.AttachmentResponse;
import com.marginalia.api.exception.InvalidContentBlockException;
import com.marginalia.api.repository.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final ResourceOwnershipService resourceOwnershipService;

    @Transactional
    public AttachmentResponse create(UUID blockId, AttachmentRequest request, UUID userId) {
        ContentBlock contentBlock = requireOwnedContentBlock(blockId, userId);
        if (contentBlock.getType() != ContentBlockType.IMAGE) {
            throw new InvalidContentBlockException("Attachments can only be added to IMAGE blocks");
        }

        Attachment attachment = Attachment.builder()
                .contentBlockId(blockId)
                .url(request.url())
                .sizeBytes(request.sizeBytes())
                .build();

        return toResponse(attachmentRepository.save(attachment));
    }

    private ContentBlock requireOwnedContentBlock(UUID blockId, UUID userId) {
        return resourceOwnershipService.requireOwnedContentBlock(blockId, userId);
    }

    private AttachmentResponse toResponse(Attachment attachment) {
        return new AttachmentResponse(
                attachment.getId(),
                attachment.getContentBlockId(),
                attachment.getUrl(),
                attachment.getSizeBytes(),
                attachment.getCreatedAt()
        );
    }
}
