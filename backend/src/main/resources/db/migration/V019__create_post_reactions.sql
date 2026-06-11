-- Migration pour créer la table post_reactions
-- Permet aux utilisateurs de réagir aux posts avec des réactions spécialisées coiffure
-- Phase D.4 - Réactions Spécialisées Coiffure

CREATE TABLE IF NOT EXISTS post_reactions (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    post_id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    reaction_type ENUM('like', 'love', 'wow', 'inspirant', 'magnifique', 'bravo') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_post_reaction_unique (post_id, user_id),
    INDEX idx_post (post_id),
    INDEX idx_user (user_id),
    INDEX idx_reaction_type (reaction_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

