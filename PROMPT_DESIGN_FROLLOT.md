# PROMPT : Refonte Design Complète de l'Application Frollot

> Ce document est un brief de design complet destiné a Claude. Il contient tout le contexte necessaire pour concevoir une charte graphique professionnelle et redesigner l'integralite des ecrans de l'application Frollot.

---

## 1. PRESENTATION DU PROJET

### Qu'est-ce que Frollot ?

Frollot est une **plateforme sociale et marketplace** dediee a la **coiffure et a la beaute**. Elle connecte trois types d'acteurs :

- **Les clients** : recherchent des salons, reservent des prestations, laissent des avis, suivent des coiffeurs
- **Les coiffeurs (hairstylists)** : presentent leur travail via des portfolios, publient des posts (avant/apres, tendances, conseils), gerent leur agenda
- **Les proprietaires de salons** : gerent leur salon (services, equipe, reservations, file d'attente, paiements), publient du contenu social

Un 4eme type, **admin**, existe pour la moderation.

### Stack technique

- **Frontend** : Kotlin Multiplatform (Compose Multiplatform 1.7.1) ciblant **Android** et **Web (WasmJs)**
- **Backend** : Spring Boot 3.4 / Kotlin avec MySQL 8, JWT, Stripe, Firebase
- **UI Framework** : Jetpack Compose avec **Material Design 3**
- **Icones** : `compose.materialIconsExtended` (deja en dependance)
- **Images** : Coil 3.0.4 (chargement asynchrone)
- **Navigation** : Jetpack Navigation Compose (type-safe routes)
- **Localisation** : 5 langues (FR, EN, ES, DE, AR avec support RTL)
- **Theming** : MaterialTheme avec light/dark mode

### Positionnement et identite

L'application doit evoquer :
- **Le luxe accessible** : elegance sans elitisme
- **La beaute et le soin** : douceur, raffinement, attention au detail
- **Le professionnalisme** : serieux, fiabilite, confiance
- **La modernite** : design contemporain, interactions fluides
- **La communaute** : partage, inspiration, connexion entre passionnes

L'application ne doit PAS avoir l'air :
- Enfantine, amatrice, generique ou cheap
- Surchargee visuellement
- Identique a une app de e-commerce basique

---

## 2. ETAT ACTUEL DU DESIGN (A REFAIRE INTEGRALEMENT)

### Problemes identifies

1. **Aucune coherence visuelle** : chaque ecran semble avoir ete designe independamment
2. **Pas de vrai design system** : les composants ne sont pas standardises, beaucoup de valeurs hardcodees
3. **Emojis Unicode utilises comme icones** : 258 occurrences de faux-icones (checkmarks, etoiles, coeurs, ciseaux) au lieu de vraies icones Material
4. **Ecrans de connexion/inscription banals** : aucune identite visuelle forte
5. **Hierarchie visuelle floue** : manque de contraste entre les elements importants et secondaires
6. **Espacement incoherent** : paddings et margins variables d'un ecran a l'autre
7. **Surcharge d'information** : certains ecrans (SalonDetail, BookingScreen) sont tres denses sans organisation claire

### Theme actuel (a remplacer)

```
LIGHT MODE :
- Primary : #6B46C1 (Violet)
- Secondary : #EC4899 (Rose)
- Tertiary : #F59E0B (Ambre)
- Surface : #FFFFFF
- OnSurface : #0F172A
- Error : #DC2626

DARK MODE :
- Primary : #A78BFA
- Secondary : #F472B6
- Tertiary : #FBBF24
- Surface : #121212

TYPOGRAPHY : Pas de police custom, tailles Material 3 standard
SHAPES : 8dp, 12dp, 16dp, 24dp, 32dp
```

---

## 3. ARCHITECTURE DES ECRANS (46 ECRANS)

Voici la liste exhaustive de tous les ecrans existants, organises par flux fonctionnel. Pour chaque ecran, le contenu actuel et les interactions sont decrits.

### 3.1 FLUX D'AUTHENTIFICATION (5 ecrans)

#### LoginScreen.kt (~850 lignes)
- **Contenu** : Logo anime Frollot en haut, titre "Bienvenue", sous-titre, champ email, champ mot de passe (avec toggle visibilite), bouton "Se connecter", lien "Mot de passe oublie ?", separateur "ou", bouton "Creer un compte"
- **Logique** : Appel API login, stockage JWT, redirection vers Home, gestion erreurs (credentials invalides, email non verifie)
- **Interactions** : Validation en temps reel, loading state sur bouton, toast succes/erreur

#### RegisterScreen.kt (~700 lignes)
- **Contenu** : Logo, titre "Rejoignez-nous", selection du type de compte (client/coiffeur/proprietaire) via ExposedDropdownMenu, champs : prenom, nom, email, telephone, mot de passe, confirmation mot de passe, bouton "S'inscrire", lien vers login
- **Logique** : Pre-inscription securisee, envoi email de verification, validation formulaire (regex email, force mot de passe, correspondance confirmation)
- **Interactions** : Selection type de compte, validation champs, loading, erreurs inline

#### EmailVerificationScreen.kt (~450 lignes)
- **Contenu** : Icone email, titre "Verifiez votre email", message avec adresse email, champ token (6 caracteres), boutons "Verifier" et "Renvoyer le code", lien retour login
- **Logique** : Verification du token, renvoi automatique, compteur avant renvoi, completion de l'inscription
- **Interactions** : Saisie token, countdown timer, renvoi code, redirect vers login apres succes

#### ForgotPasswordScreen.kt (~300 lignes)
- **Contenu** : Icone, titre "Mot de passe oublie", description, champ email, bouton "Envoyer le lien", lien retour login
- **Logique** : Envoi email de reset, confirmation d'envoi
- **Interactions** : Validation email, loading, succes/erreur

#### ResetPasswordScreen.kt (~350 lignes)
- **Contenu** : Titre, champ token, champ nouveau mot de passe, champ confirmation, bouton "Reinitialiser", indicateur de force du mot de passe
- **Logique** : Validation token + nouveau mot de passe, appel API, redirect login
- **Interactions** : Validation en temps reel, toggle visibilite, force indicator

---

### 3.2 ECRANS PRINCIPAUX (5 ecrans avec drawer)

#### HomeScreen.kt (~600 lignes)
- **Header** : StandardAppHeader avec menu burger, titre, avatar utilisateur, barre de recherche
- **Contenu** :
  - Section "Salons recents" : grille horizontale de SalonCard
  - Section "Services populaires" : chips de categories de services
  - Actions rapides : boutons "Reserver", "Creer un salon" (si proprietaire)
  - Salons a proximite (si geolocalisation disponible)
- **Navigation** : Vers SalonDetail, Booking, CreateSalon, Search

#### SocialFeedScreen.kt (~800 lignes)
- **Header** : Titre "Fil Social", onglets de filtrage (Tous, Suivis, Tendances)
- **Contenu** : LazyColumn de posts (UltraPremiumPostCard) avec :
  - Avatar auteur + nom + type + date
  - Contenu texte avec hashtags cliquables
  - Media (images, galerie, avant/apres)
  - Barre d'engagement : likes, commentaires, partages, favoris, archives
  - Menu contextuel (signaler, masquer, supprimer si proprietaire)
  - Reactions animees (menu flottant style Instagram)
- **Interactions** : Pull-to-refresh, scroll infini, like/unlike, reactions, commentaires, partage, navigation vers profils/salons

#### ProfileScreen.kt (~700 lignes)
- **Contenu** :
  - ProfileHeaderCard : photo de couverture, avatar, nom, bio, statistiques (posts, followers, following)
  - Bouton Editer profil / Suivre (selon si c'est son profil)
  - Sections : Posts recents, Salons favoris, Badges
  - Actions : changer avatar, changer couverture, modifier bio
- **Navigation** : Vers Settings, EditProfile, ChangeEmail, ChangePhone

#### MyBookingsScreen.kt (~800 lignes)
- **Header** : Titre "Mes rendez-vous"
- **Filtres** : Chips d'etat (Tous, En attente, Confirme, Termine, Annule)
- **Contenu** : Liste de reservations avec :
  - Nom du salon + service
  - Date/heure + duree
  - Statut (badge colore)
  - Coiffeur assigne
  - Prix
  - Actions contextuelles (annuler, confirmer, payer)
- **Navigation** : Vers BookingDetail, SalonDetail, Payment

#### SettingsScreen.kt (~500 lignes)
- **Contenu** : Sections de parametres :
  - **Compte** : Profil, Email, Telephone, Mot de passe, Securite
  - **Preferences** : Mode sombre (toggle), Langue (selection), Notifications
  - **Paiement** : Methodes de paiement, Historique
  - **Support** : Centre d'aide, Contact, Conditions, Confidentialite
  - **Danger zone** : Supprimer le compte, Deconnexion
- **Interactions** : Toggles, navigation vers sous-ecrans

---

### 3.3 GESTION DES SALONS (7 ecrans)

#### CreateSalonScreen.kt (~500 lignes)
- **Formulaire** : Nom, description, adresse, ville, code postal, photo de couverture (upload)
- **Validation** : Champs obligatoires, format adresse
- **Actions** : Creer, Annuler

#### SalonDetailScreen.kt (~2900 lignes - ecran le plus complexe)
- **Sections** :
  - Photo de couverture + nom + note moyenne + adresse
  - Onglets : Services | Equipe | Avis | Posts | Info
  - **Onglet Services** : liste des prestations avec prix, duree, categorie, bouton reserver
  - **Onglet Equipe** : liste du staff avec specialites, statut actif/inactif
  - **Onglet Avis** : statistiques de notation + liste de ReviewCard
  - **Onglet Posts** : fil social du salon
  - **Onglet Info** : adresse complete, horaires, contact, reseaux sociaux
  - Boutons flottants : Reserver, Rejoindre la file d'attente, Suivre
  - Section file d'attente en temps reel (QueueStatusCard)
- **Navigation** : Vers Booking, QueueManagement, CreateService, CreateStaff, SalonPosts

#### CreateServiceScreen.kt (~400 lignes)
- **Formulaire** : Nom de la prestation, categorie (dropdown), description, prix, duree (minutes), images
- **Categories** : COUPE, COLORATION, SOIN, COIFFAGE, BARBE, TECHNIQUE, AUTRE

#### CreateStaffScreen.kt (~350 lignes)
- **Formulaire** : Selection d'un utilisateur existant (type coiffeur), specialites (multi-select chips), statut actif

#### QueueManagementScreen.kt (~500 lignes)
- **Contenu** :
  - Statut de la file (ouverte/fermee)
  - Liste des clients en attente avec position, heure d'arrivee, duree estimee
  - Actions : Appeler le suivant, Retirer de la file
  - Statistiques : temps moyen d'attente, clients servis aujourd'hui

#### SalonPostsScreen.kt (~400 lignes)
- **Contenu** : Fil de posts specifiques au salon, meme format que SocialFeed mais filtre

#### SalonSocialProfileScreen.kt (~600 lignes)
- **Contenu** : Profil social enrichi du salon avec photo de couverture, description sociale, statistiques (followers, posts, avis), grille de posts recents

---

### 3.4 RESERVATIONS ET PAIEMENTS (5 ecrans)

#### BookingScreen.kt (~2100 lignes - ecran tres complexe)
- **Flux en etapes** :
  1. **Selection du service** : liste des prestations du salon avec prix/duree
  2. **Selection de la date** : calendrier interactif avec jours disponibles
  3. **Selection de l'heure** : grille de creneaux horaires disponibles
  4. **Selection du coiffeur** (optionnel) : liste du staff disponible
  5. **Recapitulatif** : resume complet + notes du client + bouton confirmer
- **Animations** : Transitions entre etapes, loading states
- **Validation** : Verification disponibilite en temps reel

#### BookingDetailScreen.kt (~500 lignes)
- **Contenu** : Toutes les informations de la reservation :
  - Salon + service + coiffeur
  - Date/heure + duree
  - Statut avec timeline visuelle (etapes de progression)
  - Prix + statut paiement
  - Notes client / salon
  - Actions : Annuler, Modifier, Payer, Laisser un avis

#### OwnerBookingsManagementScreen.kt (~700 lignes)
- **Vue proprietaire** : Toutes les reservations du salon
  - Filtres par statut, date, coiffeur
  - Actions : Confirmer, Rejeter, Assigner un coiffeur, Marquer comme termine
  - Statistiques : reservations du jour/semaine

#### PaymentFlowScreen.kt (~600 lignes)
- **Flux Stripe** :
  - Resume de la commande (service, prix, taxes)
  - Selection methode de paiement
  - Champ carte (CardInputField)
  - Bouton "Payer X EUR"
  - Ecran de succes avec animation

#### PaymentHistoryScreen.kt (~500 lignes)
- **Contenu** : Historique des transactions
  - Filtres : Tous, Payes, Rembourses, En attente
  - Liste avec : montant, date, salon, service, statut
  - Detail de chaque transaction au tap
  - Statistiques : total depense, moyenne par mois

---

### 3.5 RESEAU SOCIAL (8 ecrans)

#### CreatePostScreen.kt (~550 lignes)
- **Formulaire** :
  - Type de post (dropdown) : General, Avant/Apres, Portfolio, Tendance, Conseil, Realisation, Inspiration
  - Contenu texte (riche, avec hashtags)
  - Upload media (images multiples)
  - Visibilite : Public, Followers, Prive
  - Tags de services lies
  - Salon associe (si applicable)
- **Actions** : Publier, Brouillon, Annuler

#### PostDetailScreen.kt (~600 lignes)
- **Contenu** : Vue complete d'un post :
  - Auteur (avatar + nom + type + verification badge)
  - Contenu texte avec hashtags cliquables
  - Media (galerie avec viewer plein ecran)
  - Compteurs : likes, commentaires, partages
  - Reactions animees
  - Fil de commentaires inline
  - Champ pour ajouter un commentaire

#### CommentsScreen.kt (~400 lignes)
- **Contenu** : Fil de commentaires complet
  - Liste de commentaires avec avatar, nom, date, texte
  - Actions : liker un commentaire, repondre, signaler
  - Champ de saisie en bas avec bouton envoyer

#### UltraPremiumPostCard.kt (~1800 lignes - composant riche)
- **Carte de post** ultra-detaillee :
  - Header : avatar, nom, type badge, date, menu options
  - Corps : texte avec parsing hashtags/mentions, media gallery
  - Mode Avant/Apres : slider ou side-by-side
  - Footer : boutons like, comment, share, favorite, archive
  - Compteurs animes
  - Reactions flottantes
  - Badge "Sponsorise" / "Trending"

#### CreateReviewScreen.kt (~400 lignes)
- **Formulaire** :
  - Nom du salon + service (pre-rempli)
  - Notation etoiles (1-5, interactif)
  - Titre de l'avis
  - Texte de l'avis
  - Photos (optionnel)
  - Bouton publier

#### TrendingScreen.kt (~500 lignes)
- **Contenu** :
  - Filtres de periode : 24h, 7 jours, 30 jours
  - Onglets : Posts | Salons | Hashtags
  - Posts tendance avec indicateur de tendance
  - Salons populaires avec metriques
  - Hashtags trending avec compteurs

#### FavoritesScreen.kt (~400 lignes)
- **Contenu** : Posts et salons favoris de l'utilisateur
  - Filtres : Tous, Posts, Salons
  - Grille ou liste de favoris
  - Actions : retirer des favoris, naviguer vers le contenu

#### ArchivesScreen.kt (~380 lignes)
- **Contenu** : Posts archives par l'utilisateur
  - Liste avec actions : desarchiver, supprimer
  - Affichage similaire au feed social

---

### 3.6 PORTFOLIOS ET COLLECTIONS (6 ecrans)

#### CreatePortfolioScreen.kt (~450 lignes)
- **Formulaire** : Nom, description, visibilite (public/prive), selection de posts existants a inclure

#### PortfoliosListScreen.kt (~350 lignes)
- **Contenu** : Grille de portfolios d'un utilisateur/salon
  - Miniature, nom, nombre de posts, visibilite

#### PortfolioDetailScreen.kt (~400 lignes)
- **Contenu** : Detail d'un portfolio
  - Cover image, nom, description, auteur
  - Grille de posts inclus
  - Actions : modifier, supprimer, partager

#### CollectionsScreen.kt (~500 lignes)
- **Contenu** : Collections thematiques de l'utilisateur
  - Categories : Inspiration, Portfolio, Trending, Personnel
  - Dropdown pour filtrer par categorie
  - Grille de collections avec miniatures

#### CollectionDetailScreen.kt (~450 lignes)
- **Contenu** : Detail d'une collection
  - Nom, description, nombre de posts
  - Liste de posts inclus
  - Actions : ajouter/retirer posts, modifier, supprimer

#### SearchScreen.kt (~800 lignes)
- **Contenu** :
  - Barre de recherche avec suggestions en temps reel
  - Onglets de resultats : Tout | Salons | Posts | Utilisateurs | Hashtags
  - Filtres avances : localisation, categorie de service, notation minimum
  - Resultats avec cards adaptees a chaque type
  - Historique de recherche

---

### 3.7 PROFILS UTILISATEURS (4 ecrans)

#### CoiffeurProfileScreen.kt (~600 lignes)
- **Contenu** :
  - ProfileHeaderCard avec photo couverture et avatar
  - Bio, specialites (chips), annees d'experience, certifications
  - Statistiques : posts, followers, following
  - Onglets : Posts | Portfolios | Avis
  - Bouton Suivre / Ne plus suivre

#### ClientProfileScreen.kt (~500 lignes)
- **Contenu** :
  - ProfileHeaderCard simplifie
  - Bio, statistiques
  - Onglets : Posts | Collections | Favoris
  - Bouton Suivre

#### SalonOwnerProfileScreen.kt (~550 lignes)
- **Contenu** :
  - ProfileHeaderCard
  - Liste des salons possedes
  - Statistiques globales (tous salons)
  - Posts recents
  - Bouton Suivre

#### SalonSocialProfileScreen.kt (~600 lignes)
- **Contenu** : Vue enrichie du profil social d'un salon
  - Photo couverture + logo/avatar du salon
  - Description sociale, lien site web, reseaux sociaux
  - Statistiques : followers, posts, avis, note moyenne
  - Grille de posts recents avec engagement

---

### 3.8 SECURITE ET COMPTE (4 ecrans)

#### SecuritySettingsScreen.kt (~1100 lignes)
- **Sections** :
  - Changer le mot de passe (ancien + nouveau + confirmation)
  - Authentification a deux facteurs (toggle + config)
  - Sessions actives : liste des appareils connectes avec bouton revoquer
  - Deconnecter toutes les sessions
  - Supprimer le compte (zone danger)

#### ChangeEmailScreen.kt (~300 lignes)
- **Formulaire** : Email actuel (lecture seule), nouvel email, mot de passe pour confirmer

#### ChangePhoneScreen.kt (~300 lignes)
- **Formulaire** : Telephone actuel, nouveau numero, verification OTP

#### RequestVerificationScreen.kt (~400 lignes)
- **Contenu** : Demande de badge verifie
  - Type de verification : Email, Telephone, Business, Professional
  - Upload de documents justificatifs
  - Statut de la demande

---

### 3.9 MODERATION (1 ecran + dialog)

#### ReportScreen.kt (~400 lignes)
- **Contenu** :
  - Type d'entite signalee (post, commentaire, utilisateur, salon)
  - Selection de la raison : Inapproprie, Spam, Faux, Copyright, Autre
  - Description de chaque raison
  - Champ texte additionnel
  - Bouton envoyer le signalement

---

### 3.10 PAGES PLACEHOLDER (6 ecrans)

#### PlaceholderSettingsScreens.kt
Ecrans minimalistes avec titre + message "Bientot disponible" :
- BlockedUsersScreen
- PaymentMethodsScreen
- HelpCenterScreen
- ContactSupportScreen
- TermsOfServiceScreen
- PrivacyPolicyScreen

---

## 4. COMPOSANTS REUTILISABLES EXISTANTS (41 composants)

### Composants de navigation
| Composant | Description |
|-----------|------------|
| AppDrawer | Tiroir de navigation lateral avec sections par role (client, coiffeur, proprietaire, admin) |
| StandardAppHeader | Barre superieure avec menu/retour, titre, recherche, avatar, notifications |

### Composants de marque
| Composant | Description |
|-----------|------------|
| FrollotLogo | Logo anime "F" avec variantes (outline, filled, gradient, compact, avec texte) |
| UserAvatar | Avatar circulaire avec initiales ou photo, bordure optionnelle gradient |
| VerificationBadge | Badge de verification (icone + texte optionnel) |

### Composants de formulaire
| Composant | Description |
|-----------|------------|
| StandardTextField | Champ texte avec label, placeholder, icones, validation, etats erreur |
| PasswordTextField | Champ mot de passe avec toggle visibilite |
| SearchTextField | Champ de recherche avec icone et suggestions |
| StandardForm | Conteneur de formulaire avec gestion de validation |
| AnnotatedTextField | Champ texte avec annotations (caracteres speciaux, compteur) |

### Composants de bouton
| Composant | Description |
|-----------|------------|
| PrimaryButton | Bouton principal (fond primary, texte blanc, tailles Standard/Large) |
| SecondaryButton | Bouton secondaire |
| TextButton | Bouton texte sans fond |

### Composants de carte
| Composant | Description |
|-----------|------------|
| StandardCard | Carte generique Material 3 |
| SalonCard | Carte de salon avec image, nom, localisation, note, bouton reserver |
| ReviewCard | Carte d'avis avec avatar, note etoiles, texte, reponse salon |
| QueueStatusCard | Carte de statut file d'attente avec progression et temps estime |
| PaymentCard | Carte de methode de paiement |

### Composants de profil
| Composant | Description |
|-----------|------------|
| ProfileHeaderCard | Header de profil style Facebook (couverture + avatar + stats + bouton suivre) |
| ProfileStatsSection | Section de statistiques (posts, followers, following) |
| ProfileBadgesSection | Section de badges et certifications |

### Composants de liste
| Composant | Description |
|-----------|------------|
| ListEmptyState | Etat vide avec icone et message |
| ListErrorState | Etat erreur avec bouton retry |
| ListLoadingState | Skeleton/shimmer de chargement |
| ListSeparator | Separateur horizontal |
| PullToRefreshBox | Conteneur pull-to-refresh multiplateforme |

### Composants d'interaction
| Composant | Description |
|-----------|------------|
| RatingBar | Barre de notation 5 etoiles (interactive ou lecture seule) |
| ReactionsMenu | Menu flottant de reactions animees (6 types) |
| FilterChip | Chip de filtrage |
| PostOptionsMenu | Menu contextuel de post (editer, supprimer, signaler) |
| UnifiedSearchSuggestions | Dropdown de suggestions de recherche multi-type |
| FrollotToast | Notification toast animee (succes, erreur, info) |

### Composants de media
| Composant | Description |
|-----------|------------|
| FullScreenImageViewer | Galerie plein ecran avec zoom/pan/swipe |
| ImagePicker | Selection d'image (expect/actual par plateforme) |
| ImageUtils | Utilitaires de traitement d'image |

### Composants de dialog
| Composant | Description |
|-----------|------------|
| StandardDialog | Dialog generique |
| ReportDialog | Dialog de signalement de contenu |
| ExternalShareDialog | Dialog de partage externe |

### Composants de paiement
| Composant | Description |
|-----------|------------|
| CardInputField | Champ de saisie de carte bancaire |
| PaymentCard | Affichage de carte de paiement |

### Post card principal
| Composant | Description |
|-----------|------------|
| UltraPremiumPostCard | Carte de post sociale complete (~1800 lignes) avec toutes les interactions |

---

## 5. SYSTEME DE LOCALISATION

### Langues supportees
- **Francais (fr)** - langue par defaut
- **Anglais (en)**
- **Espagnol (es)**
- **Allemand (de)**
- **Arabe (ar)** - support RTL

### Structure
- 620+ cles de traduction organisees par ecran/composant
- Priorite : langue backend > preference locale > langue systeme > francais
- Tous les textes de l'interface passent par `Strings.get(key)`

### Le design doit prendre en compte
- Les textes allemands et francais sont souvent plus longs que l'anglais
- L'arabe necessite un layout miroir (RTL)
- Les formats de date, heure et monnaie varient selon la langue

---

## 6. CE QUI EST DEMANDE

### 6.1 Charte graphique complete

Tu dois concevoir un systeme de design complet et professionnel pour une application de beaute/coiffure haut de gamme. Cela inclut :

**Palette de couleurs** :
- Couleur primaire + variantes (container, onPrimary, onPrimaryContainer)
- Couleur secondaire + variantes
- Couleur tertiaire + variantes
- Couleurs d'etat : succes, erreur, warning, info (+ variantes container)
- Couleurs de surface : surface, surfaceVariant, surfaceContainer (low, default, high, highest)
- Couleurs d'arriere-plan
- Couleurs de contour : outline, outlineVariant
- Couleurs inversees : inverseSurface, inverseOnSurface, inversePrimary
- **Version light ET dark mode** complete
- Justification de chaque choix de couleur

**Typographie** :
- Choix de police(s) compatible(s) avec Compose Multiplatform (pas de police custom necessitant un import, utiliser les fonts systeme ou Google Fonts disponibles via Compose)
- Definition de chaque niveau typographique Material 3 : displayLarge/Medium/Small, headlineLarge/Medium/Small, titleLarge/Medium/Small, bodyLarge/Medium/Small, labelLarge/Medium/Small
- Poids, taille, interligne, espacement des lettres pour chaque niveau

**Espacement** :
- Systeme de spacing standardise (ex: 4dp, 8dp, 12dp, 16dp, 24dp, 32dp, 48dp)
- Regles d'utilisation : quand utiliser quel espacement

**Formes** :
- Coins arrondis standardises pour chaque type d'element
- Regles : extraSmall, small, medium, large, extraLarge

**Elevation** :
- Niveaux d'elevation pour chaque type de composant
- Regles d'utilisation

### 6.2 Design de chaque composant reutilisable

Pour chaque composant liste en section 4, tu dois fournir :

1. **Description** du composant et de son utilisation
2. **Variantes** (tailles, styles, etats)
3. **Etats** : default, hover/focus, pressed, disabled, loading, error, selected
4. **Specifications** : dimensions, paddings, couleurs (tokens du theme uniquement), typographie, icones
5. **Code Compose** d'implementation complet et fonctionnel
6. **Regles** : quand utiliser ce composant, quand ne pas l'utiliser

### 6.3 Design de chaque ecran (46 ecrans)

Pour **chacun** des 46 ecrans listes en section 3, tu dois fournir :

1. **Wireframe textuel detaille** : description precise du layout (disposition de chaque element, de haut en bas, de gauche a droite)
2. **Hierarchie visuelle** : qu'est-ce qui est le plus important visuellement, qu'est-ce qui est secondaire
3. **Composants utilises** : quels composants reutilisables du design system sont utilises et ou
4. **Couleurs** : quels tokens de couleur pour chaque zone/element
5. **Typographie** : quel niveau typographique pour chaque texte
6. **Espacement** : paddings et margins entre chaque section
7. **Interactions** : animations, transitions, etats dynamiques
8. **Responsive** : adaptation Web (large screen) vs Mobile
9. **Dark mode** : ajustements specifiques si necessaires
10. **Code Compose** d'implementation complet

### 6.4 Implementation des fichiers theme

Tu dois fournir le code complet et pret a copier-coller pour :
- `Color.kt` (toutes les couleurs light et dark)
- `Theme.kt` (typographie, formes, composable FrollotTheme)
- Tout fichier additionnel necessaire au design system (ex: Spacing.kt, Elevation.kt, etc.)

---

## 7. CONTRAINTES IMPERATIVES

1. **Material Design 3** : tout doit utiliser les API Material 3 de Jetpack Compose
2. **Tokens du theme uniquement** : aucune couleur, taille de police ou dimension hardcodee — tout doit passer par `MaterialTheme.colorScheme`, `MaterialTheme.typography`, `MaterialTheme.shapes`
3. **Compose Multiplatform** : le code doit fonctionner sur Android ET Web (WasmJs) — pas d'API specifique Android dans le code commun
4. **Material Icons Extended** : toutes les icones doivent provenir de `compose.materialIconsExtended` (deja en dependance)
5. **Pas de bibliotheque UI externe** : aucune dependance supplementaire (pas de Accompanist, pas de lib tierce)
6. **Support RTL** : tous les layouts doivent fonctionner en RTL (arabe)
7. **Support dark mode** : chaque ecran et composant doit etre beau en light ET dark
8. **Accessibilite** : contrastes WCAG AA minimum (4.5:1 pour texte, 3:1 pour elements graphiques), touch targets 48dp minimum, contentDescription sur les icones
9. **Responsive** : le design doit s'adapter aux ecrans mobiles (360dp-412dp) et aux ecrans web (>768dp)
10. **Performance** : eviter les recompositions inutiles, utiliser `remember` et `derivedStateOf` quand necessaire
11. **Localisation** : tous les textes visibles de l'interface doivent utiliser le systeme de localisation existant (pas de texte hardcode)

---

## 8. FORMAT DE LIVRAISON

Organise ta reponse en sections claires :

1. **Charte graphique** : palette, typographie, espacement, formes, elevation
2. **Fichiers theme** : Color.kt, Theme.kt, fichiers additionnels
3. **Composants** : chaque composant avec specs + code
4. **Ecrans** : chaque ecran avec wireframe + specs + code
5. **Guide d'utilisation** : regles et conventions pour maintenir la coherence

---

## 9. REGLE ABSOLUE

**Si tu as la moindre question ou le moindre doute sur un aspect du projet, la logique metier, les priorites, les preferences de design, ou quoi que ce soit d'autre, tu DOIS obligatoirement me les poser en francais avant de produire quoi que ce soit. Ne fais aucune supposition. Demande d'abord, produis ensuite.**
