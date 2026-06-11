-- Migration V005 : Ajout de la colonne cover_photo_url à la table salons
-- Cette migration permet aux propriétaires de salons d'ajouter une photo de couverture

ALTER TABLE salons 
ADD COLUMN cover_photo_url VARCHAR(500) NULL 
AFTER total_reviews;

