package com.marginalia.api.controller;

import com.marginalia.api.dto.AttachmentRequest;
import com.marginalia.api.dto.AttachmentResponse;
import com.marginalia.api.service.AttachmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/blocks/{blockId}/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AttachmentResponse create(
            @PathVariable UUID blockId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody AttachmentRequest request
    ) {
        return attachmentService.create(blockId, request, userId);
    }
}
