-- Migration pour ajouter les champs du profil social salon
-- Phase E.2 - Profil Salon Social
-- 
-- Ajoute les colonnes nécessaires pour le profil social des salons :
-- - social_description : Description sociale du salon (différente de description)
-- - social_cover_image : Image de couverture sociale (différente de cover_photo_url)
-- 
-- Crée également la table salon_highlighted_posts pour les posts mis en avant

-- Étape 1 : Ajouter les colonnes au profil social dans la table salons
ALTER TABLE salons
ADD COLUMN social_description TEXT NULL AFTER cover_photo_url,
ADD COLUMN social_cover_image VARCHAR(500) NULL AFTER social_description;

-- Étape 2 : Créer la table salon_highlighted_posts pour les posts mis en avant
CREATE TABLE IF NOT EXISTS salon_highlighted_posts (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    salon_id CHAR(36) NOT NULL,
    post_id CHAR(36) NOT NULL,
    order_index INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (salon_id) REFERENCES salons(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    UNIQUE KEY uk_salon_post (salon_id, post_id),
    INDEX idx_salon (salon_id),
    INDEX idx_post (post_id),
    INDEX idx_order (salon_id, order_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

