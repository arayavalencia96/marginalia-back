package com.marginalia.api.service;

import com.marginalia.api.domain.Attachment;
import com.marginalia.api.domain.ContentBlock;
import com.marginalia.api.domain.ContentBlockType;
import com.marginalia.api.dto.CloudinaryUploadResult;
import com.marginalia.api.exception.AttachmentTooLargeException;
import com.marginalia.api.exception.InvalidAttachmentException;
import com.marginalia.api.exception.InvalidContentBlockException;
import com.marginalia.api.repository.AttachmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    @Mock
    private AttachmentRepository repository;

    @Mock
    private ResourceOwnershipService ownershipService;

    @Mock
    private CloudinaryImageService imageService;

    @InjectMocks
    private AttachmentService service;

    @Test
    void uploadsAndPersistsValidImage() {
        UUID blockId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        var file = new MockMultipartFile("file", "image.png", "image/png", new byte[]{1});
        when(ownershipService.requireOwnedContentBlock(blockId, userId))
                .thenReturn(ContentBlock.builder().id(blockId).type(ContentBlockType.IMAGE).build());
        when(imageService.upload(file)).thenReturn(new CloudinaryUploadResult(
                "https://example.test/image.png",
                1,
                "marginalia/attachments/image"
        ));
        when(repository.save(any(Attachment.class))).thenAnswer(invocation -> {
            Attachment attachment = invocation.getArgument(0);
            attachment.setId(UUID.randomUUID());
            return attachment;
        });

        var response = service.create(blockId, file, userId);

        assertThat(response.contentBlockId()).isEqualTo(blockId);
        assertThat(response.url()).isEqualTo("https://example.test/image.png");
    }

    @Test
    void deletesOwnedAttachmentFromCloudinaryAndRepository() {
        UUID blockId = UUID.randomUUID();
        UUID attachmentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Attachment attachment = Attachment.builder()
                .id(attachmentId)
                .contentBlockId(blockId)
                .publicId("marginalia/attachments/image")
                .url("https://example.test/image.png")
                .build();
        when(ownershipService.requireOwnedContentBlock(blockId, userId))
                .thenReturn(ContentBlock.builder().id(blockId).type(ContentBlockType.IMAGE).build());
        when(repository.findById(attachmentId)).thenReturn(java.util.Optional.of(attachment));

        service.delete(blockId, attachmentId, userId);

        verify(repository).delete(attachment);
        verify(imageService).delete("marginalia/attachments/image", "https://example.test/image.png");
    }

    @Test
    void rejectsAttachmentForNonImageBlock() {
        UUID blockId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(ownershipService.requireOwnedContentBlock(blockId, userId))
                .thenReturn(ContentBlock.builder().type(ContentBlockType.NOTE).build());

        assertThatThrownBy(() -> service.create(blockId, image(new byte[]{1}), userId))
                .isInstanceOf(InvalidContentBlockException.class);
    }

    @Test
    void rejectsEmptyNonImageAndOversizedFiles() {
        UUID blockId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(ownershipService.requireOwnedContentBlock(blockId, userId))
                .thenReturn(ContentBlock.builder().type(ContentBlockType.IMAGE).build());

        assertThatThrownBy(() -> service.create(blockId, image(new byte[0]), userId))
                .isInstanceOf(InvalidAttachmentException.class);
        assertThatThrownBy(() -> service.create(
                blockId,
                new MockMultipartFile("file", "file.txt", "text/plain", new byte[]{1}),
                userId
        )).isInstanceOf(InvalidAttachmentException.class);
        assertThatThrownBy(() -> service.create(blockId, image(new byte[5 * 1024 * 1024 + 1]), userId))
                .isInstanceOf(AttachmentTooLargeException.class);
    }

    private MockMultipartFile image(byte[] content) {
        return new MockMultipartFile("file", "image.png", "image/png", content);
    }
}
