# Script de nettoyage et rebuild complet pour le frontend Web Kotlin/JS
# À exécuter depuis le répertoire frontend/

Write-Host "🧹 Nettoyage complet du frontend..." -ForegroundColor Cyan

# Nettoyer les caches Gradle
Write-Host "🗑️ Suppression des caches Gradle..." -ForegroundColor Yellow
if (Test-Path ".gradle") {
    Remove-Item -Recurse -Force ".gradle"
}
if (Test-Path "gradle\wrapper\gradle-wrapper.jar") {
    # Ne pas supprimer le wrapper Gradle
}

# Nettoyer les builds
Write-Host "🗑️ Suppression des builds..." -ForegroundColor Yellow
if (Test-Path "build") {
    Remove-Item -Recurse -Force "build"
}
if (Test-Path "composeApp\build") {
    Remove-Item -Recurse -Force "composeApp\build"
}
if (Test-Path "composeApp\build\js") {
    Remove-Item -Recurse -Force "composeApp\build\js"
}

# Nettoyer les node_modules et lock files
Write-Host "🗑️ Suppression des dépendances npm..." -ForegroundColor Yellow
if (Test-Path "composeApp\node_modules") {
    Remove-Item -Recurse -Force "composeApp\node_modules"
}
if (Test-Path "composeApp\package-lock.json") {
    Remove-Item "composeApp\package-lock.json"
}
if (Test-Path "composeApp\yarn.lock") {
    Remove-Item "composeApp\yarn.lock"
}
if (Test-Path "kotlin-js-store\yarn.lock") {
    Remove-Item "kotlin-js-store\yarn.lock"
}

Write-Host "✅ Nettoyage terminé" -ForegroundColor Green
Write-Host ""

# Rebuild complet
Write-Host "🔨 Rebuild complet du frontend..." -ForegroundColor Cyan

# Installer les dépendances npm
Write-Host "📦 Installation des dépendances npm..." -ForegroundColor Yellow
Push-Location "composeApp"
try {
    npm install
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Erreur lors de l'installation npm" -ForegroundColor Red
        exit 1
    }
} finally {
    Pop-Location
}

Write-Host "✅ Dépendances npm installées" -ForegroundColor Green

# Build Gradle avec clean
 

# Build spécifique JS
Write-Host "🔨 Build Kotlin/JS..." -ForegroundColor Yellow
    ./gradlew :composeApp:wasmJsBrowserProductionWebpack --no-daemon
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Erreur lors du build Kotlin/JS" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Build Kotlin/JS réussi" -ForegroundColor Green

# Copie des fichiers de build vers le répertoire de distribution attendu
Write-Host "🚚 Copie des fichiers de build vers le répertoire de distribution..." -ForegroundColor Yellow
$distDir = "composeApp\build\distributions"
if (!(Test-Path $distDir)) {
    New-Item -ItemType Directory -Force $distDir
}

Copy-Item -Path "composeApp\build\kotlin-webpack\wasmJs\productionExecutable\composeApp.js" -Destination $distDir -Force
Copy-Item -Path "composeApp\build\kotlin-webpack\wasmJs\productionExecutable\dd568dbcd078c0adf7cf.wasm" -Destination $distDir -Force
Rename-Item -Path (Join-Path $distDir "dd568dbcd078c0adf7cf.wasm") -NewName "composeApp.wasm" -Force
 
Copy-Item -Path "composeApp\build\processedResources\wasmJs\main\index.html" -Destination $distDir -Force
Copy-Item -Path "composeApp\src\wasmJsMain\resources\styles.css" -Destination $distDir -Force

Write-Host "✅ Fichiers copiés" -ForegroundColor Green

Write-Host ""
Write-Host "🎉 Rebuild complet terminé avec succès !" -ForegroundColor Green
Write-Host ""
Write-Host "Pour tester l'application web :" -ForegroundColor Cyan
Write-Host "1. Naviguez vers composeApp/build/distributions" -ForegroundColor White
Write-Host "2. Ouvrez index.html dans un navigateur" -ForegroundColor White
Write-Host "3. Ou utilisez un serveur local : python -m http.server 8080" -ForegroundColor White

