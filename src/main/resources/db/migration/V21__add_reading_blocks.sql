ALTER TABLE content_blocks DROP CONSTRAINT chk_content_blocks_type;
ALTER TABLE content_blocks ADD CONSTRAINT chk_content_blocks_type CHECK (type IN ('NOTE', 'HEADING', 'STEP_LIST', 'CODE', 'MATH', 'EXERCISE', 'QUESTION_ANSWER', 'IMAGE', 'CALLOUT', 'QUOTE', 'TODO_LIST', 'DIVIDER', 'TABLE'));
