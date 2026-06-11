-- Migration corrective pour corriger le type de la colonne following_id
-- Phase D.2 - Système de Follow Salons/Coiffeurs
-- Correction : CHAR(36) -> VARCHAR(36) pour correspondre aux attentes d'Hibernate

-- Modifier le type de colonne
ALTER TABLE follows 
MODIFY COLUMN following_id VARCHAR(36) NOT NULL;

-- Note: La suppression de l'index dupliqué idx_following_type_id n'est pas nécessaire
-- car il n'a jamais été créé dans V016 (corrigé avant l'application).
-- Si l'index existe pour une raison quelconque, il peut être supprimé manuellement.

