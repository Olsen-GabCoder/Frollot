# Script pour démarrer un serveur de développement web local
# À exécuter depuis le répertoire frontend/

param(
    [int]$Port = 8080
)

$distPath = "composeApp\build\distributions"

Write-Host "🌐 Démarrage du serveur web local..." -ForegroundColor Cyan
Write-Host "Port: $Port" -ForegroundColor White
Write-Host "Répertoire: $distPath" -ForegroundColor White
Write-Host ""

# Vérifier que le build existe
if (!(Test-Path $distPath)) {
    Write-Host "❌ Build non trouvé. Exécutez d'abord clean-build-web.ps1" -ForegroundColor Red
    exit 1
}

# Vérifier que index.html existe
$indexPath = Join-Path $distPath "index.html"
if (!(Test-Path $indexPath)) {
    Write-Host "❌ index.html non trouvé dans $distPath" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Fichiers de build trouvés" -ForegroundColor Green
Write-Host ""

# Démarrer le serveur Python
Write-Host "🚀 Serveur démarré sur http://localhost:$Port" -ForegroundColor Green
Write-Host "Appuyez sur Ctrl+C pour arrêter" -ForegroundColor Yellow
Write-Host ""

try {
    Push-Location $distPath
    python -m http.server $Port
} finally {
    Pop-Location
}

