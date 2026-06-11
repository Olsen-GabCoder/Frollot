-- V032: Correction des mots de passe de test
-- Hash BCrypt pour "password123" généré par Spring Security BCryptPasswordEncoder

-- Mettre à jour tous les utilisateurs avec un placeholder password_hash
-- Le hash ci-dessous correspond à "password123" en BCrypt
UPDATE users 
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr9TqUy9gQz2eZuXZQ9LqkVvZ5xL5Oi'
WHERE password_hash = '' 
   OR password_hash IS NULL 
   OR password_hash LIKE '$2a$12$encrypted_password_here%';

-- Correction spécifique pour les utilisateurs de test du schema.sql
UPDATE users 
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr9TqUy9gQz2eZuXZQ9LqkVvZ5xL5Oi'
WHERE email IN ('admin@coiffure.com', 'sophie@elysee-coiffure.com')
  AND (password_hash = '' OR password_hash IS NULL OR password_hash = '$2a$12$encrypted_password_here');

