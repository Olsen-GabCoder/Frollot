# Script de redemarrage complet avec nouveau schema
Write-Host ""
Write-Host "============================================================" -ForegroundColor Magenta
Write-Host "  REDEMARRAGE COMPLET AVEC NOUVEAU SCHEMA" -ForegroundColor Magenta
Write-Host "============================================================" -ForegroundColor Magenta
Write-Host ""

# Etape 1: Arreter tous les services
Write-Host "Etape 1: Arret des services..." -ForegroundColor Yellow
docker compose down --remove-orphans
Write-Host "OK Services arretes" -ForegroundColor Green

# Etape 2: Supprimer les volumes
Write-Host "`nEtape 2: Suppression des volumes..." -ForegroundColor Yellow
docker volume rm backend_mysql_data -f 2>$null
docker volume rm backend_logs_data -f 2>$null
Write-Host "OK Volumes supprimes" -ForegroundColor Green

# Etape 3: Supprimer le dossier data
Write-Host "`nEtape 3: Nettoyage du dossier data..." -ForegroundColor Yellow
if (Test-Path ".\data") {
    Remove-Item -Path ".\data" -Recurse -Force
    Write-Host "OK Dossier data supprime" -ForegroundColor Green
} else {
    Write-Host "INFO Aucun dossier data a supprimer" -ForegroundColor Yellow
}

# Etape 4: Verifier que schema.sql existe
Write-Host "`nEtape 4: Verification du schema.sql..." -ForegroundColor Yellow
if (Test-Path ".\src\main\resources\schema.sql") {
    Write-Host "OK schema.sql trouve" -ForegroundColor Green
} else {
    Write-Host "ERREUR schema.sql introuvable dans src\main\resources\" -ForegroundColor Red
    exit 1
}

# Etape 5: Demarrer MySQL
Write-Host "`nEtape 5: Demarrage de MySQL..." -ForegroundColor Yellow
docker compose up -d mysql

# Etape 6: Attendre que MySQL soit pret
Write-Host "`nEtape 6: Attente de MySQL..." -ForegroundColor Yellow
$maxAttempts = 30
$attempt = 0
$isReady = $false

while ($attempt -lt $maxAttempts -and -not $isReady) {
    $attempt++
    Write-Host "." -NoNewline -ForegroundColor Gray

    try {
        $result = docker exec frollot_mysql mysqladmin ping -u root -p"$env:MYSQL_ROOT_PASSWORD" 2>&1
        if ($result -match "mysqld is alive") {
            $isReady = $true
        }
    } catch {
        # Continue
    }

    if (-not $isReady) {
        Start-Sleep -Seconds 2
    }
}

Write-Host ""
if (-not $isReady) {
    Write-Host "ERREUR MySQL n'a pas demarre" -ForegroundColor Red
    docker compose logs mysql
    exit 1
}

Write-Host "OK MySQL est pret" -ForegroundColor Green

# Etape 7: Verifier la base
Write-Host "`nEtape 7: Verification de la base..." -ForegroundColor Yellow
$testConnection = docker exec frollot_mysql mysql -u coiffure_user -p"$env:DB_PASSWORD" -e "SELECT 1;" 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "OK Connexion reussie" -ForegroundColor Green

    # Compter les tables
    $tableCount = docker exec frollot_mysql mysql -u coiffure_user -p"$env:DB_PASSWORD" coiffure_db -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'coiffure_db';" -s -N 2>&1
    Write-Host "OK Base contient $tableCount tables" -ForegroundColor Green

    # Verifier salon_staff
    Write-Host "`nVerification de salon_staff..." -ForegroundColor Cyan
    docker exec frollot_mysql mysql -u coiffure_user -p"$env:DB_PASSWORD" coiffure_db -e "DESCRIBE salon_staff;" 2>&1

} else {
    Write-Host "ERREUR Connexion echouee" -ForegroundColor Red
    exit 1
}

# Etape 8: Résumé
Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host "  REDEMARRAGE TERMINE AVEC SUCCES" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green
Write-Host ""
Write-Host "Prochaine etape: Demarrer le backend" -ForegroundColor Yellow
Write-Host "  .\gradlew bootRun" -ForegroundColor Cyan
Write-Host ""