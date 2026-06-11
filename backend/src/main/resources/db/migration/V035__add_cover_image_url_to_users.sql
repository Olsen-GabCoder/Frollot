-- Migration pour ajouter la photo de couverture aux utilisateurs
-- Phase E - Photos de couverture pour les profils utilisateurs

ALTER TABLE users
ADD COLUMN cover_image_url VARCHAR(500) NULL;

-- Commentaire pour documenter la colonne
ALTER TABLE users
MODIFY COLUMN cover_image_url VARCHAR(500) NULL COMMENT 'URL de la photo de couverture du profil utilisateur';

