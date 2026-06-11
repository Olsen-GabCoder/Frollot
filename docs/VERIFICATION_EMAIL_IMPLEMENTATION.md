# Implémentation de la Vérification d'Email

## 📋 Résumé

Une vérification systématique des adresses email a été implémentée pour garantir que seules les adresses email réelles et valides peuvent être utilisées lors de l'inscription.

---

## ✅ Ce qui a été implémenté

### 1. Migration de Base de Données (V036)
- Ajout de `email_verified` (BOOLEAN, DEFAULT FALSE)
- Ajout de `email_verification_token` (VARCHAR(100))
- Ajout de `email_verification_token_expires_at` (TIMESTAMP)
- Ajout de `email_verification_sent_at` (TIMESTAMP)
- Index pour optimiser les requêtes

### 2. Service EmailVerificationService
- **`verifyEmailExists()`** : Vérifie l'existence réelle d'un email via les enregistrements MX DNS
- **`generateVerificationToken()`** : Génère un token UUID unique
- **`sendVerificationEmail()`** : Envoie un email HTML avec lien de vérification
- **`verifyToken()`** : Vérifie et valide un token de vérification
- **`resendVerificationEmail()`** : Renvoie un email de vérification

### 3. Modifications UserService
- Intégration de la vérification MX lors de l'inscription
- Envoi automatique d'un email de vérification après inscription
- Gestion des erreurs d'envoi d'email (ne bloque pas l'inscription)

### 4. Endpoints API
- **`POST /api/users/verify-email`** : Vérifie un token de vérification
- **`POST /api/users/me/resend-verification`** : Renvoie un email de vérification

### 5. Modifications UserController
- Blocage de la connexion si `emailVerified = false`
- Gestion des erreurs de vérification

### 6. Template Email
- Template HTML professionnel (`email-verification_fr.html`)
- Design responsive et moderne
- Lien de vérification et code token affichés

### 7. Scripts de Nettoyage
- Script SQL complet (`clean_database.sql`)
- Scripts shell/bash et batch pour Windows
- Vérification des tables après nettoyage

---

## 🔧 Configuration

### Variables d'environnement

Ajoutez ces variables dans votre fichier `.env` ou `application.yml` :

```properties
# Activation de l'envoi d'emails
EMAIL_ENABLED=true

# Activation de la vérification email (vérification MX)
EMAIL_VERIFICATION_ENABLED=true

# Configuration SMTP
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=votre-email@gmail.com
SMTP_PASSWORD=votre-mot-de-passe-app
EMAIL_FROM=no-reply@frollot.com

# Mode de développement pour les emails
# Options: send|redirect|log
EMAIL_DEV_MODE=send

# Adresse de redirection pour le mode 'redirect'
EMAIL_DEV_REDIRECT=test@frollot.com

# Fallback SMTP (si false, passe en mode log si SMTP non configuré)
EMAIL_SMTP_FALLBACK=true
```

### En Mode Développement

Le système supporte plusieurs modes de développement pour l'envoi d'emails :

- **`EMAIL_DEV_MODE=send`** (Recommandé) : Envoi réel d'emails aux utilisateurs même en développement
- **`EMAIL_DEV_MODE=redirect`** : Redirection vers une adresse de test fixe (EMAIL_DEV_REDIRECT)
- **`EMAIL_DEV_MODE=log`** : Logging seulement (pas d'envoi)

Si `EMAIL_ENABLED=false`, les emails ne sont pas envoyés mais le token est loggé dans la console pour faciliter les tests.

---

## 🔧 Configuration Détaillée

### Variables d'Environnement Email

| Variable | Valeur par défaut | Description |
|----------|------------------|-------------|
| `EMAIL_ENABLED` | `true` | Active/désactive complètement l'envoi d'emails |
| `EMAIL_VERIFICATION_ENABLED` | `true` | Active la vérification MX des domaines email |
| `EMAIL_FROM` | `no-reply@frollot.com` | Adresse expéditeur des emails |
| `EMAIL_DEV_MODE` | `send` | Mode développement : `send`, `redirect`, `log` |
| `EMAIL_DEV_REDIRECT` | `test@frollot.com` | Adresse de redirection en mode `redirect` |
| `EMAIL_SMTP_FALLBACK` | `true` | Fallback vers logging si SMTP non configuré |

### Configuration SMTP

| Variable | Valeur par défaut | Description |
|----------|------------------|-------------|
| `SMTP_HOST` | `smtp.gmail.com` | Serveur SMTP |
| `SMTP_PORT` | `587` | Port SMTP (587 pour TLS, 465 pour SSL) |
| `SMTP_USERNAME` | Vide | Nom d'utilisateur SMTP |
| `SMTP_PASSWORD` | Vide | Mot de passe SMTP |

---

## 📧 Modes de Développement Détaillés

### Mode `send` (Recommandé pour Tests Réels)
```properties
EMAIL_DEV_MODE=send
```
- ✅ Envoi réel d'emails aux utilisateurs
- ✅ Teste le flux complet en conditions réelles
- ✅ Nécessite une configuration SMTP valide
- ⚠️ Risque d'envoyer des emails de test aux vraies adresses

### Mode `redirect` (Pour Tests Contrôlés)
```properties
EMAIL_DEV_MODE=redirect
EMAIL_DEV_REDIRECT=test@frollot.com
```
- ✅ Tous les emails redirigés vers une adresse de test
- ✅ Pas de risque d'envoi accidentel
- ❌ Ne teste pas l'envoi à l'adresse réelle de l'utilisateur

### Mode `log` (Pour Développement Rapide)
```properties
EMAIL_DEV_MODE=log
```
- ✅ Logging des tokens dans la console
- ✅ Aucun envoi d'email
- ❌ Ne teste pas l'envoi réel

---

## 🚀 Démarrage Rapide

### 1. Configuration pour Développement
```bash
# Créer un fichier .env dans backend/
cp backend/env-development-example.txt backend/.env

# Éditer la configuration SMTP
# Pour Gmail : créer un mot de passe d'application
# https://myaccount.google.com/apppasswords
```

### 2. Configuration SMTP Gmail
```properties
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=votre-email@gmail.com
SMTP_PASSWORD=votre-mot-de-passe-d-application
EMAIL_FROM=no-reply@frollot.com
```

### 3. Démarrage
```bash
cd backend
./gradlew bootRun
```

---

## 🔍 Diagnostic

### Endpoint de Diagnostic
```
GET /api/diagnostic/email-config
```
Retourne la configuration email actuelle et le mode effectif.

### Logs au Démarrage
Le système log automatiquement la configuration détectée :
```
📧 [EmailConfig] Mode DEV_SEND - Envoi réel en développement
📧 [EmailConfig] SMTP configuré: true
```

---

## 🚀 Utilisation

### 1. Nettoyer la Base de Données

**Windows** :
```cmd
cd backend\scripts
clean_database.bat
```

**Linux/Mac** :
```bash
cd backend/scripts
chmod +x clean_database.sh
./clean_database.sh
```

**Manuel** :
```bash
mysql -u coiffure_user -p coiffure_db < backend/scripts/clean_database.sql
```

### 2. Appliquer la Migration

La migration V036 sera appliquée automatiquement au démarrage de l'application si Flyway est activé.

### 3. Flux d'Inscription

1. **Client** envoie `POST /api/users/register` avec email, password, firstName, lastName
2. **Backend** vérifie :
   - Format de l'email (regex)
   - Existence réelle de l'email (MX records)
   - Unicité de l'email
   - Validité du mot de passe
3. **Backend** crée l'utilisateur avec `emailVerified = false`
4. **Backend** génère un token de vérification (UUID)
5. **Backend** envoie un email avec le lien de vérification
6. **Backend** retourne `AuthResponse` avec tokens JWT (mais compte limité)

### 4. Vérification de l'Email

1. **Utilisateur** clique sur le lien dans l'email ou utilise le token
2. **Client** envoie `POST /api/users/verify-email` avec `{"token": "uuid-du-token"}`
3. **Backend** vérifie le token (non expiré, valide)
4. **Backend** met à jour `emailVerified = true`
5. **Backend** supprime le token de vérification
6. **Backend** retourne succès

### 5. Connexion

1. **Client** envoie `POST /api/users/login` avec email et password
2. **Backend** vérifie :
   - Existence de l'utilisateur
   - Mot de passe correct
   - Compte actif (`isActive = true`)
   - **Email vérifié (`emailVerified = true`)** ⭐ NOUVEAU
3. Si email non vérifié → **Erreur 403 Forbidden**
4. Si tout OK → Retourne tokens JWT

---

## 🔒 Sécurité

### Vérification MX
- Vérifie que le domaine possède des enregistrements MX (peut recevoir des emails)
- Empêche l'inscription avec des emails de domaines inexistants
- Peut être désactivée en développement (`EMAIL_VERIFICATION_ENABLED=false`)

### Tokens de Vérification
- UUID aléatoires (non prédictibles)
- Expiration après 24 heures
- Supprimés après vérification réussie
- Un seul token actif par utilisateur

### Blocage de Connexion
- Les utilisateurs avec `emailVerified = false` ne peuvent pas se connecter
- Message d'erreur clair avec indication de vérifier l'email

---

## 📝 Notes Importantes

### En Production
1. **Activer les emails** : `EMAIL_ENABLED=true`
2. **Configurer SMTP** : Utiliser un service SMTP fiable (Gmail, SendGrid, AWS SES, etc.)
3. **Rate Limiting** : Ajouter un rate limiting sur `/verify-email` et `/resend-verification`
4. **Monitoring** : Surveiller les taux d'échec de vérification MX

### En Développement
1. **Emails désactivés** : Les tokens sont loggés dans la console
2. **Vérification MX** : Peut être désactivée si trop restrictive (`EMAIL_VERIFICATION_ENABLED=false`)
3. **Tests** : Utiliser des emails de test valides (ex: test@example.com ne fonctionnera pas)

### Gestion des Erreurs
- Si l'envoi d'email échoue, l'inscription n'est pas bloquée
- L'utilisateur peut demander un renvoi via `/me/resend-verification`
- Les erreurs MX sont loggées mais n'empêchent pas l'inscription si `EMAIL_VERIFICATION_ENABLED=false`

### Changements Récents (Décembre 2025)
- ✅ **Correction majeure** : Les emails sont maintenant envoyés à l'adresse réelle de l'utilisateur par défaut (`EMAIL_DEV_MODE=send`)
- ✅ **Nouveau mode** : `DEV_SEND` pour envoi réel même en développement
- ✅ **Configuration améliorée** : Fichier d'exemple `env-development-example.txt` créé
- ✅ **Sécurité renforcée** : Rollback automatique si échec d'envoi d'email

---

## 🧪 Tests

### Tester la Vérification MX
```kotlin
// Test avec un email valide
emailVerificationService.verifyEmailExists("test@gmail.com") // true

// Test avec un domaine inexistant
emailVerificationService.verifyEmailExists("test@domain-inexistant-12345.com") // false
```

### Tester le Flux Complet
1. Inscription avec email valide → Email reçu
2. Vérification avec token → `emailVerified = true`
3. Connexion → Succès
4. Connexion sans vérification → Erreur 403

---

## 📚 Fichiers Modifiés/Créés

### Nouveaux Fichiers
- `backend/src/main/kotlin/com/frollot/service/EmailVerificationService.kt`
- `backend/src/main/resources/db/migration/V036__add_email_verification_fields.sql`
- `backend/src/main/resources/templates/email/email-verification_fr.html`
- `backend/scripts/clean_database.sql`
- `backend/scripts/clean_database.sh`
- `backend/scripts/clean_database.bat`
- `docs/ANALYSE_COMPLETE_PROJET.md`
- `docs/VERIFICATION_EMAIL_IMPLEMENTATION.md`

### Fichiers Modifiés
- `backend/src/main/kotlin/com/frollot/model/User.kt` (ajout champs email_verified)
- `backend/src/main/kotlin/com/frollot/repository/UserRepository.kt` (ajout findByEmailVerificationToken)
- `backend/src/main/kotlin/com/frollot/service/UserService.kt` (intégration vérification)
- `backend/src/main/kotlin/com/frollot/controller/UserController.kt` (endpoints + blocage connexion)
- `backend/src/main/resources/application.yml` (configuration EMAIL_VERIFICATION_ENABLED)

---

## ⚠️ Points d'Attention

1. **Performance** : La vérification MX peut prendre 1-3 secondes. Considérer un cache ou une vérification asynchrone.
2. **Faux Négatifs** : Certains serveurs SMTP bloquent les vérifications. La vérification SMTP est désactivée par défaut.
3. **Rate Limiting** : Ajouter un rate limiting sur les endpoints de vérification pour éviter les abus.
4. **Logs** : Ne jamais logger les tokens de vérification en production.
5. **Frontend** : Le frontend doit gérer le flux de vérification (écran de vérification, renvoi d'email).

---

## 🎯 Prochaines Étapes

1. ✅ Nettoyer la base de données (script créé)
2. ✅ Migration V036 appliquée
3. ✅ Service de vérification créé
4. ✅ Intégration dans UserService
5. ✅ Endpoints API créés
6. ⏳ Modifier le frontend pour gérer le flux de vérification
7. ⏳ Ajouter des tests unitaires et d'intégration
8. ⏳ Ajouter un rate limiting sur les endpoints de vérification
9. ⏳ Créer des templates email pour les autres langues (en, es, de, ar)

---

**Date d'implémentation** : 28 décembre 2025
**Version** : 1.0

