CREATE TABLE chapters (
    id UUID PRIMARY KEY,
    book_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    parent_chapter_id UUID,
    order_index INTEGER NOT NULL,
    CONSTRAINT fk_chapters_book FOREIGN KEY (book_id) REFERENCES books (id),
    CONSTRAINT fk_chapters_parent FOREIGN KEY (parent_chapter_id) REFERENCES chapters (id),
    CONSTRAINT chk_chapters_order_index CHECK (order_index >= 0),
    CONSTRAINT chk_chapters_not_self_parent CHECK (parent_chapter_id IS NULL OR parent_chapter_id <> id)
);

CREATE INDEX idx_chapters_book_order ON chapters (book_id, order_index);
CREATE INDEX idx_chapters_parent_id ON chapters (parent_chapter_id);
