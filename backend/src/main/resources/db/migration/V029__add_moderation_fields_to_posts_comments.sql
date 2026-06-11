-- Migration pour ajouter les champs de modération aux tables posts et comments
-- Phase H.3 - Modération de Contenu Coiffure
-- 
-- Ajoute :
-- - is_hidden et is_deleted à la table posts
-- - is_hidden et is_deleted à la table comments

-- Étape 1 : Ajouter is_hidden et is_deleted à la table posts
ALTER TABLE posts 
ADD COLUMN is_hidden BOOLEAN DEFAULT FALSE 
AFTER visibility;

ALTER TABLE posts 
ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE 
AFTER is_hidden;

-- Étape 2 : Ajouter is_hidden et is_deleted à la table comments
ALTER TABLE comments 
ADD COLUMN is_hidden BOOLEAN DEFAULT FALSE 
AFTER content;

ALTER TABLE comments 
ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE 
AFTER is_hidden;

-- Étape 3 : Ajouter des index pour optimiser les recherches
CREATE INDEX idx_post_hidden ON posts(is_hidden, is_deleted);
CREATE INDEX idx_comment_hidden ON comments(is_hidden, is_deleted);

