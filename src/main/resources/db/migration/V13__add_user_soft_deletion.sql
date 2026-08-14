ALTER TABLE users
    ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX idx_users_deleted_at
    ON users (deleted_at)
    WHERE deleted_at IS NOT NULL;

ALTER TABLE books
    DROP CONSTRAINT fk_books_user,
    ADD CONSTRAINT fk_books_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE chapters
    DROP CONSTRAINT fk_chapters_book,
    DROP CONSTRAINT fk_chapters_parent,
    ADD CONSTRAINT fk_chapters_book
        FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_chapters_parent
        FOREIGN KEY (parent_chapter_id) REFERENCES chapters (id) ON DELETE CASCADE;

ALTER TABLE content_blocks
    DROP CONSTRAINT fk_content_blocks_chapter,
    ADD CONSTRAINT fk_content_blocks_chapter
        FOREIGN KEY (chapter_id) REFERENCES chapters (id) ON DELETE CASCADE;
