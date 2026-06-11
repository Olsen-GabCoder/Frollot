-- Migration V006 : Création de la table post_tags
-- Cette migration permet de tagger des salons ou des utilisateurs dans un post

CREATE TABLE IF NOT EXISTS post_tags (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    post_id CHAR(36) NOT NULL,
    tagged_type ENUM('salon', 'user') NOT NULL,
    tagged_id CHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    INDEX idx_post (post_id),
    INDEX idx_tagged (tagged_type, tagged_id),
    UNIQUE KEY unique_post_tagged (post_id, tagged_type, tagged_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

