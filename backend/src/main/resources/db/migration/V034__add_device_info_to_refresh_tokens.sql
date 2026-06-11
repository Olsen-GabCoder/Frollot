-- Migration V034: Ajouter les informations de device aux refresh tokens
-- Pour un meilleur suivi des sessions actives

ALTER TABLE refresh_tokens
ADD COLUMN device_name VARCHAR(100) NULL,
ADD COLUMN device_type VARCHAR(50) NULL,
ADD COLUMN ip_address VARCHAR(45) NULL,
ADD COLUMN user_agent VARCHAR(500) NULL,
ADD COLUMN last_used_at DATETIME NULL,
ADD COLUMN location VARCHAR(100) NULL;

-- Index pour améliorer les requêtes
CREATE INDEX idx_refresh_token_last_used ON refresh_tokens(last_used_at);

