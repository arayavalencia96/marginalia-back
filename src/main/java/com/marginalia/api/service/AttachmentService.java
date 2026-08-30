package com.marginalia.api.service;

import com.marginalia.api.domain.Attachment;
import com.marginalia.api.domain.ContentBlock;
import com.marginalia.api.domain.ContentBlockType;
import com.marginalia.api.dto.AttachmentResponse;
import com.marginalia.api.dto.CloudinaryUploadResult;
import com.marginalia.api.exception.AttachmentTooLargeException;
import com.marginalia.api.exception.AttachmentNotFoundException;
import com.marginalia.api.exception.InvalidAttachmentException;
import com.marginalia.api.exception.InvalidContentBlockException;
import com.marginalia.api.repository.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.UUID;

/** Validates, uploads, and persists image attachments for content blocks owned by a user. */
@Service
@RequiredArgsConstructor
public class AttachmentService {

    private static final long MAX_IMAGE_SIZE = DataSize.ofMegabytes(5).toBytes();

    private final AttachmentRepository attachmentRepository;
    private final ResourceOwnershipService resourceOwnershipService;
    private final CloudinaryImageService cloudinaryImageService;

    /**
     * Uploads an image and creates attachment metadata for an owned IMAGE block.
     *
     * @param blockId identifier of the target content block
     * @param file image file to validate and upload
     * @param userId identifier of the owning user
     * @return the persisted attachment metadata
     * @throws InvalidContentBlockException if the target block is not an IMAGE block
     * @throws InvalidAttachmentException if the file is empty or is not an image
     * @throws AttachmentTooLargeException if the file exceeds the configured size limit
     */
    @Transactional
    public AttachmentResponse create(UUID blockId, MultipartFile file, UUID userId) {
        ContentBlock contentBlock = requireOwnedContentBlock(blockId, userId);
        if (contentBlock.getType() != ContentBlockType.IMAGE) {
            throw new InvalidContentBlockException("Attachments can only be added to IMAGE blocks");
        }
        validateImage(file);
        CloudinaryUploadResult uploadResult = cloudinaryImageService.upload(file);

        Attachment attachment = Attachment.builder()
                .contentBlockId(blockId)
                .url(uploadResult.secureUrl())
                .sizeBytes(uploadResult.sizeBytes())
                .publicId(uploadResult.publicId())
                .build();

        return toResponse(attachmentRepository.save(attachment));
    }

    @Transactional
    public void delete(UUID blockId, UUID attachmentId, UUID userId) {
        requireOwnedContentBlock(blockId, userId);
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .filter(candidate -> candidate.getContentBlockId().equals(blockId))
                .orElseThrow(() -> new AttachmentNotFoundException(attachmentId));

        attachmentRepository.delete(attachment);
        cloudinaryImageService.delete(attachment.getPublicId(), attachment.getUrl());
    }

    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidAttachmentException("Image file must not be empty");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new AttachmentTooLargeException();
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new InvalidAttachmentException("Only image files are allowed");
        }
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
