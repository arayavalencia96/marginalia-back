ALTER TABLE content_blocks
    DROP CONSTRAINT chk_content_blocks_type,
    DROP CONSTRAINT chk_content_blocks_step_style,
    DROP CONSTRAINT chk_content_blocks_code_language;

ALTER TABLE content_blocks
    ADD CONSTRAINT chk_content_blocks_type CHECK (type IN ('NOTE', 'STEP_LIST', 'CODE', 'MATH')),
    ADD CONSTRAINT chk_content_blocks_step_style CHECK (
        (type IN ('NOTE', 'CODE', 'MATH') AND step_style IS NULL)
        OR (type = 'STEP_LIST' AND step_style IN ('NUMERIC', 'ALPHABETIC'))
    ),
    ADD CONSTRAINT chk_content_blocks_code_language CHECK (
        (type = 'CODE' AND code_language IS NOT NULL AND BTRIM(code_language) <> '')
        OR (type IN ('NOTE', 'STEP_LIST', 'MATH') AND code_language IS NULL)
    );
