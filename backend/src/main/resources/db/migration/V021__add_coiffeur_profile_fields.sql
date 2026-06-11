-- Migration pour ajouter les champs du profil coiffeur enrichi
-- Phase E.1 - Profil Coiffeur Enrichi
-- 
-- Ajoute les colonnes nécessaires pour le profil social des coiffeurs :
-- - bio : Biographie du coiffeur
-- - years_experience : Années d'expérience
-- - certifications : Certifications (texte libre)
-- - instagram_handle : Handle Instagram
-- - portfolio_highlighted : ID du portfolio mis en avant
-- 
-- Crée également la table user_specialties pour les spécialités personnelles du coiffeur

-- Étape 1 : Ajouter les colonnes au profil coiffeur dans la table users
ALTER TABLE users
ADD COLUMN bio TEXT NULL AFTER avatar_url,
ADD COLUMN years_experience INT NULL AFTER bio,
ADD COLUMN certifications TEXT NULL AFTER years_experience,
ADD COLUMN instagram_handle VARCHAR(100) NULL AFTER certifications,
ADD COLUMN portfolio_highlighted CHAR(36) NULL AFTER instagram_handle;

-- Étape 2 : Créer la table user_specialties pour les spécialités personnelles
CREATE TABLE IF NOT EXISTS user_specialties (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    user_id CHAR(36) NOT NULL,
    specialty VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_specialty (specialty)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Étape 3 : Ajouter la contrainte de clé étrangère pour portfolio_highlighted
ALTER TABLE users
ADD CONSTRAINT fk_user_portfolio_highlighted 
FOREIGN KEY (portfolio_highlighted) REFERENCES portfolios(id) ON DELETE SET NULL;

-- Étape 4 : Ajouter un index sur portfolio_highlighted
CREATE INDEX idx_portfolio_highlighted ON users(portfolio_highlighted);

