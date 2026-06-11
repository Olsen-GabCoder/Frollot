@echo off
REM Script batch pour charger les variables d'environnement depuis .env et lancer l'application
REM Usage: start-with-env.bat

echo 📋 Chargement des variables d'environnement depuis .env...

if not exist ".env" (
    echo ❌ Fichier .env introuvable!
    echo Créez un fichier .env à partir de env-config.txt
    pause
    exit /b 1
)

REM Charger les variables depuis .env
for /f "usebackq tokens=1,* delims==" %%a in (".env") do (
    set "line=%%a"
    if not "!line:~0,1!"=="#" (
        if not "!line!"=="" (
            set "%%a=%%b"
            REM Exporter la variable d'environnement
            call set "%%a=%%b"
        )
    )
)

REM Méthode alternative : charger ligne par ligne
for /f "usebackq delims=" %%a in (".env") do (
    set "line=%%a"
    if not "!line:~0,1!"=="#" (
        for /f "tokens=1,* delims==" %%b in ("!line!") do (
            set "key=%%b"
            set "value=%%c"
            if not "!key!"=="" (
                set "!key!=!value!"
                REM Exporter en variable d'environnement système
                call setx "!key!" "!value!" >nul 2>&1
            )
        )
    )
)

echo ✅ Variables chargées
echo.
echo 🚀 Lancement de l'application...
echo.

REM Lancer l'application
call gradlew bootRun

