-- Migration pour créer la table des actions de modération
-- Phase H.3 - Modération de Contenu Coiffure
-- 
-- Cette table enregistre toutes les actions de modération effectuées par les administrateurs
-- sur les contenus (posts, commentaires, utilisateurs, salons).

CREATE TABLE IF NOT EXISTS moderation_actions (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    content_entity_type ENUM('POST', 'COMMENT', 'USER', 'SALON') NOT NULL,
    content_entity_id CHAR(36) NOT NULL,
    action ENUM('HIDE', 'DELETE', 'WARN') NOT NULL,
    moderator_id CHAR(36) NOT NULL,
    reason TEXT,
    appeal_status ENUM('NONE', 'PENDING', 'APPROVED', 'REJECTED') DEFAULT 'NONE',
    appeal_reason TEXT,
    appeal_processed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (moderator_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_content_entity (content_entity_type, content_entity_id),
    INDEX idx_moderator (moderator_id),
    INDEX idx_action (action),
    INDEX idx_created_at (created_at),
    INDEX idx_appeal_status (appeal_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

