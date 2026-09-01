-- ============================================================
-- V2：用户数据模型扩展
-- 新增邮箱/昵称/背景图、好友、聊天、点赞/收藏/转发、评论
-- ============================================================

-- 1. 用户表扩展：加 email、nickname、background_asset_id
ALTER TABLE user_account
    ADD COLUMN email VARCHAR(255) NULL AFTER username,
    ADD COLUMN nickname VARCHAR(64) NULL AFTER email,
    ADD COLUMN background_asset_id BIGINT NULL AFTER avatar_asset_id,
    ADD UNIQUE KEY uk_user_account_email (email),
    ADD CONSTRAINT fk_user_background_asset
        FOREIGN KEY (background_asset_id) REFERENCES asset (id);

-- 2. 帖子表补充：加 description 字段
ALTER TABLE post
    ADD COLUMN description TEXT NULL AFTER title;

-- 3. 帖子图片表（帖子的多张图片，与 asset 关联）
CREATE TABLE post_image (
    id BIGINT NOT NULL AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    asset_id BIGINT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_post_image_post (post_id),
    CONSTRAINT fk_post_image_post FOREIGN KEY (post_id) REFERENCES post (id),
    CONSTRAINT fk_post_image_asset FOREIGN KEY (asset_id) REFERENCES asset (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 好友表
CREATE TABLE friend (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'FRIEND',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_friend_pair (user_id, friend_id),
    KEY idx_friend_user (user_id),
    CONSTRAINT fk_friend_user FOREIGN KEY (user_id) REFERENCES user_account (id),
    CONSTRAINT fk_friend_friend FOREIGN KEY (friend_id) REFERENCES user_account (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 会话表（与好友聊天的一对一会话）
CREATE TABLE conversation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_a_id BIGINT NOT NULL,
    user_b_id BIGINT NOT NULL,
    last_message TEXT NULL,
    last_message_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_conversation_pair (user_a_id, user_b_id),
    CONSTRAINT fk_conversation_user_a FOREIGN KEY (user_a_id) REFERENCES user_account (id),
    CONSTRAINT fk_conversation_user_b FOREIGN KEY (user_b_id) REFERENCES user_account (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. 消息表
CREATE TABLE message (
    id BIGINT NOT NULL AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SENT',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_message_conversation (conversation_id, created_at),
    CONSTRAINT fk_message_conversation FOREIGN KEY (conversation_id) REFERENCES conversation (id),
    CONSTRAINT fk_message_sender FOREIGN KEY (sender_id) REFERENCES user_account (id),
    CONSTRAINT fk_message_receiver FOREIGN KEY (receiver_id) REFERENCES user_account (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. 帖子点赞表
CREATE TABLE post_like (
    id BIGINT NOT NULL AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_post_like_user (post_id, user_id),
    CONSTRAINT fk_post_like_post FOREIGN KEY (post_id) REFERENCES post (id),
    CONSTRAINT fk_post_like_user FOREIGN KEY (user_id) REFERENCES user_account (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. 帖子收藏表
CREATE TABLE post_favorite (
    id BIGINT NOT NULL AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_post_favorite_user (post_id, user_id),
    CONSTRAINT fk_post_favorite_post FOREIGN KEY (post_id) REFERENCES post (id),
    CONSTRAINT fk_post_favorite_user FOREIGN KEY (user_id) REFERENCES user_account (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. 帖子转发表
CREATE TABLE post_share (
    id BIGINT NOT NULL AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_post_share_post (post_id),
    CONSTRAINT fk_post_share_post FOREIGN KEY (post_id) REFERENCES post (id),
    CONSTRAINT fk_post_share_user FOREIGN KEY (user_id) REFERENCES user_account (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. 评价（评论）表
CREATE TABLE post_comment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_post_comment_post (post_id, created_at),
    CONSTRAINT fk_post_comment_post FOREIGN KEY (post_id) REFERENCES post (id),
    CONSTRAINT fk_post_comment_author FOREIGN KEY (author_id) REFERENCES user_account (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;