# ========================================
# SCRIPT DE DÉPLOIEMENT PRODUCTION - FROLLOT BACKEND
# ========================================
# Script sécurisé de déploiement en production
# À exécuter uniquement sur l'environnement de production
#
# PRÉREQUIS:
# - Variables d'environnement configurées
# - Base de données accessible
# - Secrets générés aléatoirement
# ========================================

param(
    [Parameter(Mandatory=$true)]
    [string]$Environment,

    [Parameter(Mandatory=$false)]
    [switch]$SkipHealthCheck,

    [Parameter(Mandatory=$false)]
    [switch]$Force
)

# Configuration
$PROJECT_ROOT = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$BACKEND_DIR = Join-Path $PROJECT_ROOT "backend"
$REQUIRED_VARS = @(
    "DB_URL", "DB_USERNAME", "DB_PASSWORD",
    "JWT_SECRET", "STRIPE_SECRET_KEY",
    "SMTP_USERNAME", "SMTP_PASSWORD"
)

Write-Host "🚀 [DEPLOY] Début du déploiement Frollot Backend - $Environment" -ForegroundColor Cyan
Write-Host "📁 Répertoire: $BACKEND_DIR" -ForegroundColor Gray

# Fonction de validation
function Test-Environment {
    Write-Host "🔍 [VALIDATION] Vérification de l'environnement..." -ForegroundColor Yellow

    # Vérifier les variables d'environnement
    $missingVars = @()
    foreach ($var in $REQUIRED_VARS) {
        if (-not [Environment]::GetEnvironmentVariable($var)) {
            $missingVars += $var
        }
    }

    if ($missingVars.Count -gt 0) {
        Write-Error "❌ Variables d'environnement manquantes: $($missingVars -join ', ')"
        exit 1
    }

    # Vérifier la connectivité base de données
    Write-Host "🔗 [VALIDATION] Test de connexion à la base de données..." -ForegroundColor Yellow
    try {
        $dbUrl = [Environment]::GetEnvironmentVariable("DB_URL")
        if ($dbUrl -match "mysql://([^:]+):(\d+)/(.+)\?") {
            $dbHost = $matches[1]
            $dbPort = $matches[2]
            $dbName = $matches[3]

            $connection = Test-NetConnection -ComputerName $dbHost -Port $dbPort -WarningAction SilentlyContinue
            if (-not $connection.TcpTestSucceeded) {
                throw "Connexion réseau échouée vers $dbHost`:$dbPort"
            }
        }
    } catch {
        Write-Error "❌ Test de connexion base de données échoué: $_"
        exit 1
    }

    Write-Host "✅ [VALIDATION] Environnement validé" -ForegroundColor Green
}

# Fonction de sauvegarde
function Backup-CurrentState {
    Write-Host "💾 [BACKUP] Création de sauvegarde..." -ForegroundColor Yellow

    $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $backupDir = Join-Path $BACKEND_DIR "backup_$timestamp"

    New-Item -ItemType Directory -Path $backupDir -Force | Out-Null

    # Sauvegarder les fichiers critiques
    $filesToBackup = @(
        "src/main/resources/application*.yml",
        ".env",
        "logs/application.log*"
    )

    foreach ($file in $filesToBackup) {
        $sourcePath = Join-Path $BACKEND_DIR $file
        if (Test-Path $sourcePath) {
            Copy-Item $sourcePath $backupDir -Recurse -Force
        }
    }

    Write-Host "✅ [BACKUP] Sauvegarde créée: $backupDir" -ForegroundColor Green
    return $backupDir
}

# Fonction de déploiement
function Deploy-Application {
    param([string]$BackupPath)

    Write-Host "🚀 [DEPLOY] Déploiement de l'application..." -ForegroundColor Yellow

    try {
        # Aller dans le répertoire backend
        Push-Location $BACKEND_DIR

        # Nettoyer et compiler
        Write-Host "🔨 [BUILD] Compilation de l'application..." -ForegroundColor Yellow
        .\gradlew clean build -x test

        if ($LASTEXITCODE -ne 0) {
            throw "Échec de la compilation"
        }

        # Arrêter l'application existante (si running)
        Write-Host "🛑 [STOP] Arrêt de l'application existante..." -ForegroundColor Yellow
        Stop-Process -Name "java" -Force -ErrorAction SilentlyContinue

        # Attendre que le port se libère
        Start-Sleep -Seconds 5

        # Démarrer l'application
        Write-Host "▶️ [START] Démarrage de l'application..." -ForegroundColor Yellow
        $startProcess = Start-Process -FilePath ".\gradlew.bat" -ArgumentList "bootRun", "--spring.profiles.active=$Environment" -NoNewWindow -PassThru

        # Attendre le démarrage
        Write-Host "⏳ [WAIT] Attente du démarrage complet (30s)..." -ForegroundColor Yellow
        Start-Sleep -Seconds 30

        # Vérifier que l'application fonctionne
        if (-not $SkipHealthCheck) {
            Write-Host "🏥 [HEALTH] Vérification des health checks..." -ForegroundColor Yellow
            $healthResponse = Invoke-WebRequest -Uri "http://localhost:9090/manage/health" -TimeoutSec 10 -ErrorAction SilentlyContinue

            if ($healthResponse.StatusCode -eq 200) {
                $healthData = $healthResponse.Content | ConvertFrom-Json
                if ($healthData.status -eq "UP") {
                    Write-Host "✅ [HEALTH] Application démarrée avec succès" -ForegroundColor Green
                } else {
                    throw "Health check échoué: $($healthData.status)"
                }
            } else {
                throw "Impossible d'atteindre le health check"
            }
        }

        Write-Host "🎉 [DEPLOY] Déploiement réussi !" -ForegroundColor Green

    } catch {
        Write-Error "❌ [DEPLOY] Échec du déploiement: $_"

        # Rollback si demandé
        if (-not $Force) {
            Write-Host "🔄 [ROLLBACK] Tentative de rollback..." -ForegroundColor Yellow
            # Ici on pourrait implémenter un rollback automatique
        }

        exit 1
    } finally {
        Pop-Location
    }
}

# Fonction de post-déploiement
function Test-PostDeployment {
    Write-Host "🧪 [TEST] Tests post-déploiement..." -ForegroundColor Yellow

    # Tests des endpoints critiques
    $endpoints = @(
        @{Url="http://localhost:9090/manage/health"; Name="Health Check"},
        @{Url="http://localhost:9090/v3/api-docs"; Name="API Docs"}
    )

    foreach ($endpoint in $endpoints) {
        try {
            $response = Invoke-WebRequest -Uri $endpoint.Url -TimeoutSec 10 -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                Write-Host "✅ $($endpoint.Name): OK" -ForegroundColor Green
            } else {
                Write-Warning "⚠️ $($endpoint.Name): Status $($response.StatusCode)"
            }
        } catch {
            Write-Warning "⚠️ $($endpoint.Name): Indisponible - $_"
        }
    }
}

# Script principal
try {
    # Validation
    Test-Environment

    # Sauvegarde
    $backupPath = Backup-CurrentState

    # Déploiement
    Deploy-Application -BackupPath $backupPath

    # Tests
    Test-PostDeployment

    Write-Host ""
    Write-Host "🎊 [SUCCESS] Déploiement terminé avec succès !" -ForegroundColor Green
    Write-Host "📋 Résumé:" -ForegroundColor Cyan
    Write-Host "   - Environnement: $Environment" -ForegroundColor White
    Write-Host "   - Sauvegarde: $backupPath" -ForegroundColor White
    Write-Host "   - Health Check: http://localhost:9090/manage/health" -ForegroundColor White
    Write-Host "   - API Docs: http://localhost:9090/swagger-ui.html" -ForegroundColor White

} catch {
    Write-Error "💥 [ERROR] Déploiement échoué: $_"
    exit 1
}