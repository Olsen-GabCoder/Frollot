-- Script pour créer un utilisateur de test vérifié
USE coiffure_db;

-- Créer un utilisateur vérifié (simulant une inscription réussie)
INSERT INTO users (
    id,
    email,
    password_hash,
    user_type,
    first_name,
    last_name,
    phone_number,
    is_verified,
    email_verified,
    is_active,
    email_verification_token,
    email_verification_token_expires_at,
    email_verification_sent_at,
    created_at,
    updated_at
) VALUES (
    'test-user-123',
    'test@example.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', -- mot de passe: password
    'SALON_OWNER',
    'Test',
    'User',
    '+33123456789',
    false, -- is_verified (vérification professionnelle)
    true,  -- email_verified (vérification email OTP)
    true,  -- is_active
    NULL,
    NULL,
    NOW(),
    NOW(),
    NOW()
);

-- Vérifier que l'utilisateur a été créé correctement
SELECT id, email, email_verified, is_verified, user_type, created_at FROM users WHERE email = 'test@example.com';

COMMIT;
