ALTER TABLE content_blocks
    DROP CONSTRAINT chk_content_blocks_type;

ALTER TABLE content_blocks
    ADD COLUMN step_style VARCHAR(20),
    ADD CONSTRAINT chk_content_blocks_type CHECK (type IN ('NOTE', 'STEP_LIST')),
    ADD CONSTRAINT chk_content_blocks_step_style CHECK (
        (type = 'NOTE' AND step_style IS NULL)
        OR (type = 'STEP_LIST' AND step_style IN ('NUMERIC', 'ALPHABETIC'))
    );

CREATE TABLE content_block_steps (
    id UUID PRIMARY KEY,
    content_block_id UUID NOT NULL,
    step_order INTEGER NOT NULL,
    text TEXT NOT NULL,
    CONSTRAINT fk_content_block_steps_block
        FOREIGN KEY (content_block_id) REFERENCES content_blocks (id) ON DELETE CASCADE,
    CONSTRAINT chk_content_block_steps_order CHECK (step_order >= 0),
    CONSTRAINT uq_content_block_steps_order UNIQUE (content_block_id, step_order)
);

CREATE INDEX idx_content_block_steps_block_order
    ON content_block_steps (content_block_id, step_order);
