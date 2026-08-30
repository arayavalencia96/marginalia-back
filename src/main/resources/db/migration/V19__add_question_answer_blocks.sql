ALTER TABLE content_blocks
    DROP CONSTRAINT chk_content_blocks_type,
    DROP CONSTRAINT chk_content_blocks_heading_level,
    DROP CONSTRAINT chk_content_blocks_step_style,
    DROP CONSTRAINT chk_content_blocks_code_language,
    DROP CONSTRAINT chk_content_blocks_resolved,
    DROP CONSTRAINT chk_content_blocks_description;

ALTER TABLE content_blocks
    ADD COLUMN answer TEXT,
    ADD CONSTRAINT chk_content_blocks_type
        CHECK (type IN ('NOTE', 'HEADING', 'STEP_LIST', 'CODE', 'MATH', 'EXERCISE', 'QUESTION_ANSWER', 'IMAGE')),
    ADD CONSTRAINT chk_content_blocks_heading_level CHECK (
        (type = 'HEADING' AND heading_level IN ('TITLE', 'SUBTITLE'))
        OR (type <> 'HEADING' AND heading_level IS NULL)
    ),
    ADD CONSTRAINT chk_content_blocks_step_style CHECK (
        (type = 'STEP_LIST' AND step_style IN ('NUMERIC', 'ALPHABETIC', 'BULLETED'))
        OR (type <> 'STEP_LIST' AND step_style IS NULL)
    ),
    ADD CONSTRAINT chk_content_blocks_code_language CHECK (
        (type = 'CODE' AND code_language IS NOT NULL AND BTRIM(code_language) <> '')
        OR (type <> 'CODE' AND code_language IS NULL)
    ),
    ADD CONSTRAINT chk_content_blocks_resolved CHECK (
        type = 'EXERCISE' OR resolved = FALSE
    ),
    ADD CONSTRAINT chk_content_blocks_description CHECK (
        (type IN ('CODE', 'MATH', 'EXERCISE', 'IMAGE')
            AND (description IS NULL OR BTRIM(description) <> ''))
        OR (type NOT IN ('CODE', 'MATH', 'EXERCISE', 'IMAGE') AND description IS NULL)
    ),
    ADD CONSTRAINT chk_content_blocks_answer CHECK (
        (type = 'QUESTION_ANSWER' AND answer IS NOT NULL AND BTRIM(answer) <> '')
        OR (type <> 'QUESTION_ANSWER' AND answer IS NULL)
    );
