-- Migration V044 : Authentification a deux facteurs TOTP (RFC 6238) - S9a
--
-- Deux tables :
-- - user_two_factor : un secret TOTP par utilisateur, chiffre au repos (AES-256-GCM,
--   cle env TOTP_ENCRYPTION_KEY). Activation en deux temps : la ligne existe avec
--   enabled=FALSE tant que l'utilisateur n'a pas prouve un premier code valide
--   (pattern S4 pending_email : un nouveau setup ecrase la ligne non confirmee).
-- - two_factor_recovery_codes : 10 codes de secours a usage unique, stockes
--   haches en BCrypt (jamais en clair), affiches une seule fois a la confirmation.

CREATE TABLE IF NOT EXISTS user_two_factor (
    user_id CHAR(36) PRIMARY KEY,
    secret_encrypted VARCHAR(512) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS two_factor_recovery_codes (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    user_id CHAR(36) NOT NULL,
    code_hash VARCHAR(60) NOT NULL,
    used_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_2fa_recovery_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
