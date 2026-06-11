#!/bin/bash

# ========================================
# Script de nettoyage de la base de données Frollot
# ========================================
# Ce script supprime TOUTES les données de la base de données
# ATTENTION : Cette opération est IRRÉVERSIBLE
# ========================================

# Configuration par défaut (peut être surchargée par variables d'environnement)
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-coiffure_db}"
DB_USER="${DB_USER:-coiffure_user}"
DB_PASSWORD="${DB_PASSWORD:?Erreur: la variable DB_PASSWORD doit être définie}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SQL_FILE="$SCRIPT_DIR/clean_database.sql"

echo "=========================================="
echo "NETTOYAGE DE LA BASE DE DONNÉES FROLLOT"
echo "=========================================="
echo ""
echo "⚠️  ATTENTION : Cette opération va supprimer TOUTES les données !"
echo ""
read -p "Êtes-vous sûr de vouloir continuer ? (tapez 'OUI' pour confirmer) : " confirmation

if [ "$confirmation" != "OUI" ]; then
    echo "❌ Opération annulée."
    exit 1
fi

echo ""
echo "🔍 Connexion à la base de données..."
echo "   Host: $DB_HOST"
echo "   Port: $DB_PORT"
echo "   Database: $DB_NAME"
echo "   User: $DB_USER"
echo ""

# Exécuter le script SQL
mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" < "$SQL_FILE"

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Base de données nettoyée avec succès !"
    echo ""
    echo "📊 Vérification des tables..."
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -e "
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
    "
    echo ""
    echo "✅ Nettoyage terminé !"
else
    echo ""
    echo "❌ Erreur lors du nettoyage de la base de données."
    exit 1
fi

