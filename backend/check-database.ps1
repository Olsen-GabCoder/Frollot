# Script de verification de la base de donnees
Write-Host "Verification de la structure de la base de donnees..." -ForegroundColor Cyan
Write-Host ""

# 1. Lister toutes les tables
Write-Host "=== TABLES EXISTANTES ===" -ForegroundColor Yellow
docker exec frollot_mysql mysql -u coiffure_user -p"$env:DB_PASSWORD" coiffure_db -e "SHOW TABLES;" 2>$null

Write-Host ""

# 2. Structure de la table salon_staff
Write-Host "=== STRUCTURE DE salon_staff ===" -ForegroundColor Yellow
docker exec frollot_mysql mysql -u coiffure_user -p"$env:DB_PASSWORD" coiffure_db -e "DESCRIBE salon_staff;" 2>$null

Write-Host ""

# 3. Structure de la table users
Write-Host "=== STRUCTURE DE users ===" -ForegroundColor Yellow
docker exec frollot_mysql mysql -u coiffure_user -p"$env:DB_PASSWORD" coiffure_db -e "DESCRIBE users;" 2>$null

Write-Host ""

# 4. Structure de toutes les autres tables
Write-Host "=== TOUTES LES COLONNES DE TOUTES LES TABLES ===" -ForegroundColor Yellow
docker exec frollot_mysql mysql -u coiffure_user -p"$env:DB_PASSWORD" coiffure_db -e "SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'coiffure_db' ORDER BY TABLE_NAME, ORDINAL_POSITION;" 2>$null

Write-Host ""
Write-Host "Verification terminee." -ForegroundColor Green