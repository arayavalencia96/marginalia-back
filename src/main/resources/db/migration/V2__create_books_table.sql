CREATE TABLE books (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    topic VARCHAR(20) NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_books_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT chk_books_topic CHECK (
        topic IN ('PROGRAMMING', 'MATH', 'SCIENCE', 'HISTORY', 'OTHER')
    )
);

CREATE INDEX idx_books_user_id ON books (user_id);
