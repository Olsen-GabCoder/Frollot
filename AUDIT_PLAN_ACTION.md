# Audit Complet du Projet & Plan d'Action

## Résumé de l'Architecture du Projet

Ce projet est une application full-stack développée avec Kotlin Multiplatform.

*   **Backend** :
    *   Utilise Kotlin/JVM, structuré avec des packages pour la configuration (`config`), les contrôleurs (`controller`), les objets de transfert de données (`dto`), les exceptions (`exception`), les modèles de données (`model`), les dépôts (`repository`), la sécurité (`security`) et les services (`service`).
    *   Gère l'authentification, la gestion des salons de coiffure, les réservations, les paiements, les médias, les fonctionnalités sociales, et les diagnostics.
    *   Utilise Gradle pour la gestion des dépendances et du build.
    *   Comprend des scripts PowerShell pour l'automatisation.

*   **Frontend** :
    *   Développé avec Kotlin Multiplatform (KMM) et Compose Multiplatform, ciblant Android (`androidMain`), le Web avec JavaScript (`jsMain`), et WebAssembly (`wasmJsMain`). Une cible `webMain` est également présente mais semble moins active.
    *   Partage la logique métier et potentiellement des parties de l'UI (`commonMain`).
    *   Comprend des packages pour l'authentification, la configuration, la localisation, les modèles, le réseau, les préférences utilisateur, le temps et l'UI.
    *   Utilise Gradle et Webpack pour le build.

*   **Documentation** :
    *   Présence d'un répertoire `docs/` avec des analyses détaillées sur l'authentification, la gestion des tokens, la sécurité, et d'autres aspects techniques.

## Fonctionnalités Existantes (Identifiées via l'analyse des fichiers)

*   **Gestion des Utilisateurs & Authentification** : Inscription, connexion, gestion des profils utilisateur, rafraîchissement des tokens, vérification d'e-mail.
*   **Gestion des Salons** : Création, lecture, mise à jour et suppression de salons de coiffure, gestion du personnel et des services offerts par les salons, portfolios.
*   **Réservations & Files d'attente** : Planification de rendez-vous, gestion des files d'attente.
*   **Paiements** : Intégration de fonctionnalités de paiement.
*   **Gestion de Contenu** : Upload et gestion des médias (images), suivi d'utilisateurs/salons, avis et critiques.
*   **Fonctionnalités Générales** : Configuration d'environnement, diagnostics, localisation (i18n), préférences utilisateur, notifications (Android).

## Fonctionnalités Manquantes ou à Améliorer

### 1. **Fonctionnalité "Mot de Passe Oublié" (Priorité)**
*   **État Actuel** : Non implémentée.
*   **Description** : Permettre aux utilisateurs de réinitialiser leur mot de passe s'ils l'ont oublié.
*   **Impact** : Essentielle pour l'expérience utilisateur et la gestion des comptes.

### 2. **Déploiement et Démarrage du Frontend Web**
*   **État Actuel** : Non fonctionnel, persistance d'erreurs de chargement du module WebAssembly (`composeApp.wasm` introuvable) malgré les tentatives de correction.
*   **Description** : Le build web se termine mais l'application ne démarre pas dans le navigateur en raison de problèmes de chargement des assets, spécifiquement le fichier WASM et `styles.css`.
*   **Impact** : Bloque tout développement et test du frontend web.

### 3. **Gestion des Erreurs et Feedback Utilisateur**
*   **État Actuel** : Inconnu, mais potentiellement à améliorer.
*   **Description** : Mettre en place une gestion robuste des erreurs (API et UI) avec des messages clairs pour l'utilisateur, des indicateurs de chargement, et des états d'erreur visuels.
*   **Impact** : Améliore l'expérience utilisateur et le débogage.

### 4. **Notifications Push (Frontend Web)**
*   **État Actuel** : Implémenté sur Android, manquant sur le Web.
*   **Description** : Intégrer les notifications push pour les utilisateurs web via les Service Workers.
*   **Impact** : Permet une communication proactive avec les utilisateurs web.

### 5. **Optimisation de l'Expérience Utilisateur (UX) du Frontend Web**
*   **État Actuel** : Inconnu, mais les avertissements de taille de bundle de Webpack suggèrent des optimisations nécessaires.
*   **Description** :
    *   **Accessibilité (A11y)** : S'assurer que l'application est utilisable par tous, y compris les personnes ayant des handicaps.
    *   **Responsivité** : Garantir une expérience utilisateur optimale sur différents appareils et tailles d'écran.
    *   **Performance du Bundle** : Mettre en œuvre le "code splitting" et d'autres optimisations Webpack pour réduire les temps de chargement.
*   **Impact** : Améliore la rétention des utilisateurs et l'image de marque.

### 6. **Internationalisation (i18n)**
*   **État Actuel** : Présence d'un package `localization`, mais l'étendue de son implémentation est à vérifier.
*   **Description** : S'assurer que toutes les chaînes de texte de l'application sont externalisées et qu'un mécanisme de changement de langue est disponible et fonctionnel.
*   **Impact** : Permet de cibler un public plus large.

### 7. **Tests Automatisés**
*   **État Actuel** : Présence de quelques fichiers de test, mais la couverture n'est pas connue.
*   **Description** :
    *   **Backend** : Augmenter la couverture des tests unitaires, d'intégration et end-to-end.
    *   **Frontend** : Implémenter des tests unitaires pour la logique métier partagée et des tests d'intégration/UI pour les composants Compose.
*   **Impact** : Réduit les bugs, facilite la maintenance et les refactorings.

### 8. **Intégration et Déploiement Continus (CI/CD)**
*   **État Actuel** : Scripts d'automatisation existants, mais pas de pipeline CI/CD formel.
*   **Description** : Mettre en place des pipelines CI/CD pour automatiser les processus de build, de test et de déploiement sur toutes les plateformes.
*   **Impact** : Accélère le cycle de développement, améliore la qualité et réduit les erreurs de déploiement.

### 9. **Monitoring et Alerting**
*   **État Actuel** : Logging basique, mais manque de monitoring proactif.
*   **Description** : Intégrer des outils de monitoring pour suivre les performances de l'application en temps réel et configurer des alertes pour les problèmes critiques.
*   **Impact** : Permet de détecter et de résoudre rapidement les problèmes en production.

## Plan d'Action Détaillé

### Phase 1 : Résolution de la Fonctionnalité "Mot de Passe Oublié" (Priorité Absolue)

**Objectif** : Implémenter la fonctionnalité complète de réinitialisation de mot de passe.

#### Backend
1.  **Créer de nouveaux Endpoints dans `UserController.kt`** :
    *   `POST /api/auth/forgot-password` : Prend l'e-mail de l'utilisateur, génère un token de réinitialisation, l'enregistre en base de données avec une date d'expiration, et envoie un e-mail à l'utilisateur.
    *   `POST /api/auth/reset-password` : Prend le token de réinitialisation et le nouveau mot de passe, valide le token, réinitialise le mot de passe, et invalide le token.
2.  **Ajouter un Service de Réinitialisation de Mot de Passe (`service/PasswordResetService.kt`)** :
    *   Logique de génération et de validation des tokens.
    *   Intégration avec un service d'envoi d'e-mails (potentiellement via une nouvelle dépendance ou un service existant si disponible).
3.  **Mettre à jour le Modèle Utilisateur (`model/User.kt`) et le Dépôt Utilisateur (`repository/UserRepository.kt`)** :
    *   Ajouter des champs pour le token de réinitialisation du mot de passe et sa date d'expiration.
4.  **Mettre à jour la Base de Données** :
    *   Ajouter les colonnes nécessaires (token, date d'expiration) à la table des utilisateurs via une migration Flyway (dans `backend/src/main/resources/db/migration/`).

#### Frontend (Android et Web - l'implémentation web sera plus tard)
1.  **Créer un Écran "Mot de Passe Oublié"** :
    *   Un champ pour saisir l'e-mail de l'utilisateur.
    *   Un bouton pour envoyer la demande de réinitialisation.
2.  **Créer un Écran "Réinitialiser le Mot de Passe"** :
    *   Deux champs pour le nouveau mot de passe et sa confirmation.
    *   Un champ caché ou une logique pour récupérer le token de réinitialisation (généralement depuis l'URL).
    *   Un bouton pour soumettre le nouveau mot de passe.
3.  **Intégrer les Appels API** :
    *   Utiliser les nouveaux endpoints backend pour la demande et la réinitialisation du mot de passe.
4.  **Gérer les États UI et les Retours Utilisateur** :
    *   Afficher des messages de succès/erreur clairs.
    *   Gérer les états de chargement.

### Phase 2 : Améliorations du Frontend Web (après résolution du problème de déploiement)

**Objectif** : Rendre le frontend web fonctionnel et optimisé.

1.  **Débogage Approfondi du Chargement WASM** :
    *   **Action** : Utiliser un serveur Node.js temporaire (comme `serve` ou `http-server`) à la place du serveur Python pour s'assurer que le problème n'est pas lié au type MIME.
    *   **Action** : Si le problème persiste, inspecter les headers HTTP et la réponse du serveur.
    *   **Action** : Si le problème est côté client, examiner les outils de développement du navigateur pour les erreurs JavaScript spécifiques au chargement WASM.

2.  **Optimisation du Bundle Web** :
    *   **Action** : Mettre en œuvre le "code splitting" pour charger les parties de l'application à la demande.
    *   **Action** : Examiner les rapports d'analyse de bundle Webpack pour identifier les gros contributeurs et les optimiser.

3.  **Amélioration de l'Expérience de Développement (DX)** :
    *   **Action** : Configurer `webpack-dev-server` avec Hot Module Replacement (HMR) si possible pour des rechargements rapides de l'application pendant le développement.

### Phase 3 : Autres Fonctionnalités et Améliorations Générales

**Objectif** : Étendre les fonctionnalités et améliorer la qualité générale du projet.

1.  **Internationalisation (i18n)** :
    *   **Action** : Auditer toutes les chaînes de texte de l'application (frontend et backend) et s'assurer qu'elles sont externalisées.
    *   **Action** : Implémenter un sélecteur de langue dans l'interface utilisateur.

2.  **Accessibilité (A11y) & Responsive Design** :
    *   **Action** : Effectuer des tests d'accessibilité (par exemple, navigation au clavier, lecteurs d'écran).
    *   **Action** : S'assurer que tous les composants UI sont responsives et s'adaptent aux différentes tailles d'écran.

3.  **Renforcement des Tests Automatisés** :
    *   **Action** : Augmenter la couverture des tests unitaires et d'intégration pour le backend.
    *   **Action** : Introduire des tests unitaires et d'intégration pour le frontend (logique métier partagée, composants UI).
    *   **Action** : Mettre en place des tests end-to-end pour les parcours utilisateurs critiques.

4.  **Intégration et Déploiement Continus (CI/CD)** :
    *   **Action** : Définir des pipelines CI/CD (par exemple, GitHub Actions, GitLab CI) pour automatiser :
        *   Le build et les tests du backend.
        *   Le build et les tests du frontend (pour chaque cible).
        *   Le déploiement des nouvelles versions.

5.  **Monitoring et Alerting** :
    *   **Action** : Intégrer des outils de monitoring (par exemple, Prometheus, Grafana, Sentry) pour collecter des métriques et des logs en production.
    *   **Action** : Configurer des alertes pour les erreurs, les performances lentes ou les indisponibilités.
