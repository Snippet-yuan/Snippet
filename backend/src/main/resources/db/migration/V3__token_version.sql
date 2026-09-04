-- V3：为用户增加令牌版本，用于密码修改后的 JWT 统一失效。
ALTER TABLE user_account
    ADD COLUMN token_version BIGINT NOT NULL DEFAULT 0 AFTER status;
