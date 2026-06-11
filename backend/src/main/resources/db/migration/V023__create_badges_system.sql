-- Migration pour créer le système de badges et certifications
-- Phase E.3 - Badges et Certifications
-- 
-- Crée les tables nécessaires pour gérer les badges et leur attribution aux utilisateurs :
-- - badges : Définition des badges disponibles
-- - user_badges : Association entre utilisateurs et badges

-- Étape 1 : Créer la table badges
CREATE TABLE IF NOT EXISTS badges (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    icon_url VARCHAR(500),
    category ENUM('CERTIFICATION', 'COMPETITION', 'FORMATION', 'PARTENARIAT') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Étape 2 : Créer la table user_badges
CREATE TABLE IF NOT EXISTS user_badges (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    user_id CHAR(36) NOT NULL,
    badge_id CHAR(36) NOT NULL,
    earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_displayed BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (badge_id) REFERENCES badges(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_badge (user_id, badge_id),
    INDEX idx_user (user_id),
    INDEX idx_badge (badge_id),
    INDEX idx_displayed (user_id, is_displayed),
    INDEX idx_earned_at (earned_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

