-- Migration pour créer la table hair_hashtags
-- Système de hashtags spécialisés pour l'univers de la coiffure

CREATE TABLE IF NOT EXISTS hair_hashtags (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    name VARCHAR(100) NOT NULL UNIQUE,
    category ENUM('TECHNIQUE', 'STYLE', 'COULEUR', 'LONGUEUR', 'TEXTURE') NOT NULL,
    usage_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_hashtag_name (name),
    INDEX idx_hashtag_category (category),
    INDEX idx_hashtag_usage (usage_count DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insérer les hashtags suggérés initiaux
INSERT INTO hair_hashtags (id, name, category, usage_count) VALUES
-- Techniques
(UUID(), 'balayage', 'TECHNIQUE', 0),
(UUID(), 'degrade', 'TECHNIQUE', 0),
(UUID(), 'carre', 'TECHNIQUE', 0),
(UUID(), 'decoiffe', 'TECHNIQUE', 0),
(UUID(), 'lisse', 'TECHNIQUE', 0),
(UUID(), 'boucle', 'TECHNIQUE', 0),
-- Styles
(UUID(), 'bob', 'STYLE', 0),
(UUID(), 'pixie', 'STYLE', 0),
(UUID(), 'franges', 'STYLE', 0),
(UUID(), 'raie', 'STYLE', 0),
-- Couleurs
(UUID(), 'blond', 'COULEUR', 0),
(UUID(), 'brun', 'COULEUR', 0),
(UUID(), 'rouge', 'COULEUR', 0),
(UUID(), 'colore', 'COULEUR', 0),
(UUID(), 'naturel', 'COULEUR', 0),
-- Longueurs
(UUID(), 'court', 'LONGUEUR', 0),
(UUID(), 'milong', 'LONGUEUR', 0),
(UUID(), 'long', 'LONGUEUR', 0),
(UUID(), 'cheveuxcourts', 'LONGUEUR', 0),
-- Textures
(UUID(), 'fins', 'TEXTURE', 0),
(UUID(), 'epais', 'TEXTURE', 0),
(UUID(), 'ondules', 'TEXTURE', 0),
(UUID(), 'raides', 'TEXTURE', 0);

