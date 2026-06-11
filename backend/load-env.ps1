param(
    [string]$Command = "bootRun"
)

if (Test-Path ".env") {
    Write-Host "📋 Chargement des variables d'environnement depuis .env..." -ForegroundColor Cyan
    
    Get-Content .env | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#")) {
            if ($line -match '^([^=]+)=(.*)$') {
                $key = $matches[1].Trim()
                $value = $matches[2].Trim()
                
                # Supprimer les guillemets si présents
                if ($value -match '^"(.*)"$' -or $value -match "^'(.*)'$") {
                    $value = $matches[1]
                }
                
                # Exporter dans le processus actuel
                [Environment]::SetEnvironmentVariable($key, $value, "Process")
                Write-Host "  ✓ $key" -ForegroundColor Green
            }
        }
    }
    
    Write-Host "✅ Variables d'environnement chargées avec succès!" -ForegroundColor Green
    Write-Host ""
    
    if ($Command) {
        Write-Host "🚀 Lancement: gradlew $Command" -ForegroundColor Yellow
        Write-Host ""
        & .\gradlew $Command
    } else {
        Write-Host "Vous pouvez maintenant lancer: .\gradlew bootRun" -ForegroundColor Yellow
    }
} else {
    Write-Host "❌ Fichier .env introuvable!" -ForegroundColor Red
    Write-Host "Créez un fichier .env à partir de env-config.txt" -ForegroundColor Yellow
    exit 1
}
