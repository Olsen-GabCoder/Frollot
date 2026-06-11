# Configuration Environnement - Frollot

## Fichier .env

Créez un fichier `.env` à la racine du projet avec les variables suivantes :

```bash
# =========================
# BASE DE DONNÉES MYSQL
# =========================
MYSQL_ROOT_PASSWORD=your_secure_root_password
MYSQL_DATABASE=coiffure_db
MYSQL_USER=coiffure_user
MYSQL_PASSWORD=your_secure_password
MYSQL_PORT=3306

# =========================
# BACKEND
# =========================
BACKEND_PORT=9090
SPRING_PROFILES_ACTIVE=prod

# =========================
# SÉCURITÉ JWT (OBLIGATOIRE)
# =========================
# Générer avec: openssl rand -base64 32
JWT_SECRET=your_jwt_secret_key_minimum_32_characters_required
JWT_EXPIRATION_HOURS=24
JWT_EXPIRATION_MINUTES=15

# =========================
# FLYWAY (Migrations DB)
# =========================
FLYWAY_ENABLED=true

# =========================
# EMAIL (SMTP) - Optionnel
# =========================
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=
SMTP_PASSWORD=
EMAIL_ENABLED=false
EMAIL_FROM=no-reply@frollot.com

# =========================
# FIREBASE - Optionnel
# =========================
FIREBASE_ENABLED=false
FIREBASE_SERVICE_ACCOUNT_PATH=firebase/service-account-key.json

# =========================
# STRIPE (Paiements) - Optionnel
# =========================
STRIPE_SECRET_KEY=
STRIPE_PUBLISHABLE_KEY=
STRIPE_WEBHOOK_SECRET=
```

## Génération du JWT_SECRET

```bash
# Linux/Mac
openssl rand -base64 32

# PowerShell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Minimum 0 -Maximum 256 }) -as [byte[]])
```

## Démarrage avec Docker

```bash
# Créer le fichier .env
cp docs/ENV_EXAMPLE.md .env
# Éditer et remplir les valeurs

# Démarrer
docker-compose up -d

# Voir les logs
docker-compose logs -f backend
```

## ATTENTION

- Ne jamais commiter le fichier `.env` dans Git
- Utiliser des mots de passe forts en production
- Le JWT_SECRET doit faire au minimum 32 caractères

