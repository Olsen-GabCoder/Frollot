-- Script pour nettoyer les données de test dans Frollot
-- À exécuter dans MySQL

USE coiffure_db;

-- Supprimer toutes les données de test
DELETE FROM pending_registrations WHERE email LIKE '%@example.com' OR email LIKE '%@test.%' OR email LIKE 'test%';
DELETE FROM users WHERE email LIKE '%@example.com' OR email LIKE '%@test.%' OR email LIKE 'test%';

-- Supprimer aussi les tokens d'authentification associés
DELETE FROM refresh_tokens WHERE user_id NOT IN (SELECT id FROM users);

-- Vérifier le résultat
SELECT 'Users restants:' as info, COUNT(*) as count FROM users
UNION ALL
SELECT 'Pending registrations restantes:', COUNT(*) FROM pending_registrations
UNION ALL
SELECT 'Refresh tokens restants:', COUNT(*) FROM refresh_tokens;

COMMIT;
