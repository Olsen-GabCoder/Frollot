# ========================================
# GÉNÉRATEUR DE SECRETS DE PRODUCTION - FROLLOT BACKEND
# ========================================
# Génère des secrets cryptographiques sécurisés pour la production
# Utilise des sources d'entropie système pour une sécurité maximale
# ========================================

param(
    [Parameter(Mandatory=$false)]
    [switch]$Force,

    [Parameter(Mandatory=$false)]
    [string]$OutputFile = "production-secrets.env"
)

Write-Host "🔐 [SECRETS] Génération des secrets de production Frollot" -ForegroundColor Cyan
Write-Host "📁 Fichier de sortie: $OutputFile" -ForegroundColor Gray

# Vérifier si le fichier existe déjà
if ((Test-Path $OutputFile) -and -not $Force) {
    Write-Warning "⚠️ Le fichier $OutputFile existe déjà. Utilisez -Force pour l'écraser."
    exit 1
}

# Fonction de génération de clé JWT sécurisée (512 bits minimum)
function New-JwtSecret {
    Write-Host "🔑 [JWT] Génération de la clé JWT (512 bits)..." -ForegroundColor Yellow
    try {
        # Utilise OpenSSL si disponible, sinon fallback sur .NET
        if (Get-Command openssl -ErrorAction SilentlyContinue) {
            $secret = & openssl rand -base64 64
            # Vérifier la longueur (openssl rand -base64 peut produire des sauts de ligne)
            $secret = $secret -replace "`n|`r", ""
        } else {
            # Fallback: génération .NET (moins sécurisée que OpenSSL)
            Write-Warning "⚠️ OpenSSL non trouvé, utilisation du générateur .NET (moins sécurisé)"
            $bytes = New-Object byte[] 64
            [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
            $secret = [Convert]::ToBase64String($bytes)
        }

        if ($secret.Length -lt 86) { # 64 bytes en base64 ≈ 86 caractères
            throw "Clé générée trop courte: $($secret.Length) caractères"
        }

        Write-Host "✅ [JWT] Clé JWT générée (${$secret.Length} caractères)" -ForegroundColor Green
        return $secret
    } catch {
        Write-Error "❌ Échec génération clé JWT: $_"
        exit 1
    }
}

# Fonction de génération de mot de passe DB sécurisé
function New-DbPassword {
    Write-Host "🛡️ [DB] Génération du mot de passe base de données..." -ForegroundColor Yellow

    # Caractères autorisés pour MySQL
    $chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#%^&*()-_=+[]{}|;:,.<>?"
    $password = ""

    # Générer un mot de passe de 32 caractères
    for ($i = 0; $i -lt 32; $i++) {
        $password += $chars[(Get-Random -Maximum $chars.Length)]
    }

    # Vérifier qu'il contient au moins un de chaque type
    $hasUpper = $password -match "[A-Z]"
    $hasLower = $password -match "[a-z]"
    $hasDigit = $password -match "[0-9]"
    $hasSpecial = $password -match "[!@#%^&*()\-_=+\[\]{}|;:,.<>?]"

    if (-not ($hasUpper -and $hasLower -and $hasDigit -and $hasSpecial)) {
        Write-Warning "⚠️ Mot de passe généré ne respecte pas tous les critères, régénération..."
        return New-DbPassword
    }

    Write-Host "✅ [DB] Mot de passe généré (32 caractères, complexe)" -ForegroundColor Green
    return $password
}

# Fonction de génération de webhook secret Stripe
function New-StripeWebhookSecret {
    Write-Host "💳 [STRIPE] Génération du secret webhook..." -ForegroundColor Yellow

    $bytes = New-Object byte[] 32
    [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
    $secret = "whsec_" + [Convert]::ToBase64String($bytes) -replace "[+/=]", {
        switch ($_) {
            "+" { "a" }
            "/" { "b" }
            "=" { "c" }
        }
    }

    Write-Host "✅ [STRIPE] Secret webhook généré" -ForegroundColor Green
    return $secret
}

# Génération des secrets
$secrets = [ordered]@{
    "# FROLLOT PRODUCTION SECRETS" = ""
    "# Généré le $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" = ""
    "# NE PAS COMMITTER CE FICHIER EN GIT" = ""
    "" = ""
    "# ========================================" = ""
    "# SÉCURITÉ JWT" = ""
    "# ========================================" = ""
    "JWT_SECRET" = New-JwtSecret
    "JWT_EXPIRATION_HOURS" = "168"  # 7 jours pour access token en prod
    "JWT_REFRESH_EXPIRATION_HOURS" = "8760"  # 1 an pour refresh token
    "" = ""
    "# ========================================" = ""
    "# BASE DE DONNÉES" = ""
    "# ========================================" = ""
    "# Remplissez ces valeurs selon votre infrastructure:" = ""
    "# DB_URL=jdbc:mysql://prod-db-host:3306/coiffure_db?useSSL=true&serverTimezone=UTC" = ""
    "# DB_USERNAME=coiffure_prod_user" = ""
    "DB_PASSWORD" = New-DbPassword
    "" = ""
    "# ========================================" = ""
    "# EMAIL SMTP" = ""
    "# ========================================" = ""
    "# Remplissez ces valeurs selon votre fournisseur:" = ""
    "# SMTP_HOST=smtp.gmail.com" = ""
    "# SMTP_PORT=587" = ""
    "# SMTP_USERNAME=your-smtp-username" = ""
    "# SMTP_PASSWORD=your-smtp-password" = ""
    "# EMAIL_FROM=noreply@frollot.com" = ""
    "" = ""
    "# ========================================" = ""
    "# STRIPE (PAIEMENTS)" = ""
    "# ========================================" = ""
    "# Remplissez avec vos vraies clés Stripe (production):" = ""
    "# STRIPE_SECRET_KEY=sk_live_..." = ""
    "# STRIPE_PUBLISHABLE_KEY=pk_live_..." = ""
    "STRIPE_WEBHOOK_SECRET" = New-StripeWebhookSecret
    "" = ""
    "# ========================================" = ""
    "# FIREBASE (NOTIFICATIONS)" = ""
    "# ========================================" = ""
    "# FIREBASE_ENABLED=true" = ""
    "# FIREBASE_SERVICE_ACCOUNT_PATH=config/firebase-service-account.json" = ""
    "" = ""
    "# ========================================" = ""
    "# LOGGING PRODUCTION" = ""
    "# ========================================" = ""
    "LOG_LEVEL_ROOT" = "INFO"
    "LOG_LEVEL_FROLLOT" = "INFO"
    "LOG_LEVEL_SPRING_WEB" = "WARN"
    "LOG_LEVEL_SPRING_SECURITY" = "INFO"
}

# Écriture du fichier
Write-Host "💾 [OUTPUT] Écriture du fichier de secrets..." -ForegroundColor Yellow

$content = $secrets.GetEnumerator() | ForEach-Object {
    $key = $_.Key
    $value = $_.Value

    if ($key -eq "" -or $key.StartsWith("#")) {
        $key
    } else {
        "$key=$value"
    }
}

$content | Out-File -FilePath $OutputFile -Encoding UTF8 -Force

Write-Host "✅ [OUTPUT] Fichier généré: $OutputFile" -ForegroundColor Green

# Validation finale
Write-Host "🔍 [VALIDATION] Validation des secrets générés..." -ForegroundColor Yellow

$jwtLength = $secrets["JWT_SECRET"].Length
$dbPassLength = $secrets["DB_PASSWORD"].Length

$validations = @(
    @{Test=$jwtLength -ge 86; Message="Clé JWT: $jwtLength caractères (requis: 86+)"},
    @{Test=$dbPassLength -eq 32; Message="Mot de passe DB: $dbPassLength caractères"},
    @{Test=$secrets["STRIPE_WEBHOOK_SECRET"].StartsWith("whsec_"); Message="Secret Stripe webhook: format valide"}
)

$allValid = $true
foreach ($validation in $validations) {
    if ($validation.Test) {
        Write-Host "✅ $($validation.Message)" -ForegroundColor Green
    } else {
        Write-Host "❌ $($validation.Message)" -ForegroundColor Red
        $allValid = $false
    }
}

if ($allValid) {
    Write-Host ""
    Write-Host "🎉 [SUCCESS] Tous les secrets ont été générés avec succès !" -ForegroundColor Green
    Write-Host "🔒 [SECURITY] N'oubliez pas:" -ForegroundColor Cyan
    Write-Host "   - Ne jamais committer $OutputFile en git" -ForegroundColor Yellow
    Write-Host "   - Stocker ce fichier de manière sécurisée (coffre-fort, secret manager)" -ForegroundColor Yellow
    Write-Host "   - Faire tourner régulièrement ces secrets" -ForegroundColor Yellow
    Write-Host "   - Utiliser des variables d'environnement en production" -ForegroundColor Yellow
} else {
    Write-Error "❌ Certains secrets n'ont pas passé la validation"
    exit 1
}