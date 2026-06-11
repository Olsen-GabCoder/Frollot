-- Script pour corriger le problème de checksum Flyway pour la migration V016
-- 
-- Ce script supprime l'entrée V016 de l'historique Flyway pour permettre
-- sa réapplication avec le nouveau checksum.
--
-- INSTRUCTIONS :
-- 1. Exécutez ce script dans votre base de données MySQL
-- 2. Relancez l'application Spring Boot
-- 3. Flyway réappliquera automatiquement V016 avec le bon checksum

DELETE FROM flyway_schema_history 
WHERE version = '016' AND description = 'create follows';

-- Vérification
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;

