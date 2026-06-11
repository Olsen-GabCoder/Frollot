# 🚀 Guide de Déploiement en Production - Frollot Backend

## 📋 Vue d'ensemble

Ce guide détaille le processus complet de déploiement en production du backend Frollot, incluant la sécurité, le monitoring et les bonnes pratiques.

## 🔐 Phase 1: Configuration de Sécurité

### 1.1 Génération des Secrets

```powershell
# Générer tous les secrets cryptographiques
cd backend/scripts
.\generate-secrets.ps1 -OutputFile production-secrets.env
```

**Sortie attendue:**
- `JWT_SECRET`: 512+ bits de clé JWT
- `DB_PASSWORD`: Mot de passe complexe de 32 caractères
- `STRIPE_WEBHOOK_SECRET`: Secret webhook Stripe

### 1.2 Variables d'Environnement de Production

Créer un fichier `.env.production` avec:

```bash
# Base de données (production)
DB_URL=jdbc:mysql://prod-db-host:3306/coiffure_db?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=false
DB_USERNAME=coiffure_prod_user
DB_PASSWORD=<mot_de_passe_généré>

# Sécurité JWT
JWT_SECRET=<clé_générée>
JWT_EXPIRATION_HOURS=168
JWT_REFRESH_EXPIRATION_HOURS=8760

# Email (production)
EMAIL_ENABLED=true
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=noreply@frollot.com
SMTP_PASSWORD=<mot_de_passe_app>

# Stripe (clés de production)
STRIPE_SECRET_KEY=sk_live_...
STRIPE_PUBLISHABLE_KEY=pk_live_...
STRIPE_WEBHOOK_SECRET=<secret_généré>

# Logging production
LOG_LEVEL_ROOT=INFO
LOG_LEVEL_FROLLOT=INFO
```

## 🏗️ Phase 2: Infrastructure

### 2.1 Configuration Base de Données

```yaml
# docker-compose.prod.yml
services:
  mysql:
    image: 'mysql:8.0'
    environment:
      MYSQL_DATABASE: coiffure_db
      MYSQL_USER: coiffure_prod_user
      MYSQL_PASSWORD: <mot_de_passe_généré>
      MYSQL_ROOT_PASSWORD: <root_password_complexe>
    volumes:
      - mysql_data:/var/lib/mysql
      - ./backups:/backups
    command: --default-authentication-plugin=mysql_native_password
    networks:
      - frollot_prod
```

### 2.2 Réseau et Sécurité

```yaml
networks:
  frollot_prod:
    driver: bridge

volumes:
  mysql_data:
    driver: local
```

## 🚀 Phase 3: Déploiement

### 3.1 Script de Déploiement

```powershell
# Déploiement complet
.\backend\scripts\deploy-production.ps1 -Environment prod
```

**Le script effectue:**
- ✅ Validation de l'environnement
- 💾 Sauvegarde automatique
- 🔨 Compilation et tests
- 🛑 Arrêt propre de l'ancienne version
- ▶️ Démarrage de la nouvelle version
- 🏥 Health checks post-déploiement

### 3.2 Vérifications Post-Déploiement

Après déploiement, vérifier:

```bash
# Health checks
curl http://localhost:9090/manage/health
curl http://localhost:9090/manage/info

# Métriques
curl http://localhost:9090/manage/metrics

# Logs
tail -f logs/application.log
```

## 📊 Phase 4: Monitoring et Alertes

### 4.1 Métriques Disponibles

L'application expose les métriques suivantes:

```
# Authentification
frollot.auth.login.attempts
frollot.auth.login.success/failures
frollot.auth.password.reset.requests

# Réservations
frollot.bookings.created/cancelled/completed

# Base de données
frollot.db.connections.active
frollot.db.query.duration

# Sécurité
frollot.security.rate_limit.exceeded
frollot.security.suspicious_activities
```

### 4.2 Alertes Recommandées

**Critiques:**
- Health check DOWN
- Erreurs de connexion DB
- JWT secret par défaut détecté

**Avertissements:**
- Taux d'échec login > 10%
- Rate limiting dépassé
- Migrations Flyway échouées

### 4.3 Configuration Prometheus/Grafana

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'frollot-backend'
    static_configs:
      - targets: ['localhost:9090']
    metrics_path: '/manage/prometheus'
```

## 🔄 Phase 5: Maintenance

### 5.1 Mises à Jour

```powershell
# Mise à jour mineure (pas de migration)
.\deploy-production.ps1 -Environment prod

# Mise à jour avec migration DB
# 1. Sauvegarde DB
mysqldump -u root -p coiffure_db > backup_pre_migration.sql

# 2. Déploiement
.\deploy-production.ps1 -Environment prod

# 3. Vérification migrations
mysql -u root -p coiffure_db < scripts/flyway-migration-guard.sql
```

### 5.2 Rotation des Secrets

```powershell
# Tous les 90 jours
.\generate-secrets.ps1 -OutputFile new-secrets.env -Force
# Mettre à jour les variables d'environnement
# Redémarrer l'application
```

## 🚨 Phase 6: Incident Response

### 6.1 Rollback d'Urgence

```powershell
# Arrêt immédiat
Stop-Process -Name "java" -Force

# Restauration backup DB
mysql -u root -p coiffure_db < backup_recent.sql

# Redémarrage version précédente
.\deploy-production.ps1 -Environment prod -SkipHealthCheck
```

### 6.2 Diagnostic

```powershell
# Logs détaillés
tail -100 logs/application.log

# Health check détaillé
curl http://localhost:9090/manage/health?details=true

# Métriques de performance
curl http://localhost:9090/manage/metrics | jq '.names[]'
```

## ✅ Checklist de Production

### Sécurité
- [ ] JWT_SECRET généré aléatoirement (512+ bits)
- [ ] Mots de passe DB complexes
- [ ] Clés Stripe de production
- [ ] SSL/TLS activé pour DB
- [ ] CORS configuré restrictivement
- [ ] Rate limiting actif

### Infrastructure
- [ ] Base de données dédiée production
- [ ] Réseau isolé
- [ ] Sauvegardes automatiques
- [ ] Monitoring configuré
- [ ] Alertes actives

### Application
- [ ] Profil `prod` actif
- [ ] Health checks fonctionnels
- [ ] Métriques exposées
- [ ] Logs structurés
- [ ] Timeouts appropriés

### Déploiement
- [ ] Scripts de déploiement testés
- [ ] Procédures rollback documentées
- [ ] Tests post-déploiement automatisés
- [ ] Validation manuelle avant mise en prod

## 📞 Support

En cas de problème:
1. Consulter les logs: `logs/application.log`
2. Vérifier les health checks
3. Examiner les métriques
4. Rollback si nécessaire
5. Escalader vers l'équipe technique

---

**Version:** 1.0.0
**Dernière mise à jour:** $(Get-Date -Format 'yyyy-MM-dd')
**Auteur:** Équipe Frollot