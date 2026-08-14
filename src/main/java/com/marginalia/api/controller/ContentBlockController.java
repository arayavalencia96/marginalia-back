package com.marginalia.api.controller;

import com.marginalia.api.dto.ContentBlockRequest;
import com.marginalia.api.dto.ContentBlockResponse;
import com.marginalia.api.service.ContentBlockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Exposes authenticated endpoints for managing typed content blocks within owned chapters. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ContentBlockController {

    private final ContentBlockService contentBlockService;

    /**
     * Creates a content block in an owned chapter.
     *
     * @param chapterId identifier of the target chapter
     * @param userId identifier of the authenticated user
     * @param request validated block data
     * @return the created content block
     */
    @PostMapping("/chapters/{chapterId}/blocks")
    @ResponseStatus(HttpStatus.CREATED)
    public ContentBlockResponse create(
            @PathVariable UUID chapterId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody ContentBlockRequest request
    ) {
        return contentBlockService.create(chapterId, request, userId);
    }

    /**
     * Lists all content blocks in an owned chapter in display order.
     *
     * @param chapterId identifier of the target chapter
     * @param userId identifier of the authenticated user
     * @return the chapter's content blocks
     */
    @GetMapping("/chapters/{chapterId}/blocks")
    public List<ContentBlockResponse> findAll(
            @PathVariable UUID chapterId,
            @AuthenticationPrincipal UUID userId
    ) {
        return contentBlockService.findAll(chapterId, userId);
    }

    /**
     * Updates an owned content block.
     *
     * @param id identifier of the block to update
     * @param userId identifier of the authenticated user
     * @param request validated replacement block data
     * @return the updated content block
     */
    @PutMapping("/blocks/{id}")
    public ContentBlockResponse update(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody ContentBlockRequest request
    ) {
        return contentBlockService.update(id, request, userId);
    }

    /**
     * Toggles the resolved state of an owned exercise block.
     *
     * @param id identifier of the exercise block
     * @param userId identifier of the authenticated user
     * @return the block with its updated resolved state
     */
    @PatchMapping("/blocks/{id}/resolve")
    public ContentBlockResponse toggleResolved(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId
    ) {
        return contentBlockService.toggleResolved(id, userId);
    }

    /**
     * Deletes an owned content block.
     *
     * @param id identifier of the block to delete
     * @param userId identifier of the authenticated user
     */
    @DeleteMapping("/blocks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId
    ) {
        contentBlockService.delete(id, userId);
    }
}
