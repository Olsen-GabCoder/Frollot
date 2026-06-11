# Script de verification de l'etat de la base de donnees
Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  VERIFICATION DE LA BASE DE DONNEES" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

# Compter toutes les lignes
Write-Host "Comptage des lignes dans toutes les tables..." -ForegroundColor Yellow
Write-Host ""

$result = docker exec frollot_mysql mysql -u coiffure_user -p"$env:DB_PASSWORD" coiffure_db -e "SELECT table_name as TableName, table_rows as Lignes FROM information_schema.tables WHERE table_schema = 'coiffure_db' AND table_type = 'BASE TABLE' ORDER BY table_rows DESC, table_name;" 2>$null

Write-Host $result

# Calculer le total
Write-Host ""
Write-Host "Resume:" -ForegroundColor Yellow

$totalRows = docker exec frollot_mysql mysql -u coiffure_user -p"$env:DB_PASSWORD" coiffure_db -e "SELECT SUM(table_rows) FROM information_schema.tables WHERE table_schema = 'coiffure_db' AND table_type = 'BASE TABLE';" -s -N 2>$null

$tableCount = docker exec frollot_mysql mysql -u coiffure_user -p"$env:DB_PASSWORD" coiffure_db -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'coiffure_db' AND table_type = 'BASE TABLE';" -s -N 2>$null

Write-Host "  Nombre de tables: $tableCount" -ForegroundColor Cyan
Write-Host "  Total de lignes: $totalRows" -ForegroundColor Cyan

if ($totalRows -eq 0 -or $totalRows -eq "") {
    Write-Host ""
    Write-Host "BASE DE DONNEES VIDE" -ForegroundColor Green
} elseif ($totalRows -lt 10) {
    Write-Host ""
    Write-Host "BASE DE DONNEES QUASI-VIDE (donnees initiales uniquement)" -ForegroundColor Yellow
} else {
    Write-Host ""
    Write-Host "BASE DE DONNEES CONTIENT DES DONNEES" -ForegroundColor Red
}

Write-Host ""