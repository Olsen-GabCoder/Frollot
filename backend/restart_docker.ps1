# Script PowerShell pour redémarrer Docker proprement
Write-Host "🐳 Redémarrage propre de Docker..." -ForegroundColor Yellow

Write-Host "🔽 Arrêt des services Docker..." -ForegroundColor Cyan
docker compose down -v 2>$null

Write-Host "🧹 Nettoyage des ressources Docker..." -ForegroundColor Cyan
docker system prune -f 2>$null

Write-Host "🔼 Redémarrage des services..." -ForegroundColor Cyan
docker compose up -d mysql

Write-Host "⏳ Attente de l'initialisation de MySQL (15 secondes)..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

Write-Host "✅ Docker redémarré. La base de données devrait maintenant utiliser le nouveau schema.sql" -ForegroundColor Green
