CREATE TABLE pdf_export_jobs (
    id UUID PRIMARY KEY,
    book_id UUID NOT NULL,
    user_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    file_name VARCHAR(255),
    pdf_data BYTEA,
    error_message VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_pdf_export_jobs_book
        FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE,
    CONSTRAINT fk_pdf_export_jobs_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_pdf_export_jobs_status
        CHECK (status IN ('QUEUED', 'PROCESSING', 'COMPLETED', 'FAILED')),
    CONSTRAINT chk_pdf_export_jobs_result CHECK (
        (status = 'COMPLETED' AND pdf_data IS NOT NULL AND file_name IS NOT NULL AND completed_at IS NOT NULL)
        OR (status = 'FAILED' AND error_message IS NOT NULL AND completed_at IS NOT NULL)
        OR (status IN ('QUEUED', 'PROCESSING') AND pdf_data IS NULL AND completed_at IS NULL)
    )
);

CREATE INDEX idx_pdf_export_jobs_owner
    ON pdf_export_jobs (user_id, book_id, created_at DESC);
