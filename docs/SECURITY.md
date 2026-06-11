# 🔒 Guide de Sécurité Frollot

## Table des Matières

1. [Vue d'ensemble](#vue-densemble)
2. [Configuration des Secrets](#configuration-des-secrets)
3. [Authentification JWT](#authentification-jwt)
4. [Protection Brute Force](#protection-brute-force)
5. [CORS & Headers de Sécurité](#cors--headers-de-sécurité)
6. [Stripe & Webhooks](#stripe--webhooks)
7. [Checklist Production](#checklist-production)

---

## Vue d'ensemble

Frollot utilise une architecture de sécurité multicouche :

```
┌─────────────────────────────────────────────────────────────┐
│                      CLIENT (Mobile/Web)                     │
└─────────────────────────────┬───────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    RATE LIMIT FILTER                         │
│  - 5 req/min pour /login                                    │
│  - 3 req/min pour /register                                 │
│  - 100 req/min par défaut                                   │
│  - Blocage progressif après échecs                          │
└─────────────────────────────┬───────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                 JWT AUTHENTICATION FILTER                    │
│  - Validation du token JWT                                  │
│  - Extraction des claims (userId, email, userType)          │
│  - Vérification isActive                                    │
└─────────────────────────────┬───────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    SECURITY CONFIG                           │
│  - Endpoints publics/protégés                               │
│  - CORS whitelist                                           │
│  - Headers de sécurité (CSP, HSTS, X-Frame-Options)         │
└─────────────────────────────┬───────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     CONTROLLERS                              │
│  - @PreAuthorize pour les rôles                             │
│  - Validation des données                                   │
│  - Vérification propriétaire/admin                          │
└─────────────────────────────────────────────────────────────┘
```

---

## Configuration des Secrets

### JWT_SECRET (CRITIQUE)

Le secret JWT **doit** faire minimum **32 caractères** (256 bits pour HMAC-SHA256).

#### Génération sécurisée

**PowerShell (Windows) :**
```powershell
$b = New-Object byte[] 48
[Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b)
[Convert]::ToBase64String($b)
```

**Linux/macOS :**
```bash
openssl rand -base64 48
```

#### Validation automatique

Au démarrage, `JwtTokenProvider` valide le secret :
- ❌ **Production** : L'app refuse de démarrer si le secret est invalide
- ⚠️ **Développement** : Avertissement mais l'app démarre

### Passwords de Base de Données

- **Développement** : Passwords locaux acceptés
- **Production** : Passwords forts obligatoires (min 16 caractères, mix de caractères)

### Stripe

- Utiliser des clés `sk_test_*` en développement
- **JAMAIS** de clés `sk_live_*` dans le code ou `.env` commité
- Configurer le webhook secret pour valider les signatures

---

## Authentification JWT

### Flux d'authentification

```
1. POST /api/users/login
   └─> Validation credentials
   └─> Génération Access Token (15 min)
   └─> Génération Refresh Token (30 jours, stocké en BDD)

2. Requêtes authentifiées
   └─> Header: Authorization: Bearer <access_token>
   └─> JwtAuthenticationFilter valide le token
   └─> Claims extraits et injectés dans SecurityContext

3. Refresh du token
   └─> POST /api/auth/refresh avec refresh_token
   └─> Rotation du refresh token (ancien invalidé)
   └─> Nouveau access + refresh token retournés
```

### Structure du Token JWT

```json
{
  "sub": "user-uuid",
  "userId": "user-uuid",
  "email": "user@example.com",
  "userType": "client",
  "firstName": "John",
  "lastName": "Doe",
  "isActive": true,
  "isVerified": false,
  "iat": 1703721600,
  "exp": 1703722500
}
```

---

## Protection Brute Force

### Rate Limiting

| Endpoint | Limite | Période |
|----------|--------|---------|
| `/api/users/login` | 5 requêtes | 1 minute |
| `/api/users/register` | 3 requêtes | 1 minute |
| `/api/auth/refresh` | 10 requêtes | 1 minute |
| `/api/payments/webhook` | 50 requêtes | 1 minute |
| Autres | 100 requêtes | 1 minute |

### Blocage Progressif (Login)

Après des échecs répétés de login depuis la même IP :

| Échecs | Durée de blocage |
|--------|------------------|
| 3 | 1 minute |
| 5 | 5 minutes |
| 7 | 15 minutes |
| 10+ | 1 heure |

Le compteur est réinitialisé après un login réussi.

---

## CORS & Headers de Sécurité

### CORS (Cross-Origin Resource Sharing)

**Développement :**
```
http://localhost:3000
http://localhost:8080
http://127.0.0.1:3000
http://10.0.2.2:8080 (émulateur Android)
```

**Production :**
```
https://app.frollot.com
https://staging.frollot.com
```

### Headers de Sécurité

```
Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline'; ...
Strict-Transport-Security: max-age=31536000; includeSubDomains
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
Referrer-Policy: strict-origin-when-cross-origin
```

---

## Stripe & Webhooks

### Validation des Webhooks

Les webhooks Stripe sont validés par signature :

```kotlin
// Dans PaymentController
val signature = request.getHeader("Stripe-Signature")
val event = Webhook.constructEvent(payload, signature, webhookSecret)
```

**Sans signature valide, le webhook est rejeté (401).**

### Endpoints concernés

- `POST /api/payments/webhook` - Public mais protégé par signature

---

## Checklist Production

### Avant le déploiement

- [ ] **JWT_SECRET** : Généré avec `openssl rand -base64 48`
- [ ] **DB_PASSWORD** : Password fort (16+ caractères)
- [ ] **Stripe** : Clés `sk_live_*` en variable d'environnement
- [ ] **SPRING_PROFILES_ACTIVE** : `prod`
- [ ] **JPA_DDL_AUTO** : `validate`
- [ ] **JPA_SHOW_SQL** : `false`
- [ ] **LOG_LEVEL_***: `INFO` ou `WARN`
- [ ] **Email** : SMTP configuré si `EMAIL_ENABLED=true`
- [ ] **Firebase** : Service account configuré si `FIREBASE_ENABLED=true`

### Variables d'environnement (jamais dans le code)

```bash
export JWT_SECRET="votre-secret-genere"
export DB_PASSWORD="votre-password-db"
export STRIPE_SECRET_KEY="sk_live_..."
export STRIPE_WEBHOOK_SECRET="whsec_..."
export SMTP_PASSWORD="votre-app-password"
```

### Vérification post-déploiement

1. Vérifier les logs de démarrage pour "TOUTES LES CONFIGURATIONS CRITIQUES SONT VALIDÉES"
2. Tester le login avec credentials invalides → Vérifier rate limiting
3. Tester le refresh token
4. Vérifier les headers de sécurité avec `curl -I https://api.frollot.com/api/users/login`

---

## Contact Sécurité

Pour signaler une vulnérabilité : security@frollot.com

