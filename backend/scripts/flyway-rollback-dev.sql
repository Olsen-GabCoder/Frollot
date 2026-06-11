-- ========================================
-- SCRIPT DE ROLLBACK FLYWAY (DÉVELOPPEMENT UNIQUEMENT)
-- ========================================
-- ATTENTION : À N'UTILISER QU'EN DÉVELOPPEMENT
-- En production, les rollbacks doivent être des migrations ascendantes
-- ========================================

-- Récupérer la dernière migration appliquée
SET @last_version = (SELECT version FROM flyway_schema_history WHERE success = 1 ORDER BY installed_rank DESC LIMIT 1);

-- Afficher la migration qui va être rollbackée
SELECT
    'ROLLBACK DE LA MIGRATION:' as action,
    version,
    description,
    installed_on
FROM flyway_schema_history
WHERE version = @last_version;

-- MIGRATION SPÉCIFIQUE: V038 - Suppression des colonnes de reset password
-- (À adapter selon la migration à rollbacker)
SET @migration_version = '038';

DELIMITER //

CREATE PROCEDURE rollback_v038()
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        SELECT 'ERREUR LORS DU ROLLBACK V038' as error;
        ROLLBACK;
    END;

    START TRANSACTION;

    -- Supprimer les colonnes ajoutées par V038
    ALTER TABLE users DROP COLUMN password_reset_token;
    ALTER TABLE users DROP COLUMN password_reset_token_expiry;

    -- Supprimer l'entrée de l'historique Flyway
    DELETE FROM flyway_schema_history WHERE version = @migration_version;

    SELECT CONCAT('ROLLBACK V038 RÉUSSI - Colonnes supprimées et historique nettoyé') as result;

    COMMIT;
END //

DELIMITER ;

-- Exécuter le rollback si c'est la version V038
SET @current_version = (SELECT version FROM flyway_schema_history WHERE success = 1 ORDER BY installed_rank DESC LIMIT 1);

-- Vérification de sécurité: ne rollbacker que si c'est la dernière migration
IF @current_version = @migration_version THEN
    CALL rollback_v038();
ELSE
    SELECT CONCAT('ROLLBACK ANNULÉ: La dernière migration n\'est pas V038 (c\'est V', @current_version, ')') as warning;
END IF;

-- Nettoyer la procédure
DROP PROCEDURE IF EXISTS rollback_v038;

-- Vérifier l'état final
SELECT
    'ÉTAT FINAL APRÈS ROLLBACK:' as status,
    COUNT(*) as migrations_restantes
FROM flyway_schema_history
WHERE success = 1;

-- ========================================
-- INSTRUCTIONS POST-ROLLBACK
-- ========================================
/*
APRÈS LE ROLLBACK:

1. Supprimer ou corriger le fichier de migration V038__*.sql
2. Recréer la migration avec le bon contenu si nécessaire
3. Tester la migration corrigée:
   - Supprimer flyway_schema_history si nécessaire pour test complet
   - Relancer l'application

Rappel: En production, utiliser des migrations ascendantes au lieu du rollback
*/