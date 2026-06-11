-- Migration pour corriger le cas de l'ENUM reaction_type
-- Phase D.4 - Réactions Spécialisées Coiffure
-- 
-- Problème : L'ENUM MySQL était défini en minuscules ('like', 'love', etc.)
-- mais l'enum Kotlin utilise des majuscules (LIKE, LOVE, etc.)
-- 
-- Solution : Recréer la colonne avec les valeurs en majuscules
-- et mettre à jour les données existantes si nécessaire

-- Étape 1 : Ajouter une colonne temporaire avec les nouvelles valeurs (en majuscules)
ALTER TABLE post_reactions 
ADD COLUMN reaction_type_new ENUM('LIKE', 'LOVE', 'WOW', 'INSPIRANT', 'MAGNIFIQUE', 'BRAVO') NULL AFTER reaction_type;

-- Étape 2 : Mettre à jour la colonne temporaire avec les valeurs converties (minuscules -> majuscules)
UPDATE post_reactions SET reaction_type_new = 'LIKE' WHERE reaction_type = 'like';
UPDATE post_reactions SET reaction_type_new = 'LOVE' WHERE reaction_type = 'love';
UPDATE post_reactions SET reaction_type_new = 'WOW' WHERE reaction_type = 'wow';
UPDATE post_reactions SET reaction_type_new = 'INSPIRANT' WHERE reaction_type = 'inspirant';
UPDATE post_reactions SET reaction_type_new = 'MAGNIFIQUE' WHERE reaction_type = 'magnifique';
UPDATE post_reactions SET reaction_type_new = 'BRAVO' WHERE reaction_type = 'bravo';

-- Étape 3 : Mettre une valeur par défaut pour les éventuelles valeurs NULL (si table vide ou données non migrées)
UPDATE post_reactions SET reaction_type_new = 'LIKE' WHERE reaction_type_new IS NULL;

-- Étape 4 : Rendre la colonne temporaire NOT NULL (maintenant que toutes les données sont migrées)
ALTER TABLE post_reactions 
MODIFY COLUMN reaction_type_new ENUM('LIKE', 'LOVE', 'WOW', 'INSPIRANT', 'MAGNIFIQUE', 'BRAVO') NOT NULL;

-- Étape 5 : Supprimer l'ancienne colonne
ALTER TABLE post_reactions DROP COLUMN reaction_type;

-- Étape 6 : Renommer la nouvelle colonne
ALTER TABLE post_reactions 
CHANGE COLUMN reaction_type_new reaction_type ENUM('LIKE', 'LOVE', 'WOW', 'INSPIRANT', 'MAGNIFIQUE', 'BRAVO') NOT NULL;

