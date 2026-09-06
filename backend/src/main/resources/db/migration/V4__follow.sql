-- V4：新增单向关注关系。
-- follow 与 friend 分开，避免把关注和好友申请混成同一种关系。
CREATE TABLE follow (
    id BIGINT NOT NULL AUTO_INCREMENT,
    follower_id BIGINT NOT NULL,
    following_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_follow_pair (follower_id, following_id),
    KEY idx_follow_follower_created (follower_id, created_at, id),
    KEY idx_follow_following_created (following_id, created_at, id),
    CONSTRAINT chk_follow_not_self CHECK (follower_id <> following_id),
    CONSTRAINT fk_follow_follower FOREIGN KEY (follower_id) REFERENCES user_account (id),
    CONSTRAINT fk_follow_following FOREIGN KEY (following_id) REFERENCES user_account (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
