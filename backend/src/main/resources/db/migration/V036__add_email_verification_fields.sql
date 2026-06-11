-- Migration V036: Ajout des champs de vérification d'email
-- Cette migration ajoute les colonnes nécessaires pour la vérification systématique des emails

ALTER TABLE users
ADD COLUMN email_verified BOOLEAN DEFAULT FALSE NOT NULL COMMENT 'Indique si l''email a été vérifié',
ADD COLUMN email_verification_token VARCHAR(100) NULL COMMENT 'Token de vérification d''email (UUID)',
ADD COLUMN email_verification_token_expires_at TIMESTAMP NULL COMMENT 'Date d''expiration du token de vérification',
ADD COLUMN email_verification_sent_at TIMESTAMP NULL COMMENT 'Date d''envoi du dernier email de vérification';

-- Index pour améliorer les requêtes de vérification
CREATE INDEX idx_email_verification_token ON users(email_verification_token);
CREATE INDEX idx_email_verified ON users(email_verified);

-- Commentaires pour documentation
ALTER TABLE users
MODIFY COLUMN email_verified BOOLEAN DEFAULT FALSE NOT NULL COMMENT 'Indique si l''email a été vérifié (obligatoire pour utiliser le compte)',
MODIFY COLUMN email_verification_token VARCHAR(100) NULL COMMENT 'Token de vérification d''email (UUID, expire après 24h)',
MODIFY COLUMN email_verification_token_expires_at TIMESTAMP NULL COMMENT 'Date d''expiration du token de vérification (24h après création)',
MODIFY COLUMN email_verification_sent_at TIMESTAMP NULL COMMENT 'Date d''envoi du dernier email de vérification';

