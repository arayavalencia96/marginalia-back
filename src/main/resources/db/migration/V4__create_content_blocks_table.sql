CREATE TABLE content_blocks (
    id UUID PRIMARY KEY,
    chapter_id UUID NOT NULL,
    type VARCHAR(30) NOT NULL,
    content TEXT NOT NULL,
    order_index INTEGER NOT NULL,
    CONSTRAINT fk_content_blocks_chapter FOREIGN KEY (chapter_id) REFERENCES chapters (id),
    CONSTRAINT chk_content_blocks_type CHECK (type IN ('NOTE')),
    CONSTRAINT chk_content_blocks_order_index CHECK (order_index >= 0)
);

CREATE INDEX idx_content_blocks_chapter_order ON content_blocks (chapter_id, order_index);
