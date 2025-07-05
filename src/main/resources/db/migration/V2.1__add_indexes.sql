CREATE INDEX IF NOT EXISTS idx_refresh_token_not_revoked ON refresh_tokens(token) WHERE revoked = false;
CREATE INDEX IF NOT EXISTS idx_refresh_token_user_active ON refresh_tokens(user_id) WHERE revoked = false;
CREATE INDEX IF NOT EXISTS idx_refresh_token_expiry_revoked ON refresh_tokens(expiry_date) WHERE revoked = true;