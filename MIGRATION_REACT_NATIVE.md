# Plan de Migration : Kotlin Multiplatform Compose -> React Native

## Document de reference - Zero Regression

---

## TABLE DES MATIERES

1. [Principes directeurs](#1-principes-directeurs)
2. [Architecture cible](#2-architecture-cible)
3. [Pre-requis avant toute migration](#3-pre-requis-avant-toute-migration)
4. [Phases de migration](#4-phases-de-migration)
5. [Matrice de couverture ecran par ecran](#5-matrice-de-couverture)
6. [Contrat API - Reference](#6-contrat-api)
7. [Strategie de test](#7-strategie-de-test)
8. [Risques et mitigations](#8-risques-et-mitigations)
9. [Criteres de go/no-go](#9-criteres-de-gono-go)
10. [Rollback strategy](#10-rollback-strategy)

---

## 1. PRINCIPES DIRECTEURS

### Regles absolues

1. **Le backend ne change PAS** - Aucune modification de controller, DTO, model, migration
2. **Parite fonctionnelle** - Chaque ecran migre DOIT reproduire 100% des fonctionnalites existantes
3. **Tests avant/apres** - Chaque endpoint a un test d'integration qui passe AVANT et APRES migration
4. **Migration par ecran atomique** - Un ecran est soit 100% migre + teste, soit pas du tout
5. **Design v2 pour les 5 ecrans maquettes** - Les 44 autres ecrans suivent les tokens/composants v2
6. **Deux apps en parallele** - L'ancienne app KMP reste fonctionnelle tant que la nouvelle n'est pas 100% validee

### Ce qui change

| Avant (KMP) | Apres (React Native) |
|---|---|
| Kotlin + Compose | TypeScript + React Native |
| Ktor HTTP Client | Axios + React Query |
| kotlinx.serialization | TypeScript interfaces + Zod validation |
| Compose Navigation | Expo Router (file-based) |
| DataStore / localStorage | expo-secure-store (tokens) + MMKV (prefs) |
| Compose Material 3 | NativeWind (Tailwind) + composants custom |
| Expect/Actual platform | Platform.OS + fichiers .ios.ts/.android.ts |
| Coroutines + Mutex | React Query mutations + async/await |
| Coil | expo-image |

### Ce qui ne change PAS

- Backend Spring Boot (port 9090)
- MySQL + Flyway (V001-V039)
- API REST (120+ endpoints, memes paths, memes DTOs)
- JWT access (2h) + refresh (7d) avec rotation
- Stripe Checkout (redirect flow)
- SMTP email (verification, password reset)
- Upload multipart (max 10MB)
- 5 langues (EN, FR, ES, DE, AR)

---

## 2. ARCHITECTURE CIBLE

### Stack technique

```
React Native 0.76+ (New Architecture)
Expo SDK 52+
TypeScript 5.x (strict mode)
Expo Router v4 (file-based navigation)
React Query v5 (TanStack Query) - cache + sync
Axios - HTTP client avec interceptors
Zustand - state management global (auth, user, prefs)
NativeWind v4 (Tailwind CSS) - styling
expo-secure-store - stockage tokens (chiffre)
react-native-mmkv - preferences rapides
expo-image - chargement images
expo-image-picker - selection images
react-hook-form + zod - formulaires + validation
i18next + react-i18next - localisation 5 langues
@stripe/stripe-react-native - paiements
expo-notifications - push FCM
react-native-reanimated - animations
```

### Structure du projet

```
frollot-app/
|-- app/                              # Expo Router - navigation par fichiers
|   |-- _layout.tsx                   # Root layout (providers, fonts, splash)
|   |-- (auth)/                       # Groupe auth (non-authentifie)
|   |   |-- _layout.tsx               # Stack layout auth
|   |   |-- login.tsx                 # 01 - LoginScreen
|   |   |-- register.tsx              # 02 - RegisterScreen
|   |   |-- verify-email.tsx          # 03 - EmailVerificationScreen
|   |   |-- forgot-password.tsx       # 04 - ForgotPasswordScreen
|   |   |-- reset-password.tsx        # 05 - ResetPasswordScreen
|   |-- (tabs)/                       # Groupe principal (authentifie)
|   |   |-- _layout.tsx               # Bottom tabs (5 onglets design v2)
|   |   |-- index.tsx                 # Tab 1 - HomeScreen (Accueil)
|   |   |-- feed.tsx                  # Tab 2 - SocialFeedScreen (Social)
|   |   |-- search.tsx                # Tab 3 - SearchScreen (Explorer)
|   |   |-- bookings.tsx              # Tab 4 - MyBookingsScreen (RDV)
|   |   |-- profile.tsx               # Tab 5 - ProfileScreen (Profil)
|   |-- salon/
|   |   |-- [id].tsx                  # SalonDetailScreen
|   |   |-- create.tsx                # CreateSalonScreen
|   |   |-- [id]/
|   |       |-- services/create.tsx   # CreateServiceScreen
|   |       |-- staff/create.tsx      # CreateStaffScreen
|   |       |-- posts.tsx             # SalonPostsScreen
|   |       |-- queue.tsx             # QueueManagementScreen
|   |       |-- bookings.tsx          # OwnerBookingsManagementScreen
|   |-- booking/
|   |   |-- [salonId]/[serviceId].tsx # BookingScreen
|   |   |-- detail/[id].tsx           # BookingDetailScreen
|   |   |-- payment/[id].tsx          # PaymentFlowScreen
|   |-- post/
|   |   |-- create.tsx                # CreatePostScreen
|   |   |-- [id].tsx                  # PostDetailScreen
|   |   |-- [id]/comments.tsx         # CommentsScreen
|   |-- review/
|   |   |-- create.tsx                # CreateReviewScreen
|   |-- profile/
|   |   |-- client/[id].tsx           # ClientProfileScreen
|   |   |-- coiffeur/[id].tsx         # CoiffeurProfileScreen
|   |   |-- owner/[id].tsx            # SalonOwnerProfileScreen
|   |   |-- salon/[id].tsx            # SalonSocialProfileScreen
|   |-- portfolio/
|   |   |-- create.tsx                # CreatePortfolioScreen
|   |   |-- [id].tsx                  # PortfolioDetailScreen
|   |   |-- list/[ownerId].tsx        # PortfoliosListScreen
|   |-- collection/
|   |   |-- [userId].tsx              # CollectionsScreen
|   |   |-- detail/[id].tsx           # CollectionDetailScreen
|   |-- favorites/[userId].tsx        # FavoritesScreen
|   |-- archives/[userId].tsx         # ArchivesScreen
|   |-- trending.tsx                  # TrendingScreen
|   |-- report.tsx                    # ReportScreen
|   |-- settings/
|   |   |-- index.tsx                 # SettingsScreen
|   |   |-- security.tsx              # SecuritySettingsScreen
|   |   |-- change-email.tsx          # ChangeEmailScreen
|   |   |-- change-phone.tsx          # ChangePhoneScreen
|   |   |-- blocked-users.tsx         # BlockedUsersScreen
|   |   |-- payment-methods.tsx       # PaymentMethodsScreen
|   |   |-- help.tsx                  # HelpCenterScreen
|   |   |-- contact.tsx               # ContactSupportScreen
|   |   |-- terms.tsx                 # TermsOfServiceScreen
|   |   |-- privacy.tsx               # PrivacyPolicyScreen
|   |   |-- verification.tsx          # RequestVerificationScreen
|   |-- payment-history.tsx           # PaymentHistoryScreen
|
|-- src/
|   |-- api/                          # Couche API (miroir exact du backend)
|   |   |-- client.ts                 # Axios instance + interceptors refresh
|   |   |-- auth.ts                   # 10 endpoints auth
|   |   |-- users.ts                  # 8 endpoints user
|   |   |-- security.ts               # 7 endpoints security/sessions
|   |   |-- salons.ts                 # 7 endpoints salons
|   |   |-- services.ts               # 9 endpoints services
|   |   |-- staff.ts                  # 6 endpoints staff
|   |   |-- bookings.ts               # 12 endpoints bookings
|   |   |-- queue.ts                  # 5 endpoints queue
|   |   |-- social.ts                 # 35+ endpoints social
|   |   |-- reviews.ts                # 7 endpoints reviews
|   |   |-- payments.ts               # 8 endpoints payments
|   |   |-- portfolios.ts             # 9 endpoints portfolios
|   |   |-- collections.ts            # 9 endpoints collections
|   |   |-- media.ts                  # 1 endpoint upload
|   |   |-- moderation.ts             # 10 endpoints moderation
|   |   |-- verification.ts           # 4 endpoints verification
|   |   |-- search.ts                 # 3 endpoints search
|   |
|   |-- types/                        # Types TypeScript (miroir exact des DTOs backend)
|   |   |-- auth.ts
|   |   |-- user.ts
|   |   |-- salon.ts
|   |   |-- booking.ts
|   |   |-- payment.ts
|   |   |-- queue.ts
|   |   |-- social.ts
|   |   |-- review.ts
|   |   |-- portfolio.ts
|   |   |-- collection.ts
|   |   |-- moderation.ts
|   |   |-- search.ts
|   |   |-- common.ts                 # PageResponse<T>, enums partages
|   |
|   |-- stores/                       # Zustand (state global minimal)
|   |   |-- auth.store.ts             # tokens, user, isAuthenticated, login/logout
|   |   |-- preferences.store.ts      # darkMode, language
|   |
|   |-- hooks/                        # React Query hooks (1 hook = 1 endpoint)
|   |   |-- use-auth.ts
|   |   |-- use-salons.ts
|   |   |-- use-bookings.ts
|   |   |-- use-social.ts
|   |   |-- use-queue.ts
|   |   |-- use-payments.ts
|   |   |-- ...
|   |
|   |-- components/                   # Composants reutilisables
|   |   |-- ui/                       # Atomes (Button, Input, Card, Chip, Badge, Dialog)
|   |   |-- salon/                    # SalonCard, ServiceItem, StaffCard
|   |   |-- social/                   # PostCard, CommentItem, ReactionBar, BeforeAfter
|   |   |-- booking/                  # BookingStepper, TimeSlotGrid, DaySelector
|   |   |-- profile/                  # ProfileHeader, StatsSection, BadgesSection
|   |   |-- common/                   # Avatar, Logo, Toast, SearchBar, BottomNav
|   |   |-- layout/                   # SafeArea, ScrollContainer, Header
|   |
|   |-- theme/                        # Design system v2 (tokens.css -> TS)
|   |   |-- colors.ts                 # Light + Dark, toutes les couleurs
|   |   |-- typography.ts             # Cormorant Garamond + Manrope
|   |   |-- spacing.ts                # Grille 4dp
|   |   |-- radius.ts                 # xs(4) sm(8) md(12) lg(16) xl(28) full(999)
|   |   |-- elevation.ts              # 6 niveaux d'ombre
|   |   |-- index.ts                  # Export unifie
|   |
|   |-- i18n/                         # Localisation 5 langues
|   |   |-- index.ts                  # Config i18next
|   |   |-- en.json                   # Extrait de strings_en.kt
|   |   |-- fr.json                   # Extrait de strings_fr.kt
|   |   |-- es.json                   # Extrait de strings_es.kt
|   |   |-- de.json                   # Extrait de strings_de.kt
|   |   |-- ar.json                   # Extrait de strings_ar.kt
|   |
|   |-- utils/                        # Utilitaires
|       |-- format.ts                 # Formatage dates, prix, durees
|       |-- validation.ts             # Schemas Zod
|       |-- image.ts                  # Compression, redimensionnement
|       |-- platform.ts               # Detection plateforme
```

---

## 3. PRE-REQUIS AVANT TOUTE MIGRATION

### Phase 0 : Fondation de test (OBLIGATOIRE - avant d'ecrire une ligne de React Native)

#### 0.1 - Tests d'integration API (Postman/Newman)

Creer une collection Postman exhaustive couvrant les 120+ endpoints.
Cette collection sert de **contrat immuable** : elle doit passer a 100% avant ET apres migration.

```
Collection structure :
  /Auth
    POST /api/users/register         -> 201 + AuthResponse
    POST /api/users/login            -> 200 + AuthResponse (tokens)
    POST /api/users/refresh           -> 200 + new tokens
    POST /api/users/complete-registration -> 200
    POST /api/users/verify-email      -> 200
    POST /api/users/forgot-password   -> 200
    POST /api/users/reset-password    -> 200
  /Users
    GET  /api/users/me               -> 200 + User (requires Bearer)
    GET  /api/users/search?query=    -> 200 + List<User>
    PATCH /api/users/{id}/avatar     -> 200 + User
    GET  /api/users/me/language      -> 200
    PUT  /api/users/me/language      -> 200
  /Security
    PUT  /api/users/me/password      -> 200
    GET  /api/users/me/sessions      -> 200 + sessions[]
    DELETE /api/users/me/sessions/{id} -> 200
  /Salons
    POST /api/salons                 -> 201 + Salon
    GET  /api/salons?q=&city=        -> 200 + PageResponse<Salon>
    GET  /api/salons/{id}            -> 200 + Salon
    GET  /api/salons/owner/{ownerId} -> 200 + List<Salon>
  /Services
    POST /api/salons/{id}/services   -> 201
    GET  /api/salons/{id}/services   -> 200 + List<SalonService>
    ...
  /Staff, /Bookings, /Queue, /Social, /Reviews, /Payments,
  /Portfolios, /Collections, /Media, /Moderation, /Verification
```

**Critere** : 100% des endpoints repondent avec les status codes et structures attendus.

#### 0.2 - Tests E2E des parcours critiques (Detox ou Maestro)

Ecrire 12 scenarios E2E qui couvrent les parcours utilisateurs critiques :

| # | Scenario | Ecrans traverses | Assertions |
|---|----------|------------------|------------|
| E01 | Inscription complete | Register -> EmailVerification -> Login | Compte cree, email envoye, connexion OK |
| E02 | Connexion + deconnexion | Login -> Home -> Logout -> Login | Tokens stockes, session restauree, tokens effaces |
| E03 | Restauration de session | Kill app -> Reopen | Auto-login sans ecran de connexion |
| E04 | Refresh token automatique | Login -> attendre expiration -> action | Refresh transparent, pas de 401 visible |
| E05 | Recherche et detail salon | Home -> Search -> SalonDetail | Salons affiches, detail complet |
| E06 | Reservation complete | SalonDetail -> Booking -> Payment | Booking cree, statut PENDING |
| E07 | Gestion file d'attente | SalonDetail -> QueueManagement -> Join | Position affichee, polling 15s |
| E08 | Creation de post | SocialFeed -> CreatePost -> Feed | Post visible dans le feed |
| E09 | Interaction sociale | Feed -> Like + Comment + Share | Compteurs mis a jour |
| E10 | Upload d'image | CreatePost -> pick image -> upload | Image uploadee, URL retournee |
| E11 | Changement de langue | Settings -> langue FR -> ES | Toute l'UI traduite en espagnol |
| E12 | Flow mot de passe oublie | Login -> ForgotPassword -> ResetPassword | Email envoye, mot de passe change |

**Critere** : Les 12 scenarios passent sur l'app KMP actuelle (baseline).

#### 0.3 - Screenshot testing de reference (optionnel mais recommande)

Capturer des screenshots de chaque ecran de l'app KMP actuelle pour comparaison visuelle post-migration.

---

## 4. PHASES DE MIGRATION

### Vue d'ensemble

```
Phase 0  : Tests de reference (contrat API + E2E baseline)     ~3j
Phase 1  : Scaffolding projet React Native                      ~2j
Phase 2  : Infrastructure (API client, auth, theme, i18n)       ~5j
Phase 3  : Auth screens (5 ecrans)                               ~4j
Phase 4  : Core screens (8 ecrans - Home, Salon, Booking)       ~6j
Phase 5  : Social screens (8 ecrans - Feed, Post, Comments)     ~5j
Phase 6  : Profile screens (6 ecrans)                            ~4j
Phase 7  : Management screens (6 ecrans - Queue, Staff, Owner)  ~4j
Phase 8  : Advanced screens (10 ecrans - Portfolio, Collection)  ~4j
Phase 9  : Settings & Misc (12 ecrans)                           ~3j
Phase 10 : Tests E2E + QA + Polish                               ~5j
Phase 11 : Beta + Migration utilisateurs                         ~3j
```

---

### Phase 1 : Scaffolding projet (2 jours)

**Objectif** : Projet Expo initialise, buildable sur Android/iOS, toutes les deps installees.

```bash
npx create-expo-app@latest frollot-app --template tabs
cd frollot-app
```

**Taches** :

| # | Tache | Validation |
|---|-------|-----------|
| 1.1 | Init Expo avec TypeScript strict | `npx expo start` demarre sans erreur |
| 1.2 | Installer toutes les deps (voir stack) | `npm install` sans erreur |
| 1.3 | Configurer NativeWind + tailwind.config.ts | Composant avec className rend correctement |
| 1.4 | Configurer Expo Router file-based | Navigation entre 2 ecrans fonctionne |
| 1.5 | Configurer les custom fonts (Cormorant Garamond + Manrope) | Fonts chargees au splash |
| 1.6 | Creer app/_layout.tsx racine (providers) | App demarre avec tous les providers |
| 1.7 | Configurer ESLint + Prettier + Husky pre-commit | Lint passe |
| 1.8 | Configurer app.json (nom, icone, splash, scheme deep link) | Build preview fonctionne |

**Livrable** : App vide qui demarre sur Android + iOS avec fonts + navigation.

---

### Phase 2 : Infrastructure (5 jours)

**Objectif** : Toute la plomberie invisible est en place et testee unitairement.

#### 2.1 - Client API Axios + Auto-refresh (2 jours)

**Fichier** : `src/api/client.ts`

Reproduction EXACTE du comportement de FrollotApi.kt :

```typescript
// Specifications critiques a respecter :
// 1. Timeout: request=60s, connect=15s
// 2. JSON config: ignoreUnknownKeys (Axios fait ca nativement)
// 3. Header Authorization: Bearer <token> sur chaque requete
// 4. Content-Type: application/json par defaut
// 5. Interceptor 401 -> refresh automatique avec mutex
// 6. Refresh utilise une instance Axios SEPAREE (pas d'interceptor)
// 7. Token rotation: nouveau access + refresh apres refresh
// 8. Stockage: expo-secure-store pour les tokens
// 9. Erreurs: 401->refresh, 403->throw, 400->parse body, 413->fichier trop gros
```

**Tests unitaires requis** :
- [ ] Requete avec token ajoute le header Authorization
- [ ] 401 declenche un refresh automatique
- [ ] Refresh concurrent : une seule requete de refresh envoyee
- [ ] Apres refresh, la requete originale est rejouee
- [ ] Double 401 (refresh echoue) -> AuthenticationException
- [ ] 403 -> erreur Forbidden
- [ ] 400 -> message d'erreur du body
- [ ] Timeout respecte (60s)

#### 2.2 - Auth Store Zustand (0.5 jour)

**Fichier** : `src/stores/auth.store.ts`

```typescript
interface AuthStore {
  // State
  accessToken: string | null;
  refreshToken: string | null;
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;

  // Actions
  setTokens: (access: string, refresh: string) => Promise<void>;
  clearTokens: () => Promise<void>;
  setUser: (user: User) => void;
  initializeFromStorage: () => Promise<boolean>;
}
```

**Tests** :
- [ ] setTokens persiste dans expo-secure-store
- [ ] clearTokens supprime de expo-secure-store
- [ ] initializeFromStorage restaure les tokens
- [ ] isAuthenticated = true quand tokens presents

#### 2.3 - Types TypeScript (1 jour)

**Miroir exact des DTOs Kotlin**. Chaque champ, chaque enum, chaque valeur par defaut.

Fichiers a creer (13 fichiers de types) :

| Fichier | Models Kotlin source | Enums inclus |
|---------|---------------------|--------------|
| auth.ts | AuthResponse, LoginRequest, RegisterRequest, RefreshTokenRequest, ForgotPasswordRequest/Response, ResetPasswordRequest/Response | - |
| user.ts | User, UserResponse | UserType (client, hairstylist, salon_owner, admin) |
| salon.ts | Salon, CreateSalonRequest | - |
| booking.ts | BookingResponse, CreateBookingRequest, UpdateBookingStatusRequest, AvailableSlotsRequest/Response, TimeSlot, BookingStatistics, BookingSummary | BookingStatus (6 valeurs) |
| payment.ts | PaymentIntentResponse, PaymentResponse, CheckoutSessionRequest/Response, PaymentSessionStatus, RefundRequest | PaymentStatus (8 valeurs) |
| queue.ts | QueueStatusResponse, QueueEntryResponse, JoinQueueRequest, LeaveQueueRequest | QueueEntryStatus (4 valeurs) |
| social.ts | PostResponse, CreatePostRequest, CommentResponse, CreateCommentRequest, PostShareResponse, TagResponse, FollowResponse, HairHashtagResponse | PostType (7), PostVisibility (3), ReactionType (6), FollowingType (3), HairHashtagCategory (5) |
| review.ts | Review, CreateReviewRequest, SalonReviewStats | - |
| staff.ts | StaffMember, CreateStaffRequest, StaffStatistics | - |
| service.ts | SalonService, CreateServiceRequest, UpdateServiceRequest | ServiceCategory (7 valeurs) |
| portfolio.ts | PortfolioResponse, CreatePortfolioRequest, PortfolioPostResponse | PortfolioOwnerType (2) |
| collection.ts | CollectionResponse, CreateCollectionRequest, CollectionPostResponse | CollectionCategory (4) |
| moderation.ts | ReportResponse, CreateReportRequest, ModerationActionResponse | ReportedEntityType (4), ReportReason (5), ReportStatus (4), ModerationActionType (3), AppealStatus (4) |
| search.ts | SearchResponse, SearchFilters | SearchType (5), SortBy (2), TrendPeriod (3) |
| common.ts | PageResponse<T>, NetworkState | VerificationType (4) |
| security.ts | SessionInfo, SessionsListResponse, ChangePassword/Email/Phone Request/Response, DeleteAccountRequest/Response, RevokeSessionResponse | - |
| profile.ts | CoiffeurProfileResponse, ClientProfileResponse, SalonOwnerProfileResponse, SalonSocialProfileResponse + Statistics + Update requests | - |

**Regle critique** : Chaque @SerialName dans le Kotlin doit correspondre exactement au nom de propriete TypeScript.

#### 2.4 - Theme / Design System v2 (0.5 jour)

**Transcription directe de tokens.css vers TypeScript** :

```typescript
// src/theme/colors.ts
export const colors = {
  light: {
    primary: '#6B4E78',
    onPrimary: '#FFFFFF',
    primaryContainer: '#EEE2F1',
    onPrimaryContainer: '#281733',
    secondary: '#A4677F',
    // ... (50+ tokens)
  },
  dark: {
    primary: '#D9BBE2',
    onPrimary: '#3C2A48',
    // ... (50+ tokens)
  }
} as const;
```

**Tests** : Snapshot test du theme light et dark.

#### 2.5 - Localisation i18n (1 jour)

**Extraction des 5 fichiers Kotlin -> 5 fichiers JSON** :

```
strings_en.kt -> en.json
strings_fr.kt -> fr.json
strings_es.kt -> es.json
strings_de.kt -> de.json
strings_ar.kt -> ar.json
```

**Structure JSON** : Namespace par ecran/composant (meme hierarchie que Strings.kt).

**Tests** :
- [ ] Toutes les cles presentes dans les 5 langues
- [ ] Aucune cle manquante (script de verification)
- [ ] Changement de langue dynamique fonctionne
- [ ] RTL pour l'arabe (I18nManager.forceRTL)

**Critere de validation Phase 2** :
- Client API peut faire login + refresh + CRUD
- Types compilent sans erreur
- Theme affiche les bonnes couleurs
- i18n affiche les bonnes traductions
- 100% des tests unitaires passent

---

### Phase 3 : Auth screens (4 jours)

5 ecrans + design v2 pour LoginScreen.

| # | Ecran | Maquette v2 | Endpoints utilises | Complexite |
|---|-------|-------------|-------------------|------------|
| 3.1 | LoginScreen | OUI (screen-login.jsx) | POST /login | Moyenne |
| 3.2 | RegisterScreen | Non (suivre tokens v2) | POST /register | Moyenne |
| 3.3 | EmailVerificationScreen | Non | POST /complete-registration, POST /resend-verification | Haute |
| 3.4 | ForgotPasswordScreen | Non | POST /forgot-password | Basse |
| 3.5 | ResetPasswordScreen | Non | POST /reset-password | Basse |

**LoginScreen - Specifications design v2** :
- Hero photo plein cadre (320px height)
- Gradient overlay: rgba(40,23,51,.42) -> transparent -> surface
- Logo "F" en verre depoli (blur 6px, border rgba(255,255,255,.35))
- Overline "Bon retour parmi nous" en secondary
- Titre "Bienvenue" en Cormorant Garamond 44px
- Champs: icone + label au-dessus + border focused primary
- Bouton "Se connecter" primary avec elevation 2
- Separateur "OU"
- Bouton "Creer un compte" outline
- Footer CGU

**Tests E2E Phase 3** :
- [ ] E01 : Inscription complete (register -> verify -> login)
- [ ] E02 : Connexion + deconnexion
- [ ] E12 : Mot de passe oublie

---

### Phase 4 : Core screens (6 jours)

8 ecrans dont 3 avec maquettes v2.

| # | Ecran | Maquette v2 | Endpoints | Complexite |
|---|-------|-------------|-----------|------------|
| 4.1 | HomeScreen | OUI (screen-home.jsx) | GET /salons, GET /salons/trending, GET /queue | Haute |
| 4.2 | SearchScreen | Non | GET /social/search, GET /salons?q= | Moyenne |
| 4.3 | SalonDetailScreen | OUI (screen-salon.jsx) | GET /salons/{id}, GET /services, GET /staff, GET /reviews | Tres haute |
| 4.4 | BookingScreen | OUI (screen-booking.jsx) | POST /available-slots, POST /bookings | Tres haute |
| 4.5 | BookingDetailScreen | Non | GET /bookings/{id}, PATCH /status | Moyenne |
| 4.6 | MyBookingsScreen | Non | GET /clients/{id}/bookings | Moyenne |
| 4.7 | PaymentFlowScreen | Non | POST /create-checkout-session, GET /status | Haute |
| 4.8 | PaymentHistoryScreen | Non | GET /payments/client/{id} | Basse |

**HomeScreen - Specifications design v2** :
- Header: hamburger + "Frollot" Cormorant 24px + notif + Avatar avec ring gradient
- Barre recherche pill: radius full, surface-container-high, icone search
- Salutation: overline date + headline Cormorant 30px "Bonjour {prenom}"
- Categories horizontales: icones rondes 58px, premiere selectionnee (primary bg)
- Section "Salons recents": carrousel horizontal, cards 220px avec cover + note + distance
- Bandeau file d'attente: gradient primary, CTA "Rejoindre la file"
- BottomNav 5 onglets: Accueil, Social, Explorer, RDV, Profil

**SalonDetailScreen - Specifications design v2** :
- Cover plein cadre 230px + gradient overlay
- Sheet arrondie (borderRadius 24px top) qui chevauche la cover de -22px
- Nom Cormorant 30px + adresse + badge note (tertiary-container)
- Bandeau file: success-container, dot anime, "~15 min d'attente"
- Onglets: Services | Equipe | Avis | Posts | Info
- Liste services: overline categorie, titre, duree + prix, bouton "Reserver"
- Barre flottante bas: "Suivre" outline + "Reserver une prestation" primary

**BookingScreen - Specifications design v2** :
- Stepper 4 etapes: Service -> Date -> Coiffeur -> Recap
- Resume service selectionne (primary-container)
- Selecteur mois avec fleches
- Jours horizontaux: selectionne=primary, ferme=dashed border+opacity .5
- Grille creneaux 3 colonnes: selectionne=primary
- Recap bas fixe: date + heure + prix + bouton "Continuer"

**Tests E2E Phase 4** :
- [ ] E03 : Restauration de session (kill app -> reopen)
- [ ] E05 : Recherche et detail salon
- [ ] E06 : Reservation complete

---

### Phase 5 : Social screens (5 jours)

8 ecrans dont 1 avec maquette v2.

| # | Ecran | Maquette v2 | Endpoints | Complexite |
|---|-------|-------------|-----------|------------|
| 5.1 | SocialFeedScreen | OUI (screen-feed.jsx) | GET /feed, GET /feed/following | Tres haute |
| 5.2 | CreatePostScreen | Non | POST /posts, POST /media/upload | Haute |
| 5.3 | PostDetailScreen | Non | GET /posts/{id}, reactions, comments | Haute |
| 5.4 | CommentsScreen | Non | GET /comments, POST /comments | Moyenne |
| 5.5 | TrendingScreen | Non | GET /posts/trending, GET /hashtags/trending | Moyenne |
| 5.6 | SalonPostsScreen | Non | GET /salons/{id}/posts | Basse |
| 5.7 | CreateReviewScreen | Non | POST /reviews | Moyenne |
| 5.8 | ReportScreen | Non | POST /reports | Basse |

**SocialFeedScreen - Specifications design v2** :
- Header: hamburger + "Fil social" + search + add_circle primary
- Onglets: Tous | Suivis | Tendances (underline primary 3px)
- PostCard:
  - Avatar 44px avec ring + nom bold + verified badge + type badge (secondary-container)
  - Texte body-md + hashtags en primary bold
  - Media: photo OU avant/apres cote a cote (separateur swap_horiz)
  - Barre engagement: coeur(secondary) + comment + share + bookmark
- Pull-to-refresh

**Tests E2E Phase 5** :
- [ ] E08 : Creation de post
- [ ] E09 : Interaction sociale (like, comment, share)
- [ ] E10 : Upload d'image

---

### Phase 6 : Profile screens (4 jours)

6 ecrans.

| # | Ecran | Endpoints | Complexite |
|---|-------|-----------|------------|
| 6.1 | ProfileScreen | GET /users/me, PUT /profile | Haute |
| 6.2 | ClientProfileScreen | GET /clients/{id}/profile | Moyenne |
| 6.3 | CoiffeurProfileScreen | GET /coiffeurs/{id}/profile | Haute |
| 6.4 | SalonOwnerProfileScreen | GET /owners/{id}/profile | Haute |
| 6.5 | SalonSocialProfileScreen | GET /salons/{id}/profile | Haute |
| 6.6 | RequestVerificationScreen | POST /verification/request | Basse |

---

### Phase 7 : Management screens (4 jours)

6 ecrans.

| # | Ecran | Endpoints | Complexite |
|---|-------|-----------|------------|
| 7.1 | CreateSalonScreen | POST /salons | Moyenne |
| 7.2 | CreateServiceScreen | POST /services | Moyenne |
| 7.3 | CreateStaffScreen | POST /staff | Moyenne |
| 7.4 | QueueManagementScreen | GET/POST /queue (polling 15s) | Haute |
| 7.5 | OwnerBookingsManagementScreen | GET /salons/{id}/bookings | Moyenne |
| 7.6 | CreateReviewScreen | POST /reviews | Basse |

**QueueManagementScreen - Point critique** :
- Polling toutes les 15 secondes (useQuery refetchInterval: 15000)
- Optimistic updates sur join/leave/call-next
- Mutex conceptuel (React Query mutation locking)

---

### Phase 8 : Advanced screens (4 jours)

10 ecrans.

| # | Ecran | Complexite |
|---|-------|------------|
| 8.1 | CreatePortfolioScreen | Moyenne |
| 8.2 | PortfolioDetailScreen | Moyenne |
| 8.3 | PortfoliosListScreen | Basse |
| 8.4 | CollectionsScreen | Moyenne |
| 8.5 | CollectionDetailScreen | Moyenne |
| 8.6 | FavoritesScreen | Basse |
| 8.7 | ArchivesScreen | Basse |
| 8.8 | ReportScreen | Basse |
| 8.9 | (EditPortfolio reuse CreatePortfolio) | - |
| 8.10 | (Reorder portfolio posts) | Moyenne |

---

### Phase 9 : Settings & Misc (3 jours)

12 ecrans (dont 6 placeholders simples).

| # | Ecran | Complexite |
|---|-------|------------|
| 9.1 | SettingsScreen | Moyenne |
| 9.2 | SecuritySettingsScreen | Haute |
| 9.3 | ChangeEmailScreen | Moyenne |
| 9.4 | ChangePhoneScreen | Moyenne |
| 9.5 | PaymentMethodsScreen | Placeholder |
| 9.6 | BlockedUsersScreen | Placeholder |
| 9.7 | HelpCenterScreen | Placeholder |
| 9.8 | ContactSupportScreen | Placeholder |
| 9.9 | TermsOfServiceScreen | Placeholder |
| 9.10 | PrivacyPolicyScreen | Placeholder |
| 9.11 | PaymentHistoryScreen | Basse |
| 9.12 | RequestVerificationScreen | Basse |

---

### Phase 10 : Tests E2E + QA + Polish (5 jours)

| Jour | Activite |
|------|----------|
| J1 | Executer les 12 scenarios E2E sur la nouvelle app |
| J2 | Comparer avec la baseline KMP - identifier les regressions |
| J3 | Corriger toutes les regressions identifiees |
| J4 | Tests de performance (temps de chargement, memoire, FPS) |
| J5 | Tests RTL (arabe), tests dark mode, tests offline graceful |

**Checklist zero regression** :
- [ ] 12/12 scenarios E2E passent
- [ ] 120/120 endpoints API repondent correctement
- [ ] 49/49 ecrans renders sans crash
- [ ] 5/5 langues affichent toutes les chaines
- [ ] Dark mode fonctionne sur tous les ecrans
- [ ] Pull-to-refresh fonctionne partout ou il existait
- [ ] Pagination fonctionne (scroll infini)
- [ ] Upload d'image fonctionne (camera + galerie)
- [ ] Deep links fonctionnent (payment return)
- [ ] Push notifications fonctionnent (FCM)
- [ ] Token refresh automatique fonctionne
- [ ] Session restauree apres kill app

---

### Phase 11 : Beta + Migration (3 jours)

| Jour | Activite |
|------|----------|
| J1 | Build de production Android (APK/AAB) + iOS (IPA) |
| J2 | Distribution beta (TestFlight + Google Play Internal Testing) |
| J3 | Feedback beta -> corrections -> release |

---

## 5. MATRICE DE COUVERTURE

### 49 ecrans - Mapping complet KMP -> React Native

| # | Ecran KMP | Route React Native | Maquette v2 | Phase | Priorite |
|---|-----------|-------------------|-------------|-------|----------|
| 01 | LoginScreen | (auth)/login | OUI | 3 | P0 |
| 02 | RegisterScreen | (auth)/register | Non | 3 | P0 |
| 03 | EmailVerificationScreen | (auth)/verify-email | Non | 3 | P0 |
| 04 | ForgotPasswordScreen | (auth)/forgot-password | Non | 3 | P0 |
| 05 | ResetPasswordScreen | (auth)/reset-password | Non | 3 | P0 |
| 06 | HomeScreen | (tabs)/index | OUI | 4 | P0 |
| 07 | SearchScreen | (tabs)/search | Non | 4 | P0 |
| 08 | SalonDetailScreen | salon/[id] | OUI | 4 | P0 |
| 09 | BookingScreen | booking/[salonId]/[serviceId] | OUI | 4 | P0 |
| 10 | BookingDetailScreen | booking/detail/[id] | Non | 4 | P0 |
| 11 | MyBookingsScreen | (tabs)/bookings | Non | 4 | P0 |
| 12 | PaymentFlowScreen | booking/payment/[id] | Non | 4 | P0 |
| 13 | PaymentHistoryScreen | payment-history | Non | 9 | P2 |
| 14 | SocialFeedScreen | (tabs)/feed | OUI | 5 | P0 |
| 15 | CreatePostScreen | post/create | Non | 5 | P1 |
| 16 | PostDetailScreen | post/[id] | Non | 5 | P1 |
| 17 | CommentsScreen | post/[id]/comments | Non | 5 | P1 |
| 18 | TrendingScreen | trending | Non | 5 | P1 |
| 19 | SalonPostsScreen | salon/[id]/posts | Non | 5 | P1 |
| 20 | CreateReviewScreen | review/create | Non | 7 | P1 |
| 21 | ProfileScreen | (tabs)/profile | Non | 6 | P0 |
| 22 | ClientProfileScreen | profile/client/[id] | Non | 6 | P1 |
| 23 | CoiffeurProfileScreen | profile/coiffeur/[id] | Non | 6 | P1 |
| 24 | SalonOwnerProfileScreen | profile/owner/[id] | Non | 6 | P1 |
| 25 | SalonSocialProfileScreen | profile/salon/[id] | Non | 6 | P1 |
| 26 | CreateSalonScreen | salon/create | Non | 7 | P1 |
| 27 | CreateServiceScreen | salon/[id]/services/create | Non | 7 | P1 |
| 28 | CreateStaffScreen | salon/[id]/staff/create | Non | 7 | P1 |
| 29 | QueueManagementScreen | salon/[id]/queue | Non | 7 | P1 |
| 30 | OwnerBookingsManagementScreen | salon/[id]/bookings | Non | 7 | P1 |
| 31 | CreatePortfolioScreen | portfolio/create | Non | 8 | P2 |
| 32 | PortfolioDetailScreen | portfolio/[id] | Non | 8 | P2 |
| 33 | PortfoliosListScreen | portfolio/list/[ownerId] | Non | 8 | P2 |
| 34 | CollectionsScreen | collection/[userId] | Non | 8 | P2 |
| 35 | CollectionDetailScreen | collection/detail/[id] | Non | 8 | P2 |
| 36 | FavoritesScreen | favorites/[userId] | Non | 8 | P2 |
| 37 | ArchivesScreen | archives/[userId] | Non | 8 | P2 |
| 38 | ReportScreen | report | Non | 8 | P2 |
| 39 | SettingsScreen | settings/index | Non | 9 | P1 |
| 40 | SecuritySettingsScreen | settings/security | Non | 9 | P1 |
| 41 | ChangeEmailScreen | settings/change-email | Non | 9 | P2 |
| 42 | ChangePhoneScreen | settings/change-phone | Non | 9 | P2 |
| 43 | RequestVerificationScreen | settings/verification | Non | 9 | P2 |
| 44 | PaymentMethodsScreen | settings/payment-methods | Non | 9 | P3 |
| 45 | BlockedUsersScreen | settings/blocked-users | Non | 9 | P3 |
| 46 | HelpCenterScreen | settings/help | Non | 9 | P3 |
| 47 | ContactSupportScreen | settings/contact | Non | 9 | P3 |
| 48 | TermsOfServiceScreen | settings/terms | Non | 9 | P3 |
| 49 | PrivacyPolicyScreen | settings/privacy | Non | 9 | P3 |

**Legende priorites** :
- P0 : Bloquant - l'app ne peut pas sortir sans
- P1 : Important - fonctionnalite cle
- P2 : Normal - peut sortir en v1.1 si necessaire
- P3 : Placeholder - contenu statique, peut etre une WebView temporaire

---

## 6. CONTRAT API - REFERENCE

### Configuration HTTP (a reproduire exactement)

```
Base URL:        AppConfig.baseUrl (localhost:9090 en dev)
Timeouts:        request=60s, connect=15s, socket=60s
Content-Type:    application/json (defaut)
                 application/x-www-form-urlencoded (completeRegistration)
                 multipart/form-data (uploadImage)
Auth Header:     Authorization: Bearer <accessToken>
Special Header:  X-Refresh-Token: <refreshToken> (sessions endpoints)
JSON Config:     ignoreUnknownKeys=true, isLenient=true, encodeDefaults=true
```

### Token Refresh - Algorithme exact

```
1. Requete API retourne 401
2. Verifier : refreshToken existe ?
   Non -> throw AuthenticationException
   Oui -> continuer
3. Acquerir le mutex (un seul refresh a la fois)
4. Creer un client HTTP temporaire SANS interceptor
5. POST /api/users/refresh { refreshToken }
6. Si 200:
   - Sauvegarder nouveaux tokens (access + refresh)
   - Mettre a jour les headers
   - Rejouer la requete originale
   - Si 401 a nouveau -> throw AuthenticationException
7. Si erreur:
   - Effacer tous les tokens
   - throw AuthenticationException("Session expiree")
8. Liberer le mutex
```

### Endpoints publics (pas de Bearer)

```
POST /api/users/login
POST /api/users/register
POST /api/users/refresh
POST /api/users/complete-registration
POST /api/users/forgot-password
POST /api/users/reset-password
POST /api/payments/webhook
GET  /api/salons (marketplace)
GET  /api/salons/** (detail)
GET  /api/social/feed (feed public)
GET  /api/social/posts/** (post detail)
GET  /api/salons/*/services/**
GET  /api/salons/*/staff/**
GET  /uploads/**
```

### Upload d'image - Format multipart exact

```
POST /api/media/upload
Content-Type: multipart/form-data

Form field: "file"
  - Content-Disposition: form-data; name="file"; filename="photo.jpg"
  - Content-Type: image/jpeg (auto-detect par extension)
  - Body: raw bytes

Response 200: { "url": "http://localhost:9090/uploads/uuid.jpg" }
Response 413: fichier > 10MB
```

---

## 7. STRATEGIE DE TEST

### Pyramide de tests

```
                    /\
                   /  \        12 scenarios E2E (Detox/Maestro)
                  /    \       Parcours utilisateur complets
                 /------\
                /        \     49 tests d'integration (1 par ecran)
               /          \    Rendu + navigation + API mock
              /------------\
             /              \  ~200 tests unitaires
            /                \ API client, stores, hooks, utils, types
           /------------------\
```

### Niveau 1 : Tests unitaires (~200)

| Module | Tests | Outil |
|--------|-------|-------|
| API client (interceptors, refresh) | 15 | Jest + MSW |
| Auth store (Zustand) | 10 | Jest |
| Preferences store | 5 | Jest |
| Types/validation (Zod schemas) | 50 | Jest |
| React Query hooks | 40 | React Testing Library + MSW |
| Utils (format, validation, image) | 30 | Jest |
| i18n (cles presentes, plurals) | 10 | Jest |
| Theme (tokens corrects) | 5 | Jest |
| Components (render, props) | 35 | React Native Testing Library |

### Niveau 2 : Tests d'integration (49 - un par ecran)

Chaque ecran a un test qui :
1. Monte le composant avec les providers necessaires
2. Mock les appels API (MSW)
3. Verifie le rendu initial
4. Simule les interactions utilisateur
5. Verifie les appels API effectues
6. Verifie la navigation declenchee

### Niveau 3 : Tests E2E (12 scenarios)

Les memes 12 scenarios que la Phase 0 (baseline KMP).
Executes sur un vrai device/emulateur avec le vrai backend.

### Script de verification des cles i18n

```bash
# Verifier que toutes les cles de fr.json existent dans les 4 autres langues
node scripts/check-i18n-keys.js
# Output: OK (0 missing) ou FAIL (liste des cles manquantes)
```

### Script de verification des types API

```bash
# Generer les types depuis le Swagger du backend et comparer
# avec les types ecrits manuellement
node scripts/check-api-types.js
# Output: OK (tous les champs correspondent) ou DIFF (liste des ecarts)
```

---

## 8. RISQUES ET MITIGATIONS

| # | Risque | Impact | Probabilite | Mitigation |
|---|--------|--------|-------------|------------|
| R1 | Token refresh race condition en React Native | Session perdue, UX cassee | Moyenne | Implementer un mutex (p-limit ou semaphore pattern) identique au Kotlin |
| R2 | Cles i18n manquantes dans 1+ langue | Texte affiche comme cle brute | Haute | Script CI qui valide 100% des cles dans les 5 langues |
| R3 | Champs API manquants dans les types TS | Crash runtime sur undefined | Haute | Comparer types TS vs Swagger backend + tests Zod |
| R4 | Deep link Stripe payment return echoue | Paiement confirme mais UX bloquee | Moyenne | Tester deep link sur device reel, fallback polling |
| R5 | Fonts custom pas chargees (Cormorant) | Design casse, texte en system font | Basse | expo-font avec SplashScreen.preventAutoHideAsync() |
| R6 | RTL arabe casse les layouts | UI inutilisable en arabe | Moyenne | Tester chaque ecran en AR, utiliser I18nManager.isRTL |
| R7 | Performance scroll feed social | Lag, FPS < 60 | Moyenne | FlashList au lieu de FlatList, memoisation PostCard |
| R8 | Upload image echoue sur gros fichiers | UX bloquee, post sans image | Basse | Compression avant upload (expo-image-manipulator), progress bar |
| R9 | Queue polling 15s surcharge le serveur | Backend lent | Basse | Meme intervalle que KMP (15s), React Query staleTime |
| R10 | Migration des sessions existantes | Users KMP doivent se re-logger | Certaine | Acceptable - refresh token reste valide 7j cote backend |

---

## 9. CRITERES DE GO/NO-GO

### Go pour release

TOUS les criteres suivants doivent etre remplis :

- [ ] 12/12 scenarios E2E passent a 100%
- [ ] 0 crash detecte sur 1h d'utilisation continue
- [ ] 49/49 ecrans fonctionnels (rendu + interactions + API)
- [ ] 5/5 langues completes (0 cle manquante)
- [ ] Dark mode operationnel sur tous les ecrans
- [ ] Token refresh fonctionne (test: expirer le token manuellement)
- [ ] Upload image fonctionne (photo + galerie)
- [ ] Paiement Stripe fonctionne (sandbox)
- [ ] Push notification recue (FCM)
- [ ] Performance: < 2s premier affichage, 60 FPS scroll
- [ ] Taille APK < 50MB
- [ ] Aucune regression identifiee vs baseline KMP

### No-go (bloque la release)

- Un scenario E2E echoue
- Un crash reproductible
- Le token refresh ne fonctionne pas
- Le paiement ne fonctionne pas
- Plus de 5% des cles i18n manquantes
- Performance < 30 FPS sur scroll

---

## 10. ROLLBACK STRATEGY

### Pendant la migration (phases 1-9)

L'app KMP reste la reference en production. Le nouveau projet React Native est developpe
dans un repo/dossier separe. Aucun risque de regression sur l'app existante.

### Apres release (phase 11)

Si un probleme critique est detecte post-release :

1. **Rollback immediat** : Republier l'ancienne version KMP sur les stores
   - APK/AAB KMP est archive et taggue AVANT la release RN
   - Le backend n'a PAS change -> compatible avec les deux apps
2. **Hotfix** : Corriger dans la branche React Native et re-deployer
3. **Communication** : Notifier les utilisateurs via push notification

### Conservation de l'app KMP

L'app KMP est conservee (code gel) pendant minimum 3 mois apres la release React Native,
le temps de confirmer la stabilite. Elle peut etre re-deployee a tout moment car le
backend est identique.

---

## RESUME EXECUTIF

| Metrique | Valeur |
|----------|--------|
| Ecrans a migrer | 49 |
| Endpoints API | 120+ |
| Langues | 5 |
| Maquettes design v2 | 5 ecrans |
| Phases | 12 (0-11) |
| Tests E2E | 12 scenarios |
| Tests unitaires | ~200 |
| Composants a creer | 40+ |
| Fichiers de types | 16 |
| Fichiers API | 18 |
| Fichiers i18n | 5 JSON |

### Dependances entre phases

```
Phase 0 (Tests baseline)
  |
  v
Phase 1 (Scaffolding)
  |
  v
Phase 2 (Infrastructure) -----> Prerequis pour TOUTES les phases suivantes
  |
  v
Phase 3 (Auth) ----------------> Prerequis pour phases 4-9
  |
  +-----> Phase 4 (Core)
  |         |
  |         +-----> Phase 7 (Management)
  |
  +-----> Phase 5 (Social)
  |         |
  |         +-----> Phase 8 (Advanced)
  |
  +-----> Phase 6 (Profiles)
  |
  +-----> Phase 9 (Settings)
  |
  v
Phase 10 (Tests + QA)
  |
  v
Phase 11 (Beta + Release)
```

Les phases 4-9 peuvent etre parallelisees par plusieurs developpeurs une fois
la phase 3 terminee (l'auth est le prerequis commun).
