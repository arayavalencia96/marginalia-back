ALTER TABLE content_blocks
    ADD COLUMN description TEXT,
    ADD CONSTRAINT chk_content_blocks_description CHECK (
        (type IN ('CODE', 'MATH', 'EXERCISE', 'IMAGE')
            AND (description IS NULL OR BTRIM(description) <> ''))
        OR (type IN ('NOTE', 'HEADING', 'STEP_LIST') AND description IS NULL)
    );
