CREATE TABLE verification_codes (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    code VARCHAR(6) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_verification_codes_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_verification_codes_format CHECK (code ~ '^[0-9]{6}$')
);

CREATE INDEX idx_verification_codes_lookup
    ON verification_codes (user_id, code, used, expires_at DESC);
CREATE INDEX idx_verification_codes_expires_at ON verification_codes (expires_at);
