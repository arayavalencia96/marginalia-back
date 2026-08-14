ALTER TABLE content_blocks
    DROP CONSTRAINT chk_content_blocks_type,
    DROP CONSTRAINT chk_content_blocks_step_style;

ALTER TABLE content_blocks
    ADD COLUMN code_language VARCHAR(50),
    ADD CONSTRAINT chk_content_blocks_type CHECK (type IN ('NOTE', 'STEP_LIST', 'CODE')),
    ADD CONSTRAINT chk_content_blocks_step_style CHECK (
        (type IN ('NOTE', 'CODE') AND step_style IS NULL)
        OR (type = 'STEP_LIST' AND step_style IN ('NUMERIC', 'ALPHABETIC'))
    ),
    ADD CONSTRAINT chk_content_blocks_code_language CHECK (
        (type = 'CODE' AND code_language IS NOT NULL AND BTRIM(code_language) <> '')
        OR (type IN ('NOTE', 'STEP_LIST') AND code_language IS NULL)
    );
