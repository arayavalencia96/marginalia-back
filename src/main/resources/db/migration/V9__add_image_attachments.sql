ALTER TABLE content_blocks
    DROP CONSTRAINT chk_content_blocks_type,
    DROP CONSTRAINT chk_content_blocks_step_style,
    DROP CONSTRAINT chk_content_blocks_code_language,
    DROP CONSTRAINT chk_content_blocks_resolved;

ALTER TABLE content_blocks
    ADD CONSTRAINT chk_content_blocks_type
        CHECK (type IN ('NOTE', 'STEP_LIST', 'CODE', 'MATH', 'EXERCISE', 'IMAGE')),
    ADD CONSTRAINT chk_content_blocks_step_style CHECK (
        (type IN ('NOTE', 'CODE', 'MATH', 'EXERCISE', 'IMAGE') AND step_style IS NULL)
        OR (type = 'STEP_LIST' AND step_style IN ('NUMERIC', 'ALPHABETIC'))
    ),
    ADD CONSTRAINT chk_content_blocks_code_language CHECK (
        (type = 'CODE' AND code_language IS NOT NULL AND BTRIM(code_language) <> '')
        OR (type IN ('NOTE', 'STEP_LIST', 'MATH', 'EXERCISE', 'IMAGE') AND code_language IS NULL)
    ),
    ADD CONSTRAINT chk_content_blocks_resolved CHECK (
        type = 'EXERCISE' OR resolved = FALSE
    );

CREATE TABLE attachments (
    id UUID PRIMARY KEY,
    content_block_id UUID NOT NULL,
    url VARCHAR(2048) NOT NULL,
    size_bytes BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attachments_content_block
        FOREIGN KEY (content_block_id) REFERENCES content_blocks (id) ON DELETE CASCADE,
    CONSTRAINT chk_attachments_size_bytes CHECK (size_bytes >= 0)
);

CREATE INDEX idx_attachments_content_block_id ON attachments (content_block_id);
