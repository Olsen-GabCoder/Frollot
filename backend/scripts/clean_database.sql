-- ========================================
-- SCRIPT DE NETTOYAGE COMPLET DE LA BASE DE DONNÉES
-- ========================================
-- Ce script supprime TOUTES les données de toutes les tables
-- ATTENTION : Cette opération est IRRÉVERSIBLE
-- ========================================

SET FOREIGN_KEY_CHECKS = 0;

-- Supprimer toutes les données des tables dans l'ordre inverse des dépendances
-- Les erreurs sont ignorées si les tables n'existent pas

-- Tables de relations et associations
DELETE FROM collection_posts WHERE 1=1;
DELETE FROM post_hashtags WHERE 1=1;
DELETE FROM post_tags WHERE 1=1;
DELETE FROM post_services WHERE 1=1;
DELETE FROM post_reactions WHERE 1=1;
DELETE FROM post_shares WHERE 1=1;
DELETE FROM post_archives WHERE 1=1;
DELETE FROM post_favorites WHERE 1=1;
DELETE FROM post_likes WHERE 1=1;
DELETE FROM post_media WHERE 1=1;
DELETE FROM portfolio_posts WHERE 1=1;
DELETE FROM user_badges WHERE 1=1;
DELETE FROM user_specialties WHERE 1=1;
DELETE FROM salon_highlighted_posts WHERE 1=1;
DELETE FROM moderation_actions WHERE 1=1;
DELETE FROM reports WHERE 1=1;
DELETE FROM follows WHERE 1=1;
DELETE FROM comments WHERE 1=1;
DELETE FROM posts WHERE 1=1;
DELETE FROM collections WHERE 1=1;
DELETE FROM badges WHERE 1=1;
DELETE FROM portfolios WHERE 1=1;
DELETE FROM hair_hashtags WHERE 1=1;

-- Tables de réservations et files d'attente
DELETE FROM queue_entries WHERE 1=1;
DELETE FROM waiting_queues WHERE 1=1;
DELETE FROM waiting_queue WHERE 1=1;
DELETE FROM bookings WHERE 1=1;
DELETE FROM reviews WHERE 1=1;

-- Tables de paiements
DELETE FROM payments WHERE 1=1;

-- Tables de services
DELETE FROM salon_services WHERE 1=1;
DELETE FROM services WHERE 1=1;
DELETE FROM salon_staff_specialties WHERE 1=1;
DELETE FROM salon_staff WHERE 1=1;

-- Tables de salons
DELETE FROM salons WHERE 1=1;

-- Tables de tokens et sessions
DELETE FROM device_tokens WHERE 1=1;
DELETE FROM refresh_tokens WHERE 1=1;

-- Table principale des utilisateurs (doit être supprimée en dernier)
DELETE FROM users WHERE 1=1;

-- Réinitialiser les compteurs AUTO_INCREMENT si nécessaire
-- (Non applicable pour les UUID, mais utile pour les tables avec ID auto-incrémentés)

SET FOREIGN_KEY_CHECKS = 1;

-- Vérification : compter les enregistrements restants
SELECT 
    'users' as table_name, COUNT(*) as remaining_count FROM users
UNION ALL
SELECT 'salons', COUNT(*) FROM salons
UNION ALL
SELECT 'bookings', COUNT(*) FROM bookings
UNION ALL
SELECT 'posts', COUNT(*) FROM posts
UNION ALL
SELECT 'comments', COUNT(*) FROM comments
UNION ALL
SELECT 'refresh_tokens', COUNT(*) FROM refresh_tokens
UNION ALL
SELECT 'payments', COUNT(*) FROM payments;

-- Message de confirmation
SELECT '✅ Base de données nettoyée avec succès' as status;

