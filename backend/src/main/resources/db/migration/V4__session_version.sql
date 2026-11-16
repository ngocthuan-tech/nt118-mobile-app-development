-- Incremented atomically on every successful password change/reset.
-- JWTs minted before this migration are accepted only while version remains zero.
ALTER TABLE users ADD COLUMN session_version BIGINT NOT NULL DEFAULT 0;
