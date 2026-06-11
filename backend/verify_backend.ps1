# Script pour vérifier que le backend fonctionne
Write-Host "🔍 Vérification du statut du backend..." -ForegroundColor Cyan

# Vérifier si MySQL fonctionne
Write-Host "🐬 Vérification MySQL..." -ForegroundColor Yellow
$mysqlCheck = docker ps --filter "name=frollot_mysql" --format "{{.Status}}" 2>$null
if ($mysqlCheck -and $mysqlCheck.Contains("Up")) {
    Write-Host "✅ MySQL fonctionne" -ForegroundColor Green
} else {
    Write-Host "❌ MySQL ne fonctionne pas" -ForegroundColor Red
    exit 1
}

# Vérifier si le processus Java fonctionne
Write-Host "☕ Vérification processus Java..." -ForegroundColor Yellow
$javaProcess = Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -like "*BackendApplication*" }
if ($javaProcess) {
    Write-Host "✅ Backend Java fonctionne (PID: $($javaProcess.Id))" -ForegroundColor Green
} else {
    Write-Host "❌ Aucun processus Java du backend trouvé" -ForegroundColor Red
}

# Tester la connectivité de l'API
Write-Host "🌐 Test de l'API..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:9090/manage/health" -TimeoutSec 5 -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ API répond correctement" -ForegroundColor Green
    } else {
        Write-Host "⚠️ API répond avec code $($response.StatusCode)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ API inaccessible : $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n🎉 Vérification terminée !" -ForegroundColor Magenta
