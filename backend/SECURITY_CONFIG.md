# Configuration Sécurité - Frollot Backend

## 🔒 Phase 1.2 - Sécurité Opérationnelle

Ce document décrit les améliorations de sécurité implémentées dans la Phase 1.2.

## ✅ Modifications Appliquées

### 1. Externalisation des Credentials

**Fichiers modifiés :**
- `application.yml` : Utilisation de variables d'environnement
- `.env.example` : Template de configuration
- `application-prod.yml` : Configuration production stricte

**Variables d'environnement requises :**
```bash
DB_URL=jdbc:mysql://localhost:3306/coiffure_db?useSSL=false&serverTimezone=UTC
DB_USERNAME=coiffure_user
DB_PASSWORD=CHANGEME_IN_PRODUCTION
# ============================================================
# CONFIGURATION JWT_SECRET - Phase 2.4
# ============================================================

## Exigences de sécurité

Le `JWT_SECRET` est utilisé pour signer et vérifier les tokens JWT. Il est **CRITIQUE** pour la sécurité de l'application.

### Règles obligatoires

1. **Longueur minimale** : 32 caractères (256 bits pour HMAC-SHA256)
2. **Production** : DOIT être configuré via variable d'environnement, jamais la valeur par défaut
3. **Complexité** : Recommandé de mélanger majuscules, minuscules, chiffres et caractères spéciaux
4. **Aléatoire** : Doit être généré de manière cryptographiquement sécurisée

### Génération d'un secret sécurisé

```bash
# Méthode recommandée (génère 32 caractères aléatoires en base64)
openssl rand -base64 32

# Alternative (génère 32 caractères hexadécimaux)
openssl rand -hex 32

# Exemple de sortie
# base64: K8mN3pQ7rT9vW2xY5zA6bC4dE8fG1hI3jK5lM7nO9pQ1rS3tU5vW7xY9zA
# hex: a1b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef123456
```

### Configuration

#### Développement

Dans `application.yml` ou `.env` :
```yaml
app:
  security:
    jwt:
      secret: votre-secret-securise-minimum-32-caracteres
```

#### Production

**IMPORTANT** : En production, le secret DOIT être configuré via variable d'environnement.

```bash
# Définir la variable d'environnement
export JWT_SECRET=$(openssl rand -base64 32)

# Ou dans votre fichier .env (ne jamais commiter ce fichier)
JWT_SECRET=K8mN3pQ7rT9vW2xY5zA6bC4dE8fG1hI3jK5lM7nO9pQ1rS3tU5vW7xY9zA
```

Dans `application-prod.yml` :
```yaml
app:
  security:
    jwt:
      secret: ${JWT_SECRET}  # Pas de valeur par défaut
```

### Validation automatique

L'application valide automatiquement le secret au démarrage :

- ✅ **Production** : L'application refuse de démarrer si le secret est invalide
- ⚠️ **Développement** : Des avertissements sont affichés mais l'application démarre

### Erreurs courantes

1. **Secret trop court** : Minimum 32 caractères requis
2. **Valeur par défaut en production** : Jamais utiliser "your-jwt-secret-key-change-in-production"
3. **Secret commité dans le code** : Toujours utiliser des variables d'environnement
4. **Secret partagé entre environnements** : Chaque environnement doit avoir son propre secret

### Bonnes pratiques

1. ✅ Générer un secret unique pour chaque environnement (dev, staging, prod)
2. ✅ Stocker le secret dans un gestionnaire de secrets (AWS Secrets Manager, HashiCorp Vault, etc.)
3. ✅ Ne jamais commiter le secret dans le dépôt Git
4. ✅ Rotation régulière du secret (tous les 6-12 mois)
5. ✅ Utiliser des secrets différents pour chaque instance en production (si applicable)

JWT_SECRET=CHANGEME_LONG_RANDOM_SECRET_256_BITS_MINIMUM
```

**Action requise :**
1. Copier `.env.example` en `.env`
2. Remplir les valeurs réelles
3. **NE JAMAIS COMMITER** le fichier `.env`

### 2. Fermeture des Endpoints Dev

**Fichier modifié :** `SecurityConfig.kt`

**Comportement :**
- **En développement** (profil `dev` ou aucun profil) : `/api/dev/**` accessible
- **En production** (profil `prod`) : `/api/dev/**` retourne HTTP 403 Forbidden

**Activation production :**
```bash
export SPRING_PROFILES_ACTIVE=prod
```

### 3. Durcissement CORS

**Fichier modifié :** `SecurityConfig.kt`

**Origines autorisées :**

**Développement :**
- `http://localhost:3000`
- `http://localhost:8080`
- `http://127.0.0.1:3000`
- `http://127.0.0.1:8080`
- `http://10.0.2.2:8080` (émulateur Android)

**Production :**
- `https://app.frollot.com`
- `https://staging.frollot.com`

**Headers autorisés (whitelist stricte) :**
- `Authorization`
- `Content-Type`
- `X-Requested-With`
- `Accept`
- `Origin`
- `Access-Control-Request-Method`
- `Access-Control-Request-Headers`

### 4. Headers de Sécurité HTTP

**Fichier modifié :** `SecurityConfig.kt`

**Headers ajoutés :**
- **Content-Security-Policy** : Protection XSS
- **Strict-Transport-Security (HSTS)** : Force HTTPS (1 an)
- **X-Frame-Options** : DENY (protection clickjacking)
- **X-XSS-Protection** : Activation protection XSS
- **X-Content-Type-Options** : nosniff
- **Referrer-Policy** : strict-origin-when-cross-origin

### 5. Rate Limiting

**Fichier créé :** `RateLimitFilter.kt`
**Dépendance ajoutée :** `bucket4j-core:8.7.0`

**Limites configurées :**
- `/api/users/login` : **5 tentatives / minute**
- `/api/users/register` : **3 tentatives / minute**
- Autres endpoints : **100 requêtes / minute**

**Comportement :**
- Limite dépassée → HTTP 429 Too Many Requests
- Basé sur l'adresse IP du client
- Utilise l'algorithme Token Bucket

### 6. Configuration Profils Spring

**Fichier créé :** `application-prod.yml`

**Différences production :**
- `ddl-auto: validate` (ne modifie jamais le schéma)
- `show-sql: false`
- Logging niveau WARN/INFO
- Swagger désactivé
- Stack traces masquées

### 7. Logging Production

**Fichiers modifiés :**
- `application.yml` : Variables d'environnement pour niveaux
- `application-prod.yml` : Niveaux production

**Niveaux par défaut (production) :**
- `root: WARN`
- `com.frollot: INFO`
- `org.hibernate.SQL: WARN`
- `org.springframework.web: WARN`

## 🚀 Déploiement

### Développement Local

1. Copier `.env.example` en `.env`
2. Remplir les valeurs de développement
3. Lancer l'application (profil `dev` par défaut)

```bash
./gradlew bootRun
```

### Production

1. Configurer les variables d'environnement :
```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_URL=jdbc:mysql://prod-db:3306/frollot
export DB_USERNAME=frollot_user
export DB_PASSWORD=<secret>
export JWT_SECRET=<secret-256-bits>
```

2. Lancer l'application :
```bash
java -jar frollot-backend.jar --spring.profiles.active=prod
```

## ✅ Validation

### Vérifier la sécurité

1. **Endpoints dev bloqués en prod :**
```bash
curl http://prod-url/api/dev/diagnostic/multipart-config
# Doit retourner 403 Forbidden
```

2. **CORS rejette origines non autorisées :**
```bash
curl -H "Origin: https://evil.com" http://prod-url/api/salons
# Doit retourner erreur CORS
```

3. **Rate limiting fonctionne :**
```bash
# 6 requêtes rapides sur /api/users/login
# 5ème doit passer, 6ème doit retourner 429
```

4. **Headers sécurité présents :**
```bash
curl -I http://prod-url/api/salons
# Doit contenir X-Frame-Options, X-Content-Type-Options, CSP, etc.
```

## 📝 Notes

- Les credentials en dur ont été supprimés de `application.yml`
- Le fichier `.env` est dans `.gitignore` (ne sera jamais commité)
- En production, utiliser **uniquement** le profil `prod`
- Générer un `JWT_SECRET` sécurisé (minimum 256 bits) :
  ```bash
  openssl rand -base64 32
  ```

