# Configuration des Variables d'Environnement

Ce projet utilise des variables d'environnement pour la configuration. Les clés sensibles (Stripe, JWT, etc.) sont stockées dans le fichier `.env`.

## 📋 Fichiers de Configuration

- **`.env`** : Fichier contenant vos vraies clés (NE PAS COMMITER - déjà dans .gitignore)
- **`env-config.txt`** : Template avec toutes les variables (peut être commité)
- **`load-env.ps1`** : Script PowerShell pour charger les variables
- **`start-with-env.bat`** : Script batch pour charger et lancer l'application

## 🚀 Utilisation

### Option 1 : Script Batch (Windows - Recommandé)

```bash
.\start-with-env.bat
```

### Option 2 : Script PowerShell

```powershell
.\load-env.ps1
.\gradlew bootRun
```

### Option 3 : Variables d'environnement système (Windows)

```powershell
# Charger depuis .env
Get-Content .env | ForEach-Object {
    if ($_ -match '^([^#][^=]+)=(.*)$') {
        [Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), "Process")
    }
}

# Puis lancer
.\gradlew bootRun
```

### Option 4 : Export manuel (Linux/Mac)

```bash
export $(cat .env | xargs)
./gradlew bootRun
```

## 🔑 Clés Stripe Configurées

Les clés Stripe de test doivent être configurées dans `.env` (jamais committées) :
- **Secret Key** : `sk_test_...` (depuis le dashboard Stripe, mode test)
- **Publishable Key** : `pk_test_...`

⚠️ **IMPORTANT** : Ces clés sont pour les tests uniquement. Remplacez-les par vos clés de production avant le déploiement.

## 📝 Variables Disponibles

Toutes les variables sont documentées dans `env-config.txt`. Les principales :

- **Base de données** : `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- **JWT** : `JWT_SECRET`, `JWT_EXPIRATION_HOURS`, `JWT_EXPIRATION_MINUTES`
- **Stripe** : `STRIPE_SECRET_KEY`, `STRIPE_PUBLISHABLE_KEY`, `STRIPE_WEBHOOK_SECRET`
- **Email** : `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`
- **Firebase** : `FIREBASE_ENABLED`, `FIREBASE_SERVICE_ACCOUNT_PATH`
- **Logging** : `LOG_LEVEL_ROOT`, `LOG_LEVEL_FROLLOT`, etc.

## 🔒 Sécurité

- Le fichier `.env` est dans `.gitignore` et ne sera **JAMAIS** commité
- Ne partagez jamais vos clés API
- Utilisez des clés de test pour le développement
- Remplacez toutes les clés avant la production

