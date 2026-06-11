# Analyse Complète de la Gestion des Tokens JWT

## 📋 Diagnostic

### Problème Identifié
Les requêtes échouent car l'access token utilisé par le client est expiré, et il n'existe pas actuellement de cycle de vie complet et fiable de gestion des tokens. Le backend détecte correctement l'expiration du JWT et applique la sécurité comme prévu. Il ne s'agit donc pas d'un bug, mais d'un manque fonctionnel dans la gestion des sessions.

---

## 🔍 Analyse Backend

### 1. Génération des Tokens

**Fichier** : `backend/src/main/kotlin/com/frollot/security/JwtTokenProvider.kt`

- **Access Token** : Durée de vie = **2 heures** (configurable via `JWT_EXPIRATION_MINUTES`)
- **Refresh Token** : Durée de vie = **30 jours** (hardcodé dans `generateRefreshToken()`)
- **Structure** : Access token contient userId, email, userType, firstName, lastName, isActive, isVerified

**Configuration actuelle** :
```kotlin
@Value("\${app.security.jwt.expiration-minutes:120}")
private val jwtExpirationMinutes: Long
```

### 2. Validation des Tokens

**Fichier** : `backend/src/main/kotlin/com/frollot/security/JwtAuthenticationFilter.kt`

- Le filtre intercepte toutes les requêtes
- Extrait le token du header `Authorization: Bearer <token>`
- Valide le token avec `jwtTokenProvider.validateToken()`
- Si invalide (expiré, malformé, etc.) → **Retourne 401 Unauthorized**
- Si valide → Crée l'utilisateur depuis les claims et place dans SecurityContext

**Comportement actuel** :
- ✅ Détecte correctement les tokens expirés
- ✅ Retourne 401 Unauthorized avec message "Token JWT invalide ou expiré"
- ✅ Bloque les requêtes avec tokens invalides

### 3. Refresh Token Service

**Fichier** : `backend/src/main/kotlin/com/frollot/service/RefreshTokenService.kt`

- **Stockage** : Refresh tokens stockés en base de données (table `refresh_tokens`)
- **Durée de vie** : 7 jours (constante `REFRESH_TOKEN_VALIDITY_DAYS`)
- **Rotation** : Activée (nouveau refresh token à chaque refresh)
- **Révocation** : Supportée (logout, logout all devices)

**Méthodes clés** :
- `validateRefreshToken()` : Valide un refresh token (non révoqué, non expiré)
- `rotateRefreshToken()` : Révoque l'ancien token et crée un nouveau
- `revokeRefreshToken()` : Révoque un token (logout)

### 4. Endpoint de Refresh

**Fichier** : `backend/src/main/kotlin/com/frollot/controller/UserController.kt`

**Endpoint** : `POST /api/users/refresh`

**Comportement** :
1. Valide le refresh token
2. Si invalide → Retourne 401 avec message "Refresh token invalide ou expiré"
3. Si valide → Rotation du token (nouveau access + nouveau refresh)
4. Retourne `AuthResponse` avec les nouveaux tokens

**✅ Le backend fonctionne correctement**

---

## 🔍 Analyse Frontend

### 1. Stockage des Tokens

**Fichier** : `frontend/composeApp/src/commonMain/kotlin/com/frollot/mobile/network/FrollotApi.kt`

- **Mémoire** : `globalAuthToken` et `globalRefreshToken` (variables statiques)
- **Persistance** : `AuthDataStore` (stockage persistant pour restauration au redémarrage)
- **Injection** : Token injecté automatiquement dans le header `Authorization` via `defaultRequest`

### 2. Fonction de Refresh Automatique

**Fichier** : `frontend/composeApp/src/commonMain/kotlin/com/frollot/mobile/network/FrollotApi.kt`

**Fonction existante** : `executeWithAutoRefresh()`

**Comportement** :
- Intercepte les réponses 401
- Déclenche `tryRefreshToken()`
- Relance la requête avec le nouveau token
- Utilise un mutex pour éviter les refresh simultanés

**❌ PROBLÈME CRITIQUE** : Cette fonction n'est utilisée que dans **1 seul endpoint** (`updateUserCoverImage`) sur des centaines d'endpoints !

### 3. Utilisation Actuelle

**Endpoints utilisant `executeWithAutoRefresh`** :
- ✅ `updateUserCoverImage()` (1 seul endpoint)

**Endpoints n'utilisant PAS `executeWithAutoRefresh`** :
- ❌ `getCurrentUser()`
- ❌ `updateUserAvatar()`
- ❌ `getActiveSessions()`
- ❌ `changePassword()`
- ❌ `createSalon()`
- ❌ `createSalonService()`
- ❌ `createBooking()`
- ❌ Et **tous les autres endpoints** (200+ endpoints)

### 4. Gestion des Erreurs

**Problème** : Les endpoints utilisent directement `httpClient.get/post/put/delete` qui :
- N'interceptent pas les erreurs 401
- Ne déclenchent pas le refresh automatique
- Propagent l'erreur directement au code appelant

**Conséquence** : Quand un access token expire :
1. La requête échoue avec 401
2. Le refresh automatique n'est PAS déclenché
3. L'utilisateur doit se reconnecter manuellement

---

## 🎯 Solution Proposée

### Approche : Intercepteur HTTP Global

Au lieu de modifier chaque endpoint individuellement, utiliser un **intercepteur HTTPResponse de Ktor** qui :
1. Intercepte automatiquement toutes les réponses HTTP
2. Détecte les erreurs 401 (Unauthorized)
3. Déclenche le refresh automatique du token
4. Relance la requête originale avec le nouveau token
5. Gère les cas où le refresh token est lui-même expiré

### Avantages

- ✅ **Centralisé** : Une seule implémentation pour tous les endpoints
- ✅ **Transparent** : Aucune modification nécessaire dans les endpoints existants
- ✅ **Robuste** : Gestion d'erreurs complète et sécurisée
- ✅ **Performant** : Mutex pour éviter les refresh simultanés
- ✅ **Maintenable** : Facile à modifier et étendre

### Implémentation

Utiliser le plugin `HttpCallValidator` de Ktor ou créer un intercepteur personnalisé avec `HttpResponsePipeline`.

---

## 📝 Plan d'Implémentation

1. ✅ Analyser le système existant (FAIT)
2. ⏳ Créer un intercepteur HTTPResponse pour gérer automatiquement les 401
3. ⏳ Intégrer l'intercepteur dans le HttpClient principal
4. ⏳ Tester le flux complet (expiration → refresh → retry)
5. ⏳ Gérer les cas limites (refresh token expiré, erreurs réseau)
6. ⏳ Documenter la solution

---

## 🔒 Sécurité

### Points à Vérifier

1. **Mutex pour refresh simultanés** : ✅ Déjà implémenté
2. **HttpClient temporaire pour refresh** : ✅ Déjà implémenté (évite récursion)
3. **Nettoyage des tokens invalides** : ✅ Déjà implémenté
4. **Gestion refresh token expiré** : ⏳ À améliorer (redirection vers login)

---

## 📊 État Actuel

| Composant | État | Action Requise |
|-----------|------|----------------|
| Backend - Génération tokens | ✅ OK | Aucune |
| Backend - Validation tokens | ✅ OK | Aucune |
| Backend - Refresh endpoint | ✅ OK | Aucune |
| Frontend - Stockage tokens | ✅ OK | Aucune |
| Frontend - Fonction refresh | ✅ Existe | ⚠️ Non utilisée |
| Frontend - Intercepteur auto | ❌ Manquant | 🔴 À créer |
| Frontend - Utilisation refresh | ❌ 1/200+ | 🔴 À corriger |

---

**Date d'analyse** : 28 décembre 2025
**Version** : 1.0

