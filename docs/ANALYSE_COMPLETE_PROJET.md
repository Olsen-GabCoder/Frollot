# Analyse Complète du Projet Frollot

## 📋 Vue d'ensemble

**Frollot** est une application complète de gestion de salons de coiffure avec fonctionnalités sociales, développée en **Kotlin Multiplatform** (frontend) et **Spring Boot** (backend).

---

## 🏗️ Architecture Générale

### Backend (Spring Boot)
- **Framework** : Spring Boot 3.4.1
- **Langage** : Kotlin 2.1.0
- **Base de données** : MySQL 8.0+ avec Flyway pour les migrations
- **Sécurité** : Spring Security + JWT
- **API** : REST avec Swagger/OpenAPI
- **Email** : Spring Mail avec Thymeleaf pour les templates

### Frontend (Kotlin Multiplatform)
- **Framework** : Jetpack Compose Multiplatform
- **Plateformes** : Android, iOS (via Kotlin Multiplatform), Web
- **Architecture** : MVVM avec State Management

---

## 📊 Structure de la Base de Données

### Tables Principales

#### 1. **users** (Table centrale)
- `id` (CHAR(36), UUID)
- `email` (VARCHAR(255), UNIQUE, NOT NULL)
- `password_hash` (VARCHAR(255), BCrypt)
- `user_type` (ENUM: client, hairstylist, salon_owner, admin)
- `first_name`, `last_name`
- `phone_number` (UNIQUE)
- `avatar_url`, `cover_image_url`
- `is_verified` (vérification manuelle salon/coiffeur)
- `email_verified` (vérification email obligatoire) ⭐ NOUVEAU
- `email_verification_token` (UUID) ⭐ NOUVEAU
- `email_verification_token_expires_at` ⭐ NOUVEAU
- `is_active`
- `created_at`, `updated_at`, `last_login_at`

#### 2. **salons**
- Informations des salons (nom, adresse, coordonnées GPS, horaires)
- Lié à `users` via `owner_id`
- Champs sociaux : `social_description`, `social_cover_image`

#### 3. **salon_staff**
- Relation entre salons et coiffeurs
- Rôles : owner, manager, hairstylist, apprentice

#### 4. **bookings**
- Réservations de rendez-vous
- Statuts : pending, confirmed, in_progress, completed, cancelled, no_show
- Lié à `salons`, `users` (client), `salon_staff`, `services`

#### 5. **posts**
- Posts sociaux (réseau social)
- Types : before_after, portfolio, inspiration, tutorial
- Visibilité : public, followers, private
- Compteurs : likes_count, comments_count, shares_count

#### 6. **comments**
- Commentaires sur les posts
- Lié à `posts` et `users`

#### 7. **post_likes**, **post_reactions**, **post_shares**
- Interactions sociales avec les posts

#### 8. **follows**
- Système de suivi entre utilisateurs et salons

#### 9. **portfolios**
- Portfolios de coiffeurs
- Lié à `users` (hairstylist)

#### 10. **collections**
- Collections de posts sauvegardés par les utilisateurs

#### 11. **badges** et **user_badges**
- Système de badges et certifications

#### 12. **refresh_tokens**
- Gestion des sessions utilisateurs
- Informations de device (user_agent, ip_address, device_name)

#### 13. **payments**
- Paiements Stripe
- Lié à `bookings`

#### 14. **reviews**
- Avis clients sur les salons et coiffeurs

#### 15. **waiting_queue** et **queue_entries**
- Files d'attente pour les salons

---

## 🔐 Système d'Authentification

### Flux d'Inscription (AVANT la vérification email)
1. Utilisateur envoie `RegisterRequest` avec email, password, firstName, lastName, userType
2. Validation du format email (@Email annotation)
3. Vérification unicité email
4. Hashage du mot de passe (BCrypt)
5. Création de l'utilisateur avec `emailVerified = false`
6. Génération d'un token JWT et refresh token
7. Retour de `AuthResponse` avec tokens

### Flux d'Inscription (APRÈS la vérification email) ⭐ NOUVEAU
1. Utilisateur envoie `RegisterRequest`
2. Validation du format email
3. **Vérification de l'existence réelle de l'email** (MX records) ⭐
4. Vérification unicité email
5. Hashage du mot de passe
6. Création de l'utilisateur avec `emailVerified = false`
7. **Génération d'un token de vérification** ⭐
8. **Envoi d'un email de vérification** ⭐
9. Génération d'un token JWT (mais compte limité jusqu'à vérification)
10. Retour de `AuthResponse` avec indication que l'email doit être vérifié

### Flux de Vérification Email ⭐ NOUVEAU
1. Utilisateur clique sur le lien dans l'email ou utilise le token
2. Appel à `POST /api/users/verify-email` avec le token
3. Vérification du token (non expiré, valide)
4. Mise à jour de `emailVerified = true`
5. Suppression du token de vérification
6. Compte activé

### Flux de Connexion
1. Utilisateur envoie `LoginRequest` avec email et password
2. Recherche de l'utilisateur par email
3. Vérification du mot de passe (BCrypt)
4. Vérification que le compte est actif (`isActive = true`)
5. **Vérification que l'email est vérifié** ⭐ NOUVEAU (peut être optionnel selon les besoins)
6. Génération de nouveaux tokens JWT et refresh token
7. Retour de `AuthResponse`

---

## 🔧 Services Principaux

### UserService
- `registerUser()` : Inscription avec vérification email ⭐ MODIFIÉ
- `checkPassword()` : Vérification mot de passe
- `changePassword()` : Changement de mot de passe
- `changeEmail()` : Changement d'email
- `updateUserProfile()` : Mise à jour du profil
- `deleteAccount()` : Suppression de compte

### EmailVerificationService ⭐ NOUVEAU
- `verifyEmailExists()` : Vérifie l'existence réelle d'un email (MX records)
- `generateVerificationToken()` : Génère un token UUID
- `sendVerificationEmail()` : Envoie l'email de vérification
- `verifyToken()` : Vérifie et valide un token
- `resendVerificationEmail()` : Renvoie un email de vérification

### EmailService
- `sendBookingConfirmation()` : Confirmation de réservation
- `sendBookingReminder()` : Rappel 24h avant
- `sendBookingStatusChange()` : Changement de statut
- `sendQueueNotification()` : Notification file d'attente

### SocialService
- Gestion des posts, commentaires, likes, réactions
- Système de suivi (follows)
- Collections et portfolios

### BookingService
- Création et gestion des réservations
- Gestion des statuts
- Intégration avec les paiements Stripe

### QueueService
- Gestion des files d'attente
- Calcul des temps d'attente estimés

---

## 🛡️ Sécurité

### Authentification
- **JWT** : Tokens d'accès avec expiration (2 heures par défaut)
- **Refresh Tokens** : Tokens de rafraîchissement avec rotation
- **BCrypt** : Hashage des mots de passe (force 12)

### Protection contre les attaques
- **Rate Limiting** : Protection brute force sur `/login`
- **CORS** : Configuration pour les origines autorisées
- **Validation** : Jakarta Validation sur tous les DTOs

### Vérification Email ⭐ NOUVEAU
- **Validation MX** : Vérifie que le domaine peut recevoir des emails
- **Tokens sécurisés** : UUID avec expiration 24h
- **Emails HTML** : Templates Thymeleaf professionnels

---

## 📧 Configuration Email

### Variables d'environnement
```properties
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=votre-email@gmail.com
SMTP_PASSWORD=votre-mot-de-passe-app
EMAIL_FROM=no-reply@frollot.com
EMAIL_ENABLED=true
EMAIL_VERIFICATION_ENABLED=true
```

### Templates Email
- `email/email-verification_fr.html` : Email de vérification ⭐ NOUVEAU
- `email/booking-confirmation_*.html` : Confirmations de réservation (multi-langues)
- `email/booking-reminder_*.html` : Rappels (multi-langues)
- `email/booking-status-change_*.html` : Changements de statut (multi-langues)
- `email/queue-notification_*.html` : Notifications file d'attente (multi-langues)

---

## 🗄️ Migrations Flyway

Le projet utilise **Flyway** pour gérer les migrations de base de données :

- **V001** : Schéma initial (users, salons, bookings, posts, etc.)
- **V002** : Refresh tokens
- **V003** : Payments (Stripe)
- **V004** : Device tokens (notifications push)
- **V005-V035** : Évolutions progressives
- **V036** : Champs de vérification email ⭐ NOUVEAU

---

## 🧹 Nettoyage de la Base de Données

Un script SQL complet a été créé pour nettoyer entièrement la base de données :

**Fichier** : `backend/scripts/clean_database.sql`

**Utilisation** :
```bash
mysql -u coiffure_user -p coiffure_db < backend/scripts/clean_database.sql
```

**⚠️ ATTENTION** : Ce script supprime TOUTES les données de toutes les tables. Opération irréversible.

---

## 🚀 Prochaines Étapes

1. ✅ **Nettoyer la base de données** (script créé)
2. ✅ **Migration V036** (champs email_verified ajoutés)
3. ✅ **Service EmailVerificationService** créé
4. ✅ **Intégration dans UserService** (vérification MX lors de l'inscription)
5. ✅ **Endpoint de vérification** (`/api/users/verify-email`)
6. ✅ **Template email de vérification** créé
7. ⏳ **Modifier le frontend** pour gérer le flux de vérification
8. ⏳ **Tester le flux complet** d'inscription → vérification → connexion

---

## 📝 Notes Importantes

### Vérification Email
- La vérification MX est activée par défaut (`EMAIL_VERIFICATION_ENABLED=true`)
- Peut être désactivée en développement si nécessaire
- Les emails sont désactivés par défaut (`EMAIL_ENABLED=false`)
- En mode développement sans email, le token est loggé dans la console

### Sécurité
- Les utilisateurs avec `emailVerified = false` peuvent toujours se connecter (à adapter selon les besoins)
- Pour bloquer la connexion sans email vérifié, modifier `UserController.login()`

### Performance
- La vérification MX peut prendre quelques secondes
- Considérer un cache ou une vérification asynchrone pour la production

---

## 🔍 Points d'Attention

1. **Dépendances circulaires** : Vérifier que `UserService` et `EmailVerificationService` n'ont pas de dépendances circulaires
2. **Gestion des erreurs** : Les erreurs de vérification MX ne doivent pas bloquer complètement l'inscription en production
3. **Rate limiting** : Ajouter un rate limiting sur `/verify-email` et `/resend-verification`
4. **Logs** : Les tokens de vérification ne doivent jamais être loggés en production
5. **Tests** : Créer des tests unitaires et d'intégration pour le flux de vérification

---

## 📚 Documentation Complémentaire

- `docs/ANALYSE_AUTHENTIFICATION.md` : Analyse détaillée de l'authentification
- `docs/SECURITY.md` : Documentation sécurité
- `backend/SECURITY_CONFIG.md` : Configuration sécurité
- `CHARTE_GRAPHIQUE_FROLLOT.md` : Charte graphique

---

**Date d'analyse** : 28 décembre 2025
**Version** : 1.0

