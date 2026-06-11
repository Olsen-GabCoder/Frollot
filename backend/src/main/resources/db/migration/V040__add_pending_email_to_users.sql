-- V040 : Changement d'email avec re-verification
-- Ajoute la colonne pending_email sur users : le nouvel email demande reste en attente
-- tant qu'il n'est pas confirme via le token de verification (colonnes token existantes
-- email_verification_token / email_verification_token_expires_at reutilisees).
ALTER TABLE users ADD COLUMN pending_email VARCHAR(255) NULL;
