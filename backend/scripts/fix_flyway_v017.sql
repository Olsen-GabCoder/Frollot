-- Script pour corriger le problème de migration V017 échouée
-- 
-- Ce script supprime l'entrée V017 de l'historique Flyway pour permettre
-- sa réapplication avec la version corrigée.
--
-- INSTRUCTIONS :
-- 1. Exécutez ce script dans votre base de données MySQL
-- 2. Relancez l'application Spring Boot
-- 3. Flyway réappliquera automatiquement V017 avec la version corrigée

-- Supprimer l'entrée V017 de l'historique Flyway
DELETE FROM flyway_schema_history 
WHERE version = '017' AND description = 'fix follows following id type';

-- Vérification
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;

