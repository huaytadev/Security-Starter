CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE refresh_tokens
    ADD COLUMN token_hash VARCHAR(64),
    ADD COLUMN family_id VARCHAR(36);

UPDATE refresh_tokens
SET token_hash = encode(digest(token, 'sha256'), 'hex'),
    family_id = token
WHERE token_hash IS NULL
   OR family_id IS NULL;

ALTER TABLE refresh_tokens
    ALTER COLUMN token_hash SET NOT NULL,
    ALTER COLUMN family_id SET NOT NULL;

ALTER TABLE refresh_tokens
    DROP CONSTRAINT uk_refresh_tokens_token;

ALTER TABLE refresh_tokens
    ADD CONSTRAINT uk_refresh_tokens_token_hash
        UNIQUE (token_hash);

ALTER TABLE refresh_tokens
    DROP COLUMN token;

CREATE INDEX idx_refresh_tokens_family_id
    ON refresh_tokens (family_id);