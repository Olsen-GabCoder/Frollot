-- Migration pour créer le système de signalement de contenu
-- Phase H.1 - Signalement de Contenu
-- 
-- Crée la table reports pour permettre aux utilisateurs de signaler du contenu inapproprié :
-- - posts, comments, users, salons peuvent être signalés
-- - Raisons : INAPPROPRIE, SPAM, FAUX, COPYRIGHT, AUTRE
-- - Statuts : PENDING, REVIEWED, RESOLVED, DISMISSED

-- Créer la table reports
CREATE TABLE IF NOT EXISTS reports (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    reported_entity_type ENUM('POST', 'COMMENT', 'USER', 'SALON') NOT NULL,
    reported_entity_id CHAR(36) NOT NULL,
    reporter_id CHAR(36) NOT NULL,
    reason ENUM('INAPPROPRIE', 'SPAM', 'FAUX', 'COPYRIGHT', 'AUTRE') NOT NULL,
    status ENUM('PENDING', 'REVIEWED', 'RESOLVED', 'DISMISSED') NOT NULL DEFAULT 'PENDING',
    additional_info TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_reported_entity (reported_entity_type, reported_entity_id),
    INDEX idx_reporter (reporter_id),
    INDEX idx_status (status),
    INDEX idx_reason (reason),
    INDEX idx_created_at (created_at),
    -- Empêcher qu'un utilisateur signale plusieurs fois la même entité
    UNIQUE KEY uk_reporter_entity (reporter_id, reported_entity_type, reported_entity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

