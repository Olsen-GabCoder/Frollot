-- Migration pour créer le système de collections thématiques
-- Phase F.1 - Collections Thématiques
-- 
-- Crée les tables nécessaires pour gérer les collections de posts :
-- - collections : Définition des collections créées par les utilisateurs
-- - collection_posts : Association entre collections et posts

-- Étape 1 : Créer la table collections
CREATE TABLE IF NOT EXISTS collections (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    user_id CHAR(36) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    cover_image_url VARCHAR(500),
    is_public BOOLEAN DEFAULT TRUE,
    category ENUM('INSPIRATION', 'PORTFOLIO', 'TENDANCE', 'PERSONNEL') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_category (category),
    INDEX idx_public (is_public),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Étape 2 : Créer la table collection_posts
CREATE TABLE IF NOT EXISTS collection_posts (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    collection_id CHAR(36) NOT NULL,
    post_id CHAR(36) NOT NULL,
    order_index INT DEFAULT 0,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (collection_id) REFERENCES collections(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    UNIQUE KEY uk_collection_post (collection_id, post_id),
    INDEX idx_collection (collection_id),
    INDEX idx_post (post_id),
    INDEX idx_order (collection_id, order_index),
    INDEX idx_added_at (added_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

