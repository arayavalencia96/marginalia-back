package com.marginalia.api.controller;

import com.marginalia.api.dto.AttachmentResponse;
import com.marginalia.api.service.AttachmentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/** Exposes authenticated endpoints for uploading image attachments to content blocks. */
@RestController
@RequestMapping("/api/blocks/{blockId}/attachments")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    /**
     * Uploads and associates an image with an IMAGE content block.
     *
     * @param blockId identifier of the target content block
     * @param userId identifier of the authenticated user
     * @param file multipart image to upload
     * @return the persisted attachment metadata
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AttachmentResponse create(
            @PathVariable UUID blockId,
            @AuthenticationPrincipal UUID userId,
            @RequestPart("file") MultipartFile file
    ) {
        return attachmentService.create(blockId, file, userId);
    }
}
