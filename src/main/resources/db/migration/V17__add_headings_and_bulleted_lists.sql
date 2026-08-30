ALTER TABLE content_blocks
    DROP CONSTRAINT chk_content_blocks_type,
    DROP CONSTRAINT chk_content_blocks_step_style,
    DROP CONSTRAINT chk_content_blocks_code_language,
    DROP CONSTRAINT chk_content_blocks_resolved;

ALTER TABLE content_blocks
    ADD COLUMN heading_level VARCHAR(20),
    ADD CONSTRAINT chk_content_blocks_type
        CHECK (type IN ('NOTE', 'HEADING', 'STEP_LIST', 'CODE', 'MATH', 'EXERCISE', 'IMAGE')),
    ADD CONSTRAINT chk_content_blocks_heading_level CHECK (
        (type = 'HEADING' AND heading_level IN ('TITLE', 'SUBTITLE'))
        OR (type <> 'HEADING' AND heading_level IS NULL)
    ),
    ADD CONSTRAINT chk_content_blocks_step_style CHECK (
        (type IN ('NOTE', 'HEADING', 'CODE', 'MATH', 'EXERCISE', 'IMAGE') AND step_style IS NULL)
        OR (type = 'STEP_LIST' AND step_style IN ('NUMERIC', 'ALPHABETIC', 'BULLETED'))
    ),
    ADD CONSTRAINT chk_content_blocks_code_language CHECK (
        (type = 'CODE' AND code_language IS NOT NULL AND BTRIM(code_language) <> '')
        OR (type IN ('NOTE', 'HEADING', 'STEP_LIST', 'MATH', 'EXERCISE', 'IMAGE') AND code_language IS NULL)
    ),
    ADD CONSTRAINT chk_content_blocks_resolved CHECK (
        type = 'EXERCISE' OR resolved = FALSE
    );
