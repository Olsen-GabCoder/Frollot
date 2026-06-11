-- Migration pour ajouter le champ post_type à la table posts
-- Permet de catégoriser les posts selon leur contexte coiffure

ALTER TABLE posts 
ADD COLUMN post_type ENUM(
    'GENERAL',
    'AVANT_APRES',
    'PORTFOLIO',
    'TENDANCE',
    'CONSEIL',
    'REALISATION',
    'INSPIRATION'
) NOT NULL DEFAULT 'GENERAL' AFTER content;

-- Ajouter un index pour améliorer les performances des requêtes filtrées par type
CREATE INDEX idx_post_type ON posts(post_type);

-- Mettre à jour les posts existants pour qu'ils aient le type GENERAL par défaut
UPDATE posts SET post_type = 'GENERAL' WHERE post_type IS NULL;

