@echo off
REM ========================================
REM Script de nettoyage de la base de données Frollot (Windows)
REM ========================================
REM Ce script supprime TOUTES les données de la base de données
REM ATTENTION : Cette opération est IRRÉVERSIBLE
REM ========================================

setlocal enabledelayedexpansion

REM Configuration par défaut
if "%DB_HOST%"=="" set DB_HOST=localhost
if "%DB_PORT%"=="" set DB_PORT=3306
if "%DB_NAME%"=="" set DB_NAME=coiffure_db
if "%DB_USER%"=="" set DB_USER=coiffure_user
if "%DB_PASSWORD%"=="" (
    echo ERREUR: la variable DB_PASSWORD doit etre definie
    exit /b 1
)

set SCRIPT_DIR=%~dp0
set SQL_FILE=%SCRIPT_DIR%clean_database.sql

echo ==========================================
echo NETTOYAGE DE LA BASE DE DONNÉES FROLLOT
echo ==========================================
echo.
echo ⚠️  ATTENTION : Cette opération va supprimer TOUTES les données !
echo.
set /p confirmation="Êtes-vous sûr de vouloir continuer ? (tapez OUI pour confirmer) : "

if not "%confirmation%"=="OUI" (
    echo ❌ Opération annulée.
    exit /b 1
)

echo.
echo 🔍 Connexion à la base de données...
echo    Host: %DB_HOST%
echo    Port: %DB_PORT%
echo    Database: %DB_NAME%
echo    User: %DB_USER%
echo.

REM Exécuter le script SQL
mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASSWORD% %DB_NAME% < "%SQL_FILE%"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Base de données nettoyée avec succès !
    echo.
    echo 📊 Vérification des tables...
    mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASSWORD% %DB_NAME% -e "SELECT 'users' as table_name, COUNT(*) as remaining_count FROM users UNION ALL SELECT 'salons', COUNT(*) FROM salons UNION ALL SELECT 'bookings', COUNT(*) FROM bookings UNION ALL SELECT 'posts', COUNT(*) FROM posts UNION ALL SELECT 'comments', COUNT(*) FROM comments UNION ALL SELECT 'refresh_tokens', COUNT(*) FROM refresh_tokens UNION ALL SELECT 'payments', COUNT(*) FROM payments;"
    echo.
    echo ✅ Nettoyage terminé !
) else (
    echo.
    echo ❌ Erreur lors du nettoyage de la base de données.
    exit /b 1
)

endlocal

