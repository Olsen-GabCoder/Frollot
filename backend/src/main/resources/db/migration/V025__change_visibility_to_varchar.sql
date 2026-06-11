-- Migration pour changer le type de la colonne visibility de ENUM à VARCHAR
-- Phase F.3 - Visibilité des Posts
-- 
-- Hibernate avec @Convert s'attend à un VARCHAR, mais la base de données utilise ENUM.
-- Cette migration convertit la colonne en VARCHAR(20) pour être compatible avec le converter.

-- Étape 1 : Modifier le type de colonne de ENUM à VARCHAR(20)
ALTER TABLE posts 
MODIFY COLUMN visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC';

-- Étape 2 : Convertir les valeurs existantes en majuscules (si nécessaire)
-- Les valeurs existantes sont déjà en minuscules, mais le converter les gère automatiquement
-- On peut laisser les valeurs telles quelles car le converter convertit "public" -> PUBLIC

