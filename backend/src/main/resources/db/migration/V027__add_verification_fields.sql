-- Migration pour ajouter les champs de vérification aux utilisateurs et salons
-- Phase H.2 - Vérification Salons/Coiffeurs
-- 
-- Ajoute :
-- - verification_type à la table users (nullable, pour distinguer le type de vérification)
-- - is_verified et verification_type à la table salons (pour la vérification des salons)

-- Étape 1 : Ajouter verification_type à la table users
ALTER TABLE users 
ADD COLUMN verification_type ENUM('EMAIL', 'PHONE', 'BUSINESS', 'PROFESSIONAL') NULL 
AFTER is_verified;

-- Étape 2 : Ajouter is_verified à la table salons
ALTER TABLE salons 
ADD COLUMN is_verified BOOLEAN DEFAULT FALSE 
AFTER social_cover_image;

-- Étape 3 : Ajouter verification_type à la table salons
ALTER TABLE salons 
ADD COLUMN verification_type ENUM('EMAIL', 'PHONE', 'BUSINESS', 'PROFESSIONAL') NULL 
AFTER is_verified;

-- Étape 4 : Ajouter des index pour optimiser les recherches
CREATE INDEX idx_user_verification ON users(is_verified, verification_type);
CREATE INDEX idx_salon_verification ON salons(is_verified, verification_type);

