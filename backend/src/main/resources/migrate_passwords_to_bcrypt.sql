-- ============================================
-- MIGRATION: Hashage BCrypt des mots de passe
-- ============================================
-- Ce script met à jour les mots de passe en clair existants
-- vers des hashes BCrypt sécurisés.
--
-- HASHES GÉNÉRÉS LE: 2025-01-XX
-- ============================================

USE coiffure_db;

-- Désactiver temporairement les contraintes
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;

-- ============================================
-- MISE À JOUR DES UTILISATEURS DE DÉMONSTRATION
-- ============================================

-- Admin (mot de passe: Admin@123)
-- Hash BCrypt: $2a$10$mZnT2cHLsdIVKBaaTF/6MOYxU/8Nnidnp1nKYThkWOLcbjG61h2bW
UPDATE users
SET password_hash = '$2a$10$mZnT2cHLsdIVKBaaTF/6MOYxU/8Nnidnp1nKYThkWOLcbjG61h2bW'
WHERE id = 'admin-001' AND email = 'admin@coiffure.com';

-- Sophie Martin - Propriétaire (mot de passe: Sophie@123)
-- Hash BCrypt: $2a$10$Lwk.riXowpxTU0ijOJcokOYdlVhXx.oXGqUJlIz7RHz94NAWzXrJi
UPDATE users
SET password_hash = '$2a$10$Lwk.riXowpxTU0ijOJcokOYdlVhXx.oXGqUJlIz7RHz94NAWzXrJi'
WHERE id = 'owner-001' AND email = 'sophie@elysee-coiffure.com';

-- ============================================
-- VÉRIFICATION DES MISES À JOUR
-- ============================================

SELECT
    id,
    email,
    user_type,
    CASE
        WHEN password_hash LIKE '$2a$10$%' THEN '✅ Hash BCrypt valide'
        ELSE '❌ Hash non-BCrypt'
    END AS hash_status,
    LENGTH(password_hash) AS hash_length,
    SUBSTRING(password_hash, 1, 20) AS hash_preview,
    is_active,
    created_at
FROM users
WHERE id IN ('admin-001', 'owner-001')
ORDER BY created_at;

-- ============================================
-- RÉSUMÉ DE LA MIGRATION
-- ============================================

SELECT
    COUNT(*) AS total_users,
    SUM(CASE WHEN password_hash LIKE '$2a$10$%' THEN 1 ELSE 0 END) AS bcrypt_hashed,
    SUM(CASE WHEN password_hash NOT LIKE '$2a$10$%' THEN 1 ELSE 0 END) AS plain_text
FROM users;

-- ============================================
-- NOTES IMPORTANTES
-- ============================================

-- Les mots de passe de démonstration sont:
-- 📧 admin@coiffure.com → Admin@123
-- 📧 sophie@elysee-coiffure.com → Sophie@123

-- ⚠️ IMPORTANT: Après cette migration, l'ancien endpoint /api/users/login
-- utilisera BCrypt pour vérifier les mots de passe.

-- ============================================
-- POUR TESTER LA MIGRATION
-- ============================================

-- Test 1: Connexion avec admin@coiffure.com / Admin@123
-- curl -X POST http://localhost:8080/api/users/login \
--   -H "Content-Type: application/json" \
--   -d '{"email":"admin@coiffure.com","password":"Admin@123"}'

-- Test 2: Connexion avec sophie@elysee-coiffure.com / Sophie@123
-- curl -X POST http://localhost:8080/api/users/login \
--   -H "Content-Type: application/json" \
--   -d '{"email":"sophie@elysee-coiffure.com","password":"Sophie@123"}'

-- PowerShell:
-- Invoke-RestMethod -Uri "http://localhost:8080/api/users/login" `
--   -Method POST -ContentType "application/json" `
--   -Body '{"email":"admin@coiffure.com","password":"Admin@123"}'

-- ============================================
-- NETTOYAGE (OPTIONNEL)
-- ============================================

-- Une fois la migration validée en production, vous pouvez:
-- 1. Supprimer le PasswordHashController.kt
-- 2. Retirer "dev" de spring.profiles.active dans application.yml
-- 3. Supprimer la ligne .requestMatchers("/api/dev/**").permitAll()
--    dans SecurityConfig.kt

-- Réactiver les contraintes
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- ============================================
-- FIN DE LA MIGRATION
-- ============================================

SELECT '✅ Migration des mots de passe terminée avec succès!' AS status,
       'Vous pouvez maintenant tester la connexion avec les comptes de démo' AS next_step;