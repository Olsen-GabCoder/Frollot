-- Migration V030 : Ajout du champ preferred_language dans la table users
-- Phase 3 - Fonctionnalité Langue : Synchronisation backend

ALTER TABLE users
ADD COLUMN preferred_language VARCHAR(2) DEFAULT 'fr' COMMENT 'Langue préférée de l''utilisateur (fr, en, es, de, ar)';

-- Index pour améliorer les performances des requêtes par langue
CREATE INDEX idx_users_preferred_language ON users(preferred_language);

