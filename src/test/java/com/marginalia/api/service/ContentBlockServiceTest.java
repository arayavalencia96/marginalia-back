package com.marginalia.api.service;

import com.marginalia.api.domain.Attachment;
import com.marginalia.api.domain.Chapter;
import com.marginalia.api.domain.ContentBlock;
import com.marginalia.api.domain.ContentBlockStep;
import com.marginalia.api.domain.ContentBlockType;
import com.marginalia.api.domain.HeadingLevel;
import com.marginalia.api.domain.StepStyle;
import com.marginalia.api.dto.ContentBlockOrderRequest;
import com.marginalia.api.dto.ContentBlockRequest;
import com.marginalia.api.dto.StepListBlockRequest;
import com.marginalia.api.exception.InvalidContentBlockException;
import com.marginalia.api.repository.AttachmentRepository;
import com.marginalia.api.repository.ContentBlockRepository;
import com.marginalia.api.repository.ContentBlockStepRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentBlockServiceTest {

    @Mock
    private ContentBlockRepository repository;

    @Mock
    private ContentBlockStepRepository stepRepository;

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private ResourceOwnershipService ownershipService;

    @InjectMocks
    private ContentBlockService service;

    @Test
    void createsNoteBlock() {
        UUID chapterId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(ownershipService.requireOwnedChapter(chapterId, userId)).thenReturn(Chapter.builder().id(chapterId).build());
        when(repository.save(any(ContentBlock.class))).thenAnswer(invocation -> {
            ContentBlock block = invocation.getArgument(0);
            block.setId(UUID.randomUUID());
            return block;
        });

        var response = service.create(chapterId, request(ContentBlockType.NOTE, "A note"), userId);

        assertThat(response.type()).isEqualTo(ContentBlockType.NOTE);
        assertThat(response.content()).isEqualTo("A note");
        verify(repository).shiftOrderIndexesForInsert(chapterId, 0);
    }

    @Test
    void createsStepListWithNestedSteps() {
        UUID chapterId = UUID.randomUUID();
        UUID blockId = UUID.randomUUID();
        ContentBlockRequest request = new ContentBlockRequest(
                ContentBlockType.STEP_LIST,
                null,
                null,
                null,
                null,
                null,
                0,
                new StepListBlockRequest(StepStyle.ALPHABETIC, List.of("First", "Second"))
        );
        when(repository.save(any(ContentBlock.class))).thenAnswer(invocation -> {
            ContentBlock block = invocation.getArgument(0);
            block.setId(blockId);
            return block;
        });
        when(stepRepository.findAllByContentBlockIdOrderByStepOrderAsc(blockId)).thenReturn(List.of(
                ContentBlockStep.builder().text("First").build(),
                ContentBlockStep.builder().text("Second").build()
        ));

        var response = service.create(chapterId, request, UUID.randomUUID());

        assertThat(response.stepList().steps()).containsExactly("First", "Second");
        verify(stepRepository).saveAll(org.mockito.ArgumentMatchers.argThat(steps -> {
            List<ContentBlockStep> list = (List<ContentBlockStep>) steps;
            return list.size() == 2 && list.get(1).getStepOrder() == 1;
        }));
    }

    @Test
    void rejectsInvalidTypeSpecificData() {
        UUID chapterId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> service.create(chapterId, request(ContentBlockType.NOTE, " "), userId))
                .isInstanceOf(InvalidContentBlockException.class);
        assertThatThrownBy(() -> service.create(chapterId, request(ContentBlockType.CODE, "code"), userId))
                .isInstanceOf(InvalidContentBlockException.class);
        assertThatThrownBy(() -> service.create(chapterId, request(ContentBlockType.MATH, null), userId))
                .isInstanceOf(InvalidContentBlockException.class);
        assertThatThrownBy(() -> service.create(chapterId, request(ContentBlockType.HEADING, "Title"), userId))
                .isInstanceOf(InvalidContentBlockException.class);
        assertThatThrownBy(() -> service.create(chapterId, request(ContentBlockType.QUESTION_ANSWER, "Question"), userId))
                .isInstanceOf(InvalidContentBlockException.class);
    }

    @Test
    void persistsHeadingsDescriptionsAndQuestionAnswers() {
        UUID chapterId = UUID.randomUUID();
        when(repository.save(any(ContentBlock.class))).thenAnswer(invocation -> {
            ContentBlock block = invocation.getArgument(0);
            block.setId(UUID.randomUUID());
            return block;
        });

        var heading = service.create(chapterId, new ContentBlockRequest(
                ContentBlockType.HEADING, "Main idea", null, null,
                HeadingLevel.TITLE, null, 0, null
        ), UUID.randomUUID());
        var code = service.create(chapterId, new ContentBlockRequest(
                ContentBlockType.CODE, "const value = 1", null, "Example context",
                null, "javascript", 1, null
        ), UUID.randomUUID());
        var questionAnswer = service.create(chapterId, new ContentBlockRequest(
                ContentBlockType.QUESTION_ANSWER, "What is RSI?", "A momentum indicator", null,
                null, null, 2, null
        ), UUID.randomUUID());

        assertThat(heading.headingLevel()).isEqualTo(HeadingLevel.TITLE);
        assertThat(code.description()).isEqualTo("Example context");
        assertThat(questionAnswer.answer()).isEqualTo("A momentum indicator");
    }

    @Test
    void togglesExerciseAndRejectsOtherTypes() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ContentBlock exercise = ContentBlock.builder().id(id).type(ContentBlockType.EXERCISE).content("Solve").build();
        when(ownershipService.requireOwnedContentBlock(id, userId)).thenReturn(exercise);
        when(repository.save(exercise)).thenReturn(exercise);

        assertThat(service.toggleResolved(id, userId).resolved()).isTrue();

        exercise.setType(ContentBlockType.NOTE);
        assertThatThrownBy(() -> service.toggleResolved(id, userId)).isInstanceOf(InvalidContentBlockException.class);
    }

    @Test
    void listsUpdatesAndDeletesOwnedBlocks() {
        UUID id = UUID.randomUUID();
        UUID chapterId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ContentBlock block = ContentBlock.builder()
                .id(id).chapterId(chapterId).type(ContentBlockType.NOTE).content("Old").build();
        when(repository.findAllByChapterIdOrderByOrderIndexAsc(chapterId)).thenReturn(List.of(block));
        when(ownershipService.requireOwnedContentBlock(id, userId)).thenReturn(block);
        when(repository.save(block)).thenReturn(block);

        assertThat(service.findAll(chapterId, userId)).hasSize(1);
        assertThat(service.update(id, request(ContentBlockType.NOTE, "New"), userId).content()).isEqualTo("New");
        service.delete(id, userId);

        verify(stepRepository).deleteAllByContentBlockId(id);
        verify(repository).delete(block);
    }

    @Test
    void includesImageAttachmentsWhenListingBlocks() {
        UUID chapterId = UUID.randomUUID();
        UUID blockId = UUID.randomUUID();
        ContentBlock imageBlock = ContentBlock.builder()
                .id(blockId)
                .chapterId(chapterId)
                .type(ContentBlockType.IMAGE)
                .build();
        Attachment attachment = Attachment.builder()
                .id(UUID.randomUUID())
                .contentBlockId(blockId)
                .url("https://images.example/image.png")
                .sizeBytes(42L)
                .createdAt(Instant.now())
                .build();
        when(repository.findAllByChapterIdOrderByOrderIndexAsc(chapterId)).thenReturn(List.of(imageBlock));
        when(attachmentRepository.findAllByContentBlockIdIn(List.of(blockId))).thenReturn(List.of(attachment));

        var response = service.findAll(chapterId, UUID.randomUUID());

        assertThat(response.getFirst().attachments()).extracting(attachmentResponse -> attachmentResponse.url())
                .containsExactly("https://images.example/image.png");
    }

    @Test
    void reordersEveryChapterBlockAtomically() {
        UUID chapterId = UUID.randomUUID();
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        ContentBlock first = ContentBlock.builder().id(firstId).chapterId(chapterId).orderIndex(0).build();
        ContentBlock second = ContentBlock.builder().id(secondId).chapterId(chapterId).orderIndex(1).build();
        when(repository.findAllByChapterIdOrderByOrderIndexAsc(chapterId))
                .thenReturn(List.of(first, second));

        service.reorder(chapterId, List.of(
                new ContentBlockOrderRequest(secondId, 0),
                new ContentBlockOrderRequest(firstId, 1)
        ), UUID.randomUUID());

        assertThat(first.getOrderIndex()).isEqualTo(1);
        assertThat(second.getOrderIndex()).isZero();
        verify(repository).saveAll(List.of(first, second));
    }

    @Test
    void rejectsIncompleteBlockOrder() {
        UUID chapterId = UUID.randomUUID();
        ContentBlock block = ContentBlock.builder().id(UUID.randomUUID())
                .chapterId(chapterId).orderIndex(0).build();
        when(repository.findAllByChapterIdOrderByOrderIndexAsc(chapterId))
                .thenReturn(List.of(block));

        assertThatThrownBy(() -> service.reorder(chapterId, List.of(
                new ContentBlockOrderRequest(UUID.randomUUID(), 0)
        ), UUID.randomUUID())).isInstanceOf(InvalidContentBlockException.class);
    }

    private ContentBlockRequest request(ContentBlockType type, String content) {
        String language = type == ContentBlockType.CODE ? null : "";
        return new ContentBlockRequest(type, content, null, null, null, language, 0, null);
    }
}
