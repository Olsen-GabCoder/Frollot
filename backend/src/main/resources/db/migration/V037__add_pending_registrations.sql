-- Migration V037: Système de pré-inscription avec vérification email obligatoire
-- Cette migration crée la table des pré-inscriptions pour garantir qu'aucun compte
-- ne peut être créé sans vérification préalable de l'adresse email

CREATE TABLE pending_registrations (
    id CHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    user_type VARCHAR(20) NOT NULL,
    first_name VARCHAR(100) NULL,
    last_name VARCHAR(100) NULL,
    phone_number VARCHAR(20) NULL,
    verification_token VARCHAR(100) UNIQUE,
    token_expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    attempts INT DEFAULT 0,
    last_attempt_at TIMESTAMP NULL,

    INDEX idx_pending_email (email),
    INDEX idx_pending_token (verification_token),
    INDEX idx_pending_expires (token_expires_at),
    INDEX idx_pending_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
