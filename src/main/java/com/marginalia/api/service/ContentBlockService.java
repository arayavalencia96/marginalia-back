package com.marginalia.api.service;

import com.marginalia.api.domain.Chapter;
import com.marginalia.api.domain.ContentBlock;
import com.marginalia.api.domain.ContentBlockStep;
import com.marginalia.api.domain.ContentBlockType;
import com.marginalia.api.domain.StepStyle;
import com.marginalia.api.dto.ContentBlockRequest;
import com.marginalia.api.dto.ContentBlockResponse;
import com.marginalia.api.dto.StepListBlockResponse;
import com.marginalia.api.exception.InvalidContentBlockException;
import com.marginalia.api.repository.ContentBlockRepository;
import com.marginalia.api.repository.ContentBlockStepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

/** Implements owned content-block CRUD and type-specific persistence rules. */
@Service
@RequiredArgsConstructor
public class ContentBlockService {

    private final ContentBlockRepository contentBlockRepository;
    private final ContentBlockStepRepository contentBlockStepRepository;
    private final ResourceOwnershipService resourceOwnershipService;

    /**
     * Creates a typed content block and any nested step-list entries in an owned chapter.
     *
     * @param chapterId identifier of the target chapter
     * @param request content-block data
     * @param userId identifier of the expected owner
     * @return the created content block
     * @throws InvalidContentBlockException if required type-specific data is missing
     */
    @Transactional
    public ContentBlockResponse create(UUID chapterId, ContentBlockRequest request, UUID userId) {
        requireOwnedChapter(chapterId, userId);
        validateRequest(request);

        ContentBlock contentBlock = ContentBlock.builder()
                .chapterId(chapterId)
                .type(request.type())
                .content(contentFor(request))
                .stepStyle(stepStyleFor(request))
                .codeLanguage(codeLanguageFor(request))
                .orderIndex(request.orderIndex())
                .build();

        ContentBlock savedContentBlock = contentBlockRepository.save(contentBlock);
        saveSteps(savedContentBlock.getId(), request);
        return toResponse(savedContentBlock);
    }

    /**
     * Lists content blocks in an owned chapter in display order.
     *
     * @param chapterId identifier of the target chapter
     * @param userId identifier of the expected owner
     * @return ordered content blocks
     */
    @Transactional(readOnly = true)
    public List<ContentBlockResponse> findAll(UUID chapterId, UUID userId) {
        requireOwnedChapter(chapterId, userId);

        return contentBlockRepository.findAllByChapterIdOrderByOrderIndexAsc(chapterId).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Replaces an owned block's type-specific content and nested step-list entries.
     *
     * @param id identifier of the content block
     * @param request replacement content-block data
     * @param userId identifier of the expected owner
     * @return the updated content block
     * @throws InvalidContentBlockException if required type-specific data is missing
     */
    @Transactional
    public ContentBlockResponse update(UUID id, ContentBlockRequest request, UUID userId) {
        ContentBlock contentBlock = findOwnedContentBlock(id, userId);
        validateRequest(request);
        contentBlock.setType(request.type());
        contentBlock.setContent(contentFor(request));
        contentBlock.setStepStyle(stepStyleFor(request));
        contentBlock.setCodeLanguage(codeLanguageFor(request));
        if (request.type() != ContentBlockType.EXERCISE) {
            contentBlock.setResolved(false);
        }
        contentBlock.setOrderIndex(request.orderIndex());

        ContentBlock savedContentBlock = contentBlockRepository.save(contentBlock);
        contentBlockStepRepository.deleteAllByContentBlockId(id);
        saveSteps(id, request);
        return toResponse(savedContentBlock);
    }

    /**
     * Deletes an owned content block.
     *
     * @param id identifier of the content block
     * @param userId identifier of the expected owner
     */
    @Transactional
    public void delete(UUID id, UUID userId) {
        contentBlockRepository.delete(findOwnedContentBlock(id, userId));
    }

    /**
     * Toggles the resolved state of an owned exercise block.
     *
     * @param id identifier of the content block
     * @param userId identifier of the expected owner
     * @return the updated exercise block
     * @throws InvalidContentBlockException if the block is not an exercise
     */
    @Transactional
    public ContentBlockResponse toggleResolved(UUID id, UUID userId) {
        ContentBlock contentBlock = findOwnedContentBlock(id, userId);
        if (contentBlock.getType() != ContentBlockType.EXERCISE) {
            throw new InvalidContentBlockException("Only EXERCISE blocks can be resolved");
        }

        contentBlock.setResolved(!contentBlock.isResolved());
        return toResponse(contentBlockRepository.save(contentBlock));
    }

    private ContentBlock findOwnedContentBlock(UUID id, UUID userId) {
        return resourceOwnershipService.requireOwnedContentBlock(id, userId);
    }

    private Chapter requireOwnedChapter(UUID chapterId, UUID userId) {
        return resourceOwnershipService.requireOwnedChapter(chapterId, userId);
    }

    private void validateRequest(ContentBlockRequest request) {
        if (request.type() == ContentBlockType.NOTE
                && (request.content() == null || request.content().isBlank())) {
            throw new InvalidContentBlockException("NOTE content must not be blank");
        }

        if (request.type() == ContentBlockType.STEP_LIST && request.stepList() == null) {
            throw new InvalidContentBlockException("STEP_LIST requires stepList data");
        }

        if (request.type() == ContentBlockType.CODE
                && (request.content() == null || request.content().isBlank())) {
            throw new InvalidContentBlockException("CODE content must not be blank");
        }

        if (request.type() == ContentBlockType.CODE
                && (request.codeLanguage() == null || request.codeLanguage().isBlank())) {
            throw new InvalidContentBlockException("CODE language must not be blank");
        }

        if (request.type() == ContentBlockType.MATH
                && (request.content() == null || request.content().isBlank())) {
            throw new InvalidContentBlockException("MATH content must not be blank");
        }

        if (request.type() == ContentBlockType.EXERCISE
                && (request.content() == null || request.content().isBlank())) {
            throw new InvalidContentBlockException("EXERCISE content must not be blank");
        }
    }

    private String contentFor(ContentBlockRequest request) {
        return request.type() == ContentBlockType.STEP_LIST
                || request.type() == ContentBlockType.IMAGE
                ? ""
                : request.content();
    }

    private StepStyle stepStyleFor(ContentBlockRequest request) {
        return request.type() == ContentBlockType.STEP_LIST
                ? request.stepList().stepStyle()
                : null;
    }

    private String codeLanguageFor(ContentBlockRequest request) {
        return request.type() == ContentBlockType.CODE ? request.codeLanguage() : null;
    }

    private void saveSteps(UUID contentBlockId, ContentBlockRequest request) {
        if (request.type() != ContentBlockType.STEP_LIST) {
            return;
        }

        List<String> steps = request.stepList().steps();
        List<ContentBlockStep> contentBlockSteps = IntStream.range(0, steps.size())
                .mapToObj(index -> ContentBlockStep.builder()
                        .contentBlockId(contentBlockId)
                        .stepOrder(index)
                        .text(steps.get(index))
                        .build())
                .toList();
        contentBlockStepRepository.saveAll(contentBlockSteps);
    }

    private ContentBlockResponse toResponse(ContentBlock contentBlock) {
        StepListBlockResponse stepList = contentBlock.getType() == ContentBlockType.STEP_LIST
                ? toStepListResponse(contentBlock)
                : null;

        return new ContentBlockResponse(
                contentBlock.getId(),
                contentBlock.getChapterId(),
                contentBlock.getType(),
                contentBlock.getContent(),
                contentBlock.getCodeLanguage(),
                contentBlock.isResolved(),
                contentBlock.getOrderIndex(),
                stepList
        );
    }

    private StepListBlockResponse toStepListResponse(ContentBlock contentBlock) {
        List<String> steps = contentBlockStepRepository
                .findAllByContentBlockIdOrderByStepOrderAsc(contentBlock.getId())
                .stream()
                .map(ContentBlockStep::getText)
                .toList();

        return new StepListBlockResponse(contentBlock.getStepStyle(), steps);
    }
}
