-- Migration pour créer la table follows
-- Permet aux utilisateurs de suivre des salons, coiffeurs ou autres utilisateurs
-- Phase D.2 - Système de Follow Salons/Coiffeurs

CREATE TABLE IF NOT EXISTS follows (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    follower_id CHAR(36) NOT NULL,
    following_type ENUM('user', 'salon', 'coiffeur') NOT NULL,
    following_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_follow_unique (follower_id, following_type, following_id),
    INDEX idx_follower (follower_id),
    INDEX idx_following (following_type, following_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

