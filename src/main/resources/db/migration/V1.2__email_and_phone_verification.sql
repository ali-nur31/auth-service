ALTER TABLE users
    ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;
    ADD COLUMN phone_verified BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE verification_tokens (
    id          SERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type        VARCHAR(20) NOT NULL,
    token       VARCHAR(120) NOT NULL UNIQUE,
    expiry_at   TIMESTAMP NOT NULL,
    consumed    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_verify_token ON verification_tokens(token);