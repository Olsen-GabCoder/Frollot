-- Migration pour créer la table post_media
-- Permet de gérer plusieurs images par post (avant/après, processus, détails)

CREATE TABLE IF NOT EXISTS post_media (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    post_id CHAR(36) NOT NULL,
    media_url VARCHAR(500) NOT NULL,
    media_type ENUM('before', 'after', 'process', 'detail') NOT NULL DEFAULT 'before',
    order_index INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    INDEX idx_post_media_post (post_id),
    INDEX idx_post_media_type (post_id, media_type),
    INDEX idx_post_media_order (post_id, order_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

