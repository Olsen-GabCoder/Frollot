-- ========================================
-- SCRIPT DE NETTOYAGE COMPLET DE LA BASE DE DONNÉES (Version automatique)
-- ========================================
-- Ce script supprime TOUTES les données de toutes les tables existantes
-- ATTENTION : Cette opération est IRRÉVERSIBLE
-- ========================================

SET FOREIGN_KEY_CHECKS = 0;

-- Supprimer toutes les données des tables existantes
-- Le script ignore les erreurs si une table n'existe pas

-- Tables de relations et associations (avec gestion d'erreur)
SET @sql = '';
SELECT GROUP_CONCAT(CONCAT('DELETE FROM ', table_name, ';') SEPARATOR ' ')
INTO @sql
FROM information_schema.tables
WHERE table_schema = 'coiffure_db'
AND table_name IN (
    'collection_posts', 'post_hashtags', 'post_tags', 'post_services',
    'post_reactions', 'post_shares', 'post_archives', 'post_favorites',
    'post_likes', 'post_media', 'portfolio_posts', 'user_badges',
    'user_specialties', 'salon_highlighted_posts', 'moderation_actions',
    'reports', 'follows', 'comments', 'posts', 'collections', 'badges',
    'portfolios', 'hair_hashtags', 'queue_entries', 'waiting_queues',
    'waiting_queue', 'bookings', 'reviews', 'payments', 'salon_services',
    'services', 'salon_staff_specialties', 'salon_staff', 'salons',
    'device_tokens', 'refresh_tokens', 'users'
);

SET @sql = IFNULL(@sql, 'SELECT "Aucune table à nettoyer" as message;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;

-- Vérification : compter les enregistrements restants dans les tables existantes
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

