# ============================================================================
# Script de redemarrage complet du projet Coiffure
# ============================================================================

# Configuration
$ErrorActionPreference = "Continue"
$ProgressPreference = "SilentlyContinue"

# Fonction pour afficher les messages avec couleurs
function Write-Step {
    param($Message, $Color = "Cyan")
    Write-Host "`n$Message" -ForegroundColor $Color
}

function Write-Success {
    param($Message)
    Write-Host "OK $Message" -ForegroundColor Green
}

function Write-ErrorCustom {
    param($Message)
    Write-Host "ERREUR $Message" -ForegroundColor Red
}

function Write-Info {
    param($Message)
    Write-Host "INFO $Message" -ForegroundColor Yellow
}

# ============================================================================
# DEBUT DU SCRIPT
# ============================================================================

Write-Host ""
Write-Host "============================================================" -ForegroundColor Magenta
Write-Host "  REDEMARRAGE COMPLET DU PROJET COIFFURE" -ForegroundColor Magenta
Write-Host "============================================================" -ForegroundColor Magenta
Write-Host ""

# ============================================================================
# ETAPE 1: ARRET DES SERVICES
# ============================================================================
Write-Step "Etape 1: Arret de tous les services Docker..." "Yellow"

try {
    docker compose down --remove-orphans 2>&1 | Out-Null
    Write-Success "Services Docker arretes"
} catch {
    Write-Info "Aucun service Docker a arreter"
}

# Arret des processus MySQL locaux
Write-Step "   Arret des processus MySQL locaux..." "Cyan"
try {
    Get-Process -Name mysqld -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
    Write-Success "Processus MySQL arretes"
} catch {
    Write-Info "Aucun processus MySQL local trouve"
}

# ============================================================================
# ETAPE 2: NETTOYAGE DES VOLUMES
# ============================================================================
Write-Step "Etape 2: Suppression des volumes Docker..." "Yellow"

# Liste des volumes a supprimer
$volumes = @(
    "backend_mysql_data",
    "backend_logs_data"
)

foreach ($volume in $volumes) {
    try {
        $exists = docker volume ls -q | Select-String -Pattern "^$volume$"
        if ($exists) {
            docker volume rm $volume -f 2>&1 | Out-Null
            Write-Success "Volume '$volume' supprime"
        } else {
            Write-Info "Volume '$volume' n'existe pas"
        }
    } catch {
        Write-Info "Impossible de supprimer '$volume'"
    }
}

# ============================================================================
# ETAPE 3: NETTOYAGE DES DONNEES LOCALES
# ============================================================================
Write-Step "Etape 3: Nettoyage des donnees locales..." "Yellow"

$dataDir = ".\data"
if (Test-Path $dataDir) {
    try {
        Remove-Item -Path $dataDir -Recurse -Force -ErrorAction SilentlyContinue
        Write-Success "Dossier 'data' supprime"
    } catch {
        Write-ErrorCustom "Impossible de supprimer le dossier 'data'"
    }
} else {
    Write-Info "Aucun dossier 'data' a supprimer"
}

# ============================================================================
# ETAPE 4: DEMARRAGE DE MYSQL
# ============================================================================
Write-Step "Etape 4: Demarrage de MySQL avec le nouveau schema..." "Yellow"

try {
    Write-Host "   Lancement du conteneur MySQL..." -ForegroundColor Cyan
    docker compose up -d mysql

    if ($LASTEXITCODE -eq 0) {
        Write-Success "Conteneur MySQL demarre"
    } else {
        Write-ErrorCustom "Echec du demarrage de MySQL"
        exit 1
    }
} catch {
    Write-ErrorCustom "Erreur lors du demarrage de MySQL"
    exit 1
}

# ============================================================================
# ETAPE 5: ATTENTE DE L'INITIALISATION
# ============================================================================
Write-Step "Etape 5: Attente de l'initialisation de MySQL..." "Yellow"

$maxAttempts = 30
$attempt = 0
$isReady = $false

Write-Host "   Verification de l'etat de MySQL" -ForegroundColor Cyan

while ($attempt -lt $maxAttempts -and -not $isReady) {
    $attempt++
    Write-Host "." -NoNewline -ForegroundColor Gray

    try {
        $result = docker exec frollot_mysql mysqladmin ping -u root -p"$env:MYSQL_ROOT_PASSWORD" 2>&1
        if ($result -match "mysqld is alive") {
            $isReady = $true
            Write-Host ""
            Write-Success "MySQL est pret (tentative $attempt/$maxAttempts)"
        }
    } catch {
        # Continue a attendre
    }

    if (-not $isReady) {
        Start-Sleep -Seconds 2
    }
}

if (-not $isReady) {
    Write-Host ""
    Write-ErrorCustom "MySQL n'a pas demarre dans le delai imparti"
    Write-Host "`nLogs de MySQL:" -ForegroundColor Yellow
    docker compose logs mysql
    exit 1
}

# ============================================================================
# ETAPE 6: VERIFICATION DE LA BASE DE DONNEES
# ============================================================================
Write-Step "Etape 6: Verification de la base de donnees..." "Yellow"

try {
    # Verifier la connexion
    Write-Host "   Test de connexion..." -ForegroundColor Cyan
    $testConnection = docker exec frollot_mysql mysql -u coiffure_user -p"$env:DB_PASSWORD" -e "SELECT 1;" 2>&1

    if ($LASTEXITCODE -eq 0) {
        Write-Success "Connexion a la base reussie"

        # Compter les tables
        Write-Host "   Verification des tables..." -ForegroundColor Cyan
        $tableCount = docker exec frollot_mysql mysql -u coiffure_user -p"$env:DB_PASSWORD" coiffure_db -e "SELECT COUNT(*) as count FROM information_schema.tables WHERE table_schema = 'coiffure_db';" -s -N 2>&1

        if ($tableCount -match '^\d+$') {
            Write-Success "Base de donnees contient $tableCount tables"
        }
    } else {
        Write-ErrorCustom "Echec de la connexion a la base"
        exit 1
    }
} catch {
    Write-ErrorCustom "Erreur lors de la verification de la base"
    exit 1
}

# ============================================================================
# ETAPE 7: RESUME ET PROCHAINES ETAPES
# ============================================================================
Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host "  REDEMARRAGE TERMINE AVEC SUCCES !" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green
Write-Host ""

Write-Host "Resume:" -ForegroundColor Cyan
Write-Host "   - Services arretes" -ForegroundColor Gray
Write-Host "   - Volumes Docker supprimes" -ForegroundColor Gray
Write-Host "   - Donnees locales nettoyees" -ForegroundColor Gray
Write-Host "   - MySQL demarre avec nouveau schema" -ForegroundColor Gray
Write-Host "   - Base de donnees verifiee" -ForegroundColor Gray

Write-Host ""
Write-Host "Prochaines etapes:" -ForegroundColor Yellow
Write-Host "   1. Demarrer le backend:" -ForegroundColor White
Write-Host "      .\gradlew bootRun" -ForegroundColor Cyan
Write-Host ""
Write-Host "   2. Ou demarrer tous les services:" -ForegroundColor White
Write-Host "      docker compose up -d" -ForegroundColor Cyan
Write-Host ""

# Afficher l'etat des conteneurs
Write-Host "Etat actuel des conteneurs:" -ForegroundColor Yellow
docker ps --format "table {{.Names}}`t{{.Status}}`t{{.Ports}}"

Write-Host ""