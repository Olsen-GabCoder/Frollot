-- ========================================
-- GARDE DE SÉCURITÉ FLYWAY - PROTECTION CONTRE LES MODIFICATIONS
-- ========================================
-- Ce script vérifie l'intégrité des migrations appliquées
-- et prévient les modifications dangereuses
-- ========================================

DELIMITER //

CREATE PROCEDURE check_flyway_integrity()
BEGIN
    DECLARE checksum_mismatch INT DEFAULT 0;
    DECLARE failed_migrations INT DEFAULT 0;
    DECLARE out_of_order_migrations INT DEFAULT 0;

    -- Vérifier les checksums des migrations appliquées
    SELECT COUNT(*) INTO checksum_mismatch
    FROM flyway_schema_history
    WHERE success = 1
      AND checksum IS NOT NULL
      AND checksum != (
          SELECT checksum FROM flyway_schema_history fsh2
          WHERE fsh2.version = flyway_schema_history.version
          AND fsh2.success = 1
          ORDER BY installed_rank DESC LIMIT 1
      );

    -- Vérifier les migrations échouées
    SELECT COUNT(*) INTO failed_migrations
    FROM flyway_schema_history
    WHERE success = 0;

    -- Vérifier les migrations out-of-order (si configuré)
    SELECT COUNT(*) INTO out_of_order_migrations
    FROM flyway_schema_history
    WHERE installed_rank != (
        SELECT COUNT(*) FROM flyway_schema_history fsh2
        WHERE fsh2.installed_on <= flyway_schema_history.installed_on
    );

    -- Rapport des problèmes
    SELECT
        'RAPPORT D\'INTÉGRITÉ FLYWAY' as title,
        CASE
            WHEN checksum_mismatch = 0 AND failed_migrations = 0 THEN '✅ INTÉGRITÉ OK'
            ELSE '❌ PROBLÈMES DÉTECTÉS'
        END as status;

    IF checksum_mismatch > 0 THEN
        SELECT CONCAT('🚨 CHECKSUM MISMATCH: ', checksum_mismatch, ' migration(s) ont un checksum différent') as alert;
    END IF;

    IF failed_migrations > 0 THEN
        SELECT CONCAT('🚨 MIGRATIONS ÉCHOUÉES: ', failed_migrations, ' migration(s) en erreur') as alert;
    END IF;

    IF out_of_order_migrations > 0 THEN
        SELECT CONCAT('⚠️ OUT-OF-ORDER: ', out_of_order_migrations, ' migration(s) appliquées hors ordre') as warning;
    END IF;

    -- Lister les migrations problématiques
    IF checksum_mismatch > 0 OR failed_migrations > 0 THEN
        SELECT
            'MIGRATIONS PROBLÉMATIQUES:' as section,
            version,
            description,
            success,
            checksum,
            installed_on
        FROM flyway_schema_history
        WHERE success = 0
           OR (success = 1 AND checksum IS NOT NULL)
        ORDER BY installed_rank DESC
        LIMIT 10;
    END IF;

    -- Statistiques générales
    SELECT
        'STATISTIQUES:' as section,
        COUNT(*) as total_migrations,
        SUM(CASE WHEN success = 1 THEN 1 ELSE 0 END) as successful_migrations,
        SUM(CASE WHEN success = 0 THEN 1 ELSE 0 END) as failed_migrations,
        MAX(installed_on) as last_migration_date
    FROM flyway_schema_history;

END //

DELIMITER ;

-- Exécuter la vérification d'intégrité
CALL check_flyway_integrity();

-- Nettoyer la procédure
DROP PROCEDURE IF EXISTS check_flyway_integrity;

-- ========================================
-- RÈGLES DE SÉCURITÉ FLYWAY
-- ========================================
/*
RÈGLES STRICTES À RESPECTER:

1. JAMAIS modifier une migration déjà appliquée en production
2. Les migrations sont IMMUABLES une fois versionnées
3. En cas d'erreur, créer une NOUVELLE migration corrective
4. Utiliser flyway repair UNIQUEMENT en développement
5. Tester TOUTES les migrations sur une base vierge avant commit

COMMANDES DE SECOURS (DÉVELOPPEMENT UNIQUEMENT):
- flyway repair: Recalcule les checksums
- Suppression manuelle de flyway_schema_history: Reset complet (DANGER)
- Rollback scripts: Pour développement uniquement
*/