# PLAN DE MIGRATION COMPLET - React Native Frollot

## Document de reference base sur l'audit exhaustif de 9 agents d'analyse

Date: 2026-06-09
Statut: Plan definitif a suivre etape par etape

---

## ETAT ACTUEL - SYNTHESE DE L'AUDIT

### Ce qui est FAIT

| Couche | Statut | Detail |
|--------|--------|--------|
| Scaffolding Expo SDK 56 | FAIT | RN 0.85.3, TS 6, Expo Router, app.json configure |
| Client API Axios (client.ts) | FAIT | 165L, interceptors, refresh mutex, secure-store |
| Auth Store Zustand | FAIT | 99L, login/register/logout/refresh/init |
| Types TypeScript | PARTIEL | 10/16 fichiers (890L) - regroupement different du plan |
| Theme Design System | FAIT 95% | colors (180L), typography (119L), tokens (66L) - conforme design v2 |
| i18n Config | FAIT | i18next + 5 JSON mais couverture faible |
| 36 ecrans crees | PARTIEL | Code fonctionnel mais features manquantes vs KMP |
| API layer (12 fichiers) | PARTIEL | ~590L, couverture 85% des endpoints |

### Ce qui MANQUE - Decouvert par l'audit

#### BUGS CRITIQUES (a corriger immediatement)

| # | Bug | Impact | Agent | Statut |
|---|-----|--------|-------|--------|
| B1 | **Refresh token path mismatch** : `/api/auth/refresh` -> `/api/users/refresh` | Token refresh casse | Agent 5 | CORRIGE |
| B2 | **Queue paths incorrects** : `/api/queue/{id}` -> `/api/salons/{id}/queue` | File d'attente cassee | Agent 5 | CORRIGE |
| B3 | **VerificationType enum** : `IDENTITY` -> `BUSINESS`+`PROFESSIONAL` | Badges casses | Agent 4 | CORRIGE |
| B4 | **PostVisibility enum** | Deja present dans social.ts | Agent 4 | OK (faux positif) |
| B5 | **Media upload MIME type** : auto-detect par extension | Upload PNG/WEBP | Agent 5 | CORRIGE |
| B6 | **Search param name** : `query` -> `q` | Recherche hashtags | Agent 5 | CORRIGE |
| B7 | **Base URL port** : 8080 -> 9090 | Connexion au backend | - | CORRIGE |

#### GAPS STRUCTURELS

| Categorie | KMP | RN | Gap |
|-----------|-----|-----|-----|
| Composants reutilisables | 40+ | 0 | 40 composants a creer |
| React Query hooks | N/A | 0 | Pattern manquant |
| Ecrans | 49 | 36 | 13 ecrans manquants |
| Cles i18n (EN/FR) | 867 | 213 | 654 cles manquantes (75%) |
| Cles i18n (ES/DE) | 867 | 70 | 797 cles manquantes (92%) |
| Cles i18n (AR) | 867 | 61 | 806 cles manquantes (93%) |
| Endpoints API | 163 | 139 | 24 endpoints manquants |
| Types/enums | 22 enums | 14 enums | 8 enums manquants |
| Features par ecran | ~180 features | manquantes | Pagination, offline, mentions, etc. |
| Tests | 200+ prevus | 0 | Tout a faire |
| Design v2 applique | 5 maquettes | 0 appliquees | Les 5 ecrans maquettes sans design v2 |
| Preferences Store | 1 | 0 | Dark mode, langue non persistes |

---

## PLAN D'ACTION - 15 ETAPES SEQUENTIELLES

### Vue d'ensemble des etapes

```
ETAPE 1  : Corriger les bugs critiques (B1-B7)                    ~0.5j  [FAIT]
ETAPE 2  : Completer les types TypeScript manquants                ~1j   [FAIT]
ETAPE 3  : Completer la couche API manquante                       ~1j   [FAIT]
ETAPE 4  : Creer la librairie de composants reutilisables          ~3j   [FAIT - Lot 1]
ETAPE 5  : Preferences Store + dark mode + fonts                   ~0.5j  [FAIT]
ETAPE 6  : Appliquer le design v2 aux 5 ecrans maquettes           ~4j   [FAIT]
ETAPE 6B : Migration theming - Dark mode (P0 CRITIQUE)             ~2j   [FAIT]
ETAPE 7  : Enrichir les ecrans existants (features manquantes)     ~5j   [FAIT]
ETAPE 8  : Creer les 14 ecrans manquants (revise, paiement retire) ~3j
ETAPE 9  : Completer les traductions i18n (5 langues)              ~2j
ETAPE 10 : Navigation avancee (drawer, deep links, auth guards)    ~1.5j
ETAPE 11 : Gestion offline + performance                           ~1.5j
ETAPE 12 : Tests unitaires et d'integration                        ~3j
ETAPE 13 : Tests E2E (12 scenarios)                                ~2j
ETAPE 14 : QA, polish, dark mode, RTL arabe                        ~2j
ETAPE 15 : Build production + beta                                 ~1.5j
                                                          TOTAL : ~31 jours
```

---

### ETAPE 1 : Corriger les bugs critiques (0.5 jour) -- FAIT

**Objectif** : Eliminer les 6 bugs bloquants decouverts par l'audit.

#### 1.1 - Fix refresh token path (B1)
**Fichier** : `src/api/client.ts`
```
AVANT:  POST /api/auth/refresh
APRES:  POST /api/users/refresh
```
Verifier le path exact dans le backend SecurityConfig.kt (ligne ~100).

#### 1.2 - Fix queue paths (B2)
**Fichier** : `src/api/queue.ts`
```
AVANT:  /api/queue/{salonId}
APRES:  /api/salons/{salonId}/queue

Endpoints a corriger :
- GET  /api/salons/{salonId}/queue          (getQueueStatus)
- POST /api/salons/{salonId}/queue/join     (joinQueue)
- POST /api/salons/{salonId}/queue/leave    (leaveQueue)
- POST /api/salons/{salonId}/queue/call-next (callNextClient)
- DELETE /api/salons/{salonId}/queue/{entryId} (removeQueueEntry)
```

#### 1.3 - Fix VerificationType enum (B3)
**Fichier** : `src/types/user.ts`
```typescript
// AVANT (incorrect)
enum VerificationType { EMAIL, PHONE, IDENTITY }

// APRES (correct - conforme backend)
enum VerificationType { EMAIL, PHONE, BUSINESS, PROFESSIONAL }
```

#### 1.4 - Ajouter PostVisibility enum (B4)
**Fichier** : `src/types/social.ts`
```typescript
// AJOUTER
export enum PostVisibility {
  PUBLIC = 'PUBLIC',
  FOLLOWERS = 'FOLLOWERS',
  PRIVATE = 'PRIVATE'
}
```

#### 1.5 - Fix media upload MIME type (B5)
**Fichier** : `src/api/media.ts`
```typescript
// AJOUTER auto-detection du MIME type
function getMimeType(fileName: string): string {
  if (fileName.endsWith('.png')) return 'image/png';
  if (fileName.endsWith('.webp')) return 'image/webp';
  if (fileName.endsWith('.gif')) return 'image/gif';
  return 'image/jpeg'; // default
}
```

#### 1.6 - Fix search param name (B6)
**Fichier** : `src/api/social.ts`
```
AVANT:  params: { query }
APRES:  params: { q: query }
```

**Critere de validation** : Tous les paths correspondent au backend. Tests manuels login + refresh + queue + upload.

---

### ETAPE 2 : Completer les types TypeScript manquants (1 jour) -- FAIT

**Objectif** : Parite 100% avec les models Kotlin backend.

#### 2.1 - Creer `src/types/moderation.ts` (NOUVEAU)
```typescript
// Enums
export enum ReportedEntityType { POST, COMMENT, USER, SALON }
export enum ReportReason { SPAM, HARASSMENT, INAPPROPRIATE, MISLEADING, OTHER }
export enum ReportStatus { PENDING, UNDER_REVIEW, RESOLVED, DISMISSED }
export enum ModerationActionType { WARNING, CONTENT_REMOVAL, ACCOUNT_SUSPENSION }
export enum AppealStatus { PENDING, UNDER_REVIEW, ACCEPTED, REJECTED }

// Interfaces
export interface CreateReportRequest { ... }
export interface ReportResponse { ... }
export interface HandleReportRequest { ... }
export interface ModerateContentRequest { ... }
export interface ModerationActionResponse { ... }
export interface AppealModerationRequest { ... }
export interface HandleAppealRequest { ... }
```

#### 2.2 - Creer `src/types/collection.ts` (NOUVEAU)
```typescript
export enum CollectionCategory { INSPIRATIONS, FAVORITES, SAVED, CUSTOM }
export interface CollectionResponse { ... }
export interface CreateCollectionRequest { ... }
export interface UpdateCollectionRequest { ... }
export interface CollectionPostResponse { ... }
```

#### 2.3 - Creer `src/types/search.ts` (NOUVEAU)
```typescript
export enum SearchType { ALL, POSTS, SALONS, USERS, HASHTAGS }
export interface SearchResponse { posts, salons, users, hashtags }
export interface SearchFilters { ... }
```

#### 2.4 - Creer `src/types/security.ts` (NOUVEAU)
```typescript
export interface SessionInfo { ... }
export interface SessionsListResponse { ... }
export interface ChangePasswordRequest { ... }
export interface ChangeEmailRequest { ... }
export interface ChangePhoneRequest { ... }
export interface DeleteAccountRequest { ... }
```

#### 2.5 - Completer `src/types/social.ts`
Ajouter les interfaces manquantes :
- PostVisibility (fait en 1.4)
- HairHashtagCategory enum (5 valeurs)
- PostShareResponse
- FollowResponse (completer)
- HairHashtagResponse

#### 2.6 - Completer `src/types/user.ts`
Ajouter les champs manquants au model User :
- bio, yearsExperience, certifications, instagramHandle
- specialties, preferredLanguage, emailVerified

#### 2.7 - Completer `src/types/booking.ts`
- Ajouter StripePaymentStatus avec les 7 valeurs (pending, processing, succeeded, failed, canceled, refunded, partially_refunded)
- Ajouter BookingStatistics interface

#### 2.8 - Completer `src/types/review.ts`
- Ajouter staffId, staffName, responseSalon, responseAt au Review

#### 2.9 - Creer `src/types/common.ts`
```typescript
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}
```

**Critere** : `npx tsc --noEmit` passe sans erreur. Tous les enums correspondent au backend.

---

### ETAPE 3 : Completer la couche API manquante (1 jour) -- FAIT

**Objectif** : 163/163 endpoints couverts (actuellement 139/163).

#### 3.1 - Creer `src/api/moderation.ts` (NOUVEAU - 10 endpoints)
```typescript
reportContent(request)                    // POST /api/social/reports
getMyReports(page, size)                  // GET /api/social/reports/my-reports
getReports(page, size, status?)           // GET /api/social/reports (ADMIN)
getPendingReports(page, size)             // GET /api/social/reports/pending (ADMIN)
handleReport(reportId, request)           // PUT /api/social/moderation/reports/{id}/handle
moderateContent(request)                  // POST /api/social/moderation/moderate (ADMIN)
appealModeration(request)                 // POST /api/social/moderation/appeal
handleAppeal(moderationActionId, request) // PUT /api/social/moderation/appeals/{id}/handle
getModerationActions(entityType, entityId)// GET /api/social/moderation/actions/entity/{type}/{id}
getPendingAppeals(page, size)             // GET /api/social/moderation/appeals/pending (ADMIN)
```

#### 3.2 - Creer `src/api/verification.ts` (NOUVEAU - 5 endpoints)
```typescript
requestVerification(entityType, entityId, request) // POST /api/verification/request/{type}/{id}
verifyUser(userId, request)                        // PUT /api/verification/users/{id}/verify (ADMIN)
verifySalon(salonId, request)                      // PUT /api/verification/salons/{id}/verify (ADMIN)
revokeVerification(entityType, entityId)           // DELETE /api/verification/{type}/{id}/revoke (ADMIN)
requestVerificationCode()                          // POST /api/users/request-verification
```

#### 3.3 - Creer `src/api/collections.ts` (NOUVEAU - complet)
```typescript
createCollection(request)                          // POST /api/collections
updateCollection(collectionId, request)            // PUT /api/collections/{id}
deleteCollection(collectionId)                     // DELETE /api/collections/{id}
getCollectionsByUser(userId)                       // GET /api/collections/user/{id}
getCollectionById(collectionId)                    // GET /api/collections/{id}
addPostToCollection(collectionId, postId)          // POST /api/collections/{id}/posts/{postId}
removePostFromCollection(collectionId, postId)     // DELETE /api/collections/{id}/posts/{postId}
getCollectionPosts(collectionId, page, size)       // GET /api/collections/{id}/posts
getPublicCollections()                             // GET /api/collections/public
```

#### 3.4 - Completer `src/api/bookings.ts` (3 endpoints manquants)
```typescript
getAvailableSlots(salonId, request)    // POST /api/salons/{salonId}/available-slots
updateBookingStatus(bookingId, request)// PATCH /api/bookings/{id}/status
updateBookingPayment(bookingId, request)// PATCH /api/bookings/{id}/payment
getBookingStatistics(salonId)          // GET /api/salons/{salonId}/bookings/statistics
```

#### 3.5 - Creer `src/api/search.ts` (NOUVEAU)
```typescript
unifiedSearch(query, type, filters, page, size)    // GET /api/social/search
```
Note : certains endpoints search existent deja dans social.ts, deplacer ou re-exporter.

#### 3.6 - Mettre a jour `src/api/index.ts`
Ajouter les exports des nouveaux modules : moderation, verification, collections, search.

**Critere** : 163/163 endpoints presents. Chaque endpoint est appelable et retourne le bon type.

---

### ETAPE 4 : Creer la librairie de composants reutilisables (3 jours)

**Objectif** : Recreer les 40+ composants KMP en React Native.

#### Structure cible
```
src/components/
  ui/           -> Atomes (Button, Input, Card, Badge, Chip, Dialog, Toast)
  salon/        -> SalonCard, ServiceItem, StaffCard
  social/       -> PostCard, CommentItem, ReactionBar, BeforeAfterMedia
  booking/      -> BookingStepper, TimeSlotGrid, DaySelector
  profile/      -> ProfileHeaderCard, StatsSection, BadgesSection
  common/       -> Avatar, Logo, SearchBar, VerificationBadge
  layout/       -> SafeArea, ScrollContainer, Header, AppDrawer
  lists/        -> EmptyState, LoadingState, ErrorState
  forms/        -> StandardTextField, PasswordTextField, SearchTextField
```

#### Jour 1 - Atomes et Layout (16 composants)

| # | Composant | Source KMP | Priorite |
|---|-----------|-----------|----------|
| 1 | `PrimaryButton` | buttons/PrimaryButton.kt | P0 |
| 2 | `SecondaryButton` | buttons/SecondaryButton.kt | P0 |
| 3 | `TextButton` | buttons/TextButton.kt | P1 |
| 4 | `StandardTextField` | forms/StandardTextField.kt | P0 |
| 5 | `PasswordTextField` | forms/PasswordTextField.kt | P0 |
| 6 | `SearchTextField` | forms/SearchTextField.kt | P1 |
| 7 | `StandardCard` | cards/StandardCard.kt | P0 |
| 8 | `UserAvatar` | UserAvatar.kt | P0 |
| 9 | `FrollotLogo` (4 variants) | FrollotLogo.kt | P1 |
| 10 | `FrollotToast` | FrollotToast.kt | P0 |
| 11 | `VerificationBadge` | VerificationBadge.kt | P1 |
| 12 | `StatusBadge` | design v2 screen-shared.jsx | P1 |
| 13 | `FilterChip` | chips/FilterChip.kt | P1 |
| 14 | `ListEmptyState` | lists/ListEmptyState.kt | P0 |
| 15 | `ListLoadingState` | lists/ListLoadingState.kt | P0 |
| 16 | `ListErrorState` | lists/ListErrorState.kt | P0 |

#### Jour 2 - Composants metier (12 composants)

| # | Composant | Source KMP | Priorite |
|---|-----------|-----------|----------|
| 17 | `SalonCard` | SalonCard.kt | P0 |
| 18 | `RatingBar` | RatingBar.kt | P0 |
| 19 | `ReviewCard` | ReviewCard.kt | P1 |
| 20 | `QueueStatusCard` | QueueStatusCard.kt | P1 |
| 21 | `PostCard` | UltraPremiumPostCard.kt | P0 |
| 22 | `CommentItem` | inline dans CommentsScreen | P1 |
| 23 | `ReactionsMenu` | ReactionsMenu.kt | P1 |
| 24 | `BeforeAfterMedia` | design v2 screen-feed.jsx | P1 |
| 25 | `BookingStepper` | design v2 screen-booking.jsx | P0 |
| 26 | `TimeSlotGrid` | BookingScreen.kt | P0 |
| 27 | `DaySelector` | BookingScreen.kt | P0 |
| 28 | `ServiceItem` | SalonDetailScreen.kt | P0 |

#### Jour 3 - Composants complexes (12 composants)

| # | Composant | Source KMP | Priorite |
|---|-----------|-----------|----------|
| 29 | `ProfileHeaderCard` | profile/ProfileHeaderCard.kt | P0 |
| 30 | `StandardAppHeader` | StandardAppHeader.kt | P0 |
| 31 | `AppDrawer` | AppDrawer.kt | P0 |
| 32 | `BottomTabBar` | design v2 screen-home.jsx | P0 |
| 33 | `PostOptionsMenu` | menus/PostOptionsMenu.kt | P1 |
| 34 | `ImagePicker` | ImagePicker.kt | P1 |
| 35 | `FullScreenImageViewer` | FullScreenImageViewer.kt | P2 |
| 36 | `PullToRefresh` | PullToRefresh.kt | P0 |
| 37 | `StandardDialog` | dialogs/StandardDialog.kt | P1 |
| 38 | `ExternalShareDialog` | ExternalShareDialog.kt | P2 |
| 39 | `PaymentCard` | payment/PaymentCard.kt | P2 |
| 40 | `AnnotatedTextField` | AnnotatedTextField.kt (mentions/hashtags) | P2 |

**Critere** : Chaque composant rend correctement en light + dark mode. Props correspondent aux equivalents KMP.

---

### ETAPE 5 : Preferences Store + Dark mode + Fonts (0.5 jour)

#### 5.1 - Creer `src/stores/preferencesStore.ts`
```typescript
interface PreferencesStore {
  darkMode: 'system' | 'light' | 'dark';
  language: 'en' | 'fr' | 'es' | 'de' | 'ar';
  setDarkMode: (mode) => void;
  setLanguage: (lang) => void;
  initializeFromStorage: () => Promise<void>;
}
```
Persister avec MMKV ou AsyncStorage.

#### 5.2 - Charger les fonts custom dans `app/_layout.tsx`
```typescript
import { useFonts } from 'expo-font';
// Charger Cormorant Garamond (Regular, SemiBold, Bold)
// Charger Manrope (Regular, Medium, SemiBold, Bold)
// SplashScreen.preventAutoHideAsync() jusqu'a chargement
```

#### 5.3 - Integrer le dark mode dans le theme
Utiliser `useColorScheme()` + preferences store pour basculer entre light/dark.

**Critere** : Dark mode fonctionne. Fonts Cormorant Garamond et Manrope visibles. Langue persiste apres kill app.

---

### ETAPE 6 : Appliquer le design v2 aux 5 ecrans maquettes (4 jours)

**Objectif** : Les 5 ecrans avec maquettes design v2 doivent etre pixel-perfect.

#### 6.1 - LoginScreen (1 jour)
**Maquette** : `design_v2/frollot/project/screen-login.jsx`

Specs a implementer :
- Hero photo plein cadre (320px height)
- Gradient overlay : `rgba(40,23,51,.42) -> transparent -> surface`
- Logo "F" glassmorphism (blur 6px, border rgba(255,255,255,.35))
- Brand text "Frollot" Cormorant Garamond 26px
- Overline "Bon retour parmi nous" secondary color
- Titre "Bienvenue" Cormorant 44px, 600 weight
- Champs avec icon gauche (22px), label au-dessus, border focused 2px primary
- Bouton "Se connecter" primary 48px height, radius full, icon login
- Separateur "OU" avec lignes
- Bouton "Creer un compte" outline
- Footer CGU body-sm

#### 6.2 - HomeScreen (1 jour)
**Maquette** : `design_v2/frollot/project/screen-home.jsx`

Specs a implementer :
- Header : hamburger + "Frollot" Cormorant 24px + notif + Avatar avec ring gradient
- Search bar pill : radius full, surface-container-high, 52px height
- Salutation : overline date secondary + headline Cormorant 30px
- Categories horizontales : icons rondes 58px, premiere selectionnee (primary bg)
- Section "Salons recents" : carrousel horizontal, cards 220px
  - Image 132px + coeur favori + nom + lieu + note (tertiary)
- Bandeau file : gradient primary, radius xl, CTA blanc
- BottomNav 5 onglets avec pill secondary-container active

#### 6.3 - SocialFeedScreen (0.5 jour)
**Maquette** : `design_v2/frollot/project/screen-feed.jsx`

Specs a implementer :
- Header : hamburger + "Fil social" + search + add_circle
- Onglets : Tous | Suivis | Tendances (underline primary 3px)
- PostCard complet :
  - Avatar 44px avec ring + nom + verified + type badge (secondary-container)
  - Texte + hashtags primary bold
  - Media OU avant/apres (swap_horiz icon au centre, 40x40 blanc)
  - Engagement : coeur(secondary) + comment + share + bookmark

#### 6.4 - SalonDetailScreen (1 jour)
**Maquette** : `design_v2/frollot/project/screen-salon.jsx`

Specs a implementer :
- Cover 230px + gradient overlay + boutons flottants (back, share, favorite)
- Sheet arrondie (24px top) qui chevauche cover de -22px
- Nom Cormorant 30px + adresse + badge note (tertiary-container)
- Bandeau file : success-container, dot anime pulse, "~15 min d'attente"
- Onglets : Services | Equipe | Avis | Posts | Info (underline 3px)
- Service items : overline categorie, nom, duree+prix, bouton "Reserver" outline pill
- Barre flottante bas : "Suivre" outline + "Reserver une prestation" primary

#### 6.5 - BookingScreen (0.5 jour)
**Maquette** : `design_v2/frollot/project/screen-booking.jsx`

Specs a implementer :
- Header avec back + "Reserver" + subtitle salon
- Stepper 4 etapes visuelles : cercles 28px + lignes + labels
  - Done : vert + checkmark, Current : primary + numero, Todo : gris
- Resume service (primary-container, radius md, icon + nom + duree/prix + "Modifier")
- Mois header Cormorant 22px + fleches
- Jours : 7 jours horizontaux, selectionne=primary, off=dashed+opacity
- Creneaux : grille 3 colonnes, selectionne=primary, radius sm
- Resume bas fixe : date/heure + prix + bouton "Continuer" primary

**Critere** : Comparaison visuelle avec les maquettes JSX. Les 5 ecrans doivent etre conformes au design v2.

---

### ETAPE 6B : Migration theming - Dark mode (P0 CRITIQUE) -- FAIT

**Justification** : Les 15 composants et 10 ecrans utilisent des couleurs hex en dur
dans `StyleSheet.create()` statiques. Le hook `useTheme()` existe mais n'est pas
consomme. Le dark mode est structurellement casse. Chaque nouvel ecran reproduirait
le pattern casse. Correction obligatoire AVANT les etapes 7-8.

**Objectif** : Migrer les 27 fichiers impactes vers le pattern inline `useTheme()`.
Zero regression visuelle en mode light. Dark mode fonctionnel sur l'integralite de l'app.

**Pattern officiel (decide 2026-06-10)** : Pattern inline. `StyleSheet.create()` statique
ne contient QUE des valeurs structurelles (layout, font, radius, spacing). Toutes les
couleurs sont injectees en inline via `useTheme()` dans le composant. La factory
`createStyles(colors)` n'est PAS la cible — le pattern inline est le standard unique.

Exemple reel (tire de `src/components/ui/Card.tsx`) :
```tsx
// Dans le composant :
const { colors } = useTheme();
return (
  <View style={[styles.card, { backgroundColor: colors.surface, borderColor: colors.outlineVariant + '4D' }]}>
    {children}
  </View>
);

// StyleSheet statique — zero couleur :
const styles = StyleSheet.create({
  card: {
    borderRadius: 16,
    borderWidth: 0.5,
    shadowColor: 'rgb(39,26,44)', // design-fixed
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.12,
    shadowRadius: 3,
    elevation: 1,
  },
});
```

**Critere de validation** : Zero couleur hex/rgb/rgba en dur, a l'exception unique des
lignes explicitement marquees `// design-fixed`. La verification consiste a confirmer que
chaque couleur brute restante porte ce commentaire. Light mode identique pixel-pour-pixel.
Dark mode visuellement correct sur les 5 ecrans maquettes.

**Note polish dark mode** : Les overlays blancs `rgba(255,255,255,0.9x)` marques
`// design-fixed` (bouton favori SalonCard, icone swap PostCard) seront tres contrastes
en dark mode. A traiter en phase 14 (QA/polish dark mode), pas dans cette migration.

#### 6B.0 - Fix useTheme() pour respecter preferencesStore.themeMode
**Fichier** : `src/theme/index.ts`
**Bug** : `useTheme()` lisait `useColorScheme()` (OS) et ignorait `preferencesStore.themeMode`.
**Fix** : `useTheme()` consomme maintenant `themeMode` du store. Si `'dark'` → dark, si `'light'` → light, si `'system'` → suit l'OS.
**Statut** : FAIT

#### 6B.1 - Composants UI atomiques (6 fichiers)
- `src/components/ui/Button.tsx` -- FAIT
- `src/components/ui/TextField.tsx` -- FAIT
- `src/components/ui/Card.tsx` -- FAIT
- `src/components/ui/Chip.tsx` -- FAIT
- `src/components/ui/StatusBadge.tsx` -- FAIT
- `src/components/ui/Toast.tsx` -- FAIT

#### 6B.2 - Composants common (3 fichiers)
- `src/components/common/Avatar.tsx` -- FAIT
- `src/components/common/RatingStars.tsx` -- FAIT
- `src/components/common/SectionHeader.tsx` -- FAIT

#### 6B.3 - Composants metier (3 fichiers)
- `src/components/salon/SalonCard.tsx` -- FAIT
- `src/components/social/PostCard.tsx` -- FAIT
- `src/components/booking/BookingStepper.tsx` -- FAIT

#### 6B.4 - Composants lists (3 fichiers)
- `src/components/lists/EmptyState.tsx` -- FAIT
- `src/components/lists/ErrorState.tsx` -- FAIT
- `src/components/lists/LoadingState.tsx` -- FAIT

#### 6B.5 - Ecrans maquettes V2 (5 fichiers)
- `app/(auth)/login.tsx` -- FAIT (useTheme inline, 9 design-fixed: hero gradient, glassmorphism, brand text, shadowColor)
- `app/(tabs)/index.tsx` -- FAIT (useTheme inline, 8 design-fixed: banner gradient, banner content, shadowColors)
- `app/(tabs)/social.tsx` -- FAIT (useTheme ajoute, 11 hex remplaces par tokens, 0 design-fixed)
- `app/salon/[id].tsx` -- FAIT (useTheme ajoute, ~48 hex remplaces par tokens, 3 design-fixed: cover gradient, coverBtn overlay, shadowColor)
- `app/booking/new.tsx` -- FAIT (useTheme ajoute, ~40 hex remplaces par tokens, 1 design-fixed: shadowColor)

#### 6B.6 - Ecrans restants avec hex (5 fichiers)
- `app/(tabs)/bookings.tsx` -- FAIT (STATUS_COLORS migre vers tokens colors.warning/success/info/error, 0 hex)
- `app/owner-bookings.tsx` -- FAIT (STATUS_COLORS migre vers tokens, 0 hex)
- `app/queue-management.tsx` -- FAIT (STATUS_COLORS migre vers tokens, 0 hex)
- `app/create-post.tsx` -- FAIT (2 design-fixed: #fff icon sur overlay, rgba(0,0,0,0.6) overlay)
- `app/post/[id].tsx` -- FAIT (1 design-fixed: shadowColor #000)

#### 6B.7 - BottomNav pill active (1 fichier)
- `app/(tabs)/_layout.tsx` -- FAIT (pill secondaryContainer ajoutee sur onglet actif via focused + pillStyle, 0 hex)

---

### Correctifs hors-scope appliques pendant test 6B (2026-06-10)

Ces modifications ont ete faites pour debloquer le test du dark mode. Elles ne font
pas partie du scope theming mais sont necessaires au fonctionnement de l'app.

#### C1 - CORS : ajout localhost:8081 pour Expo dev server
**Fichier** : `backend/src/main/kotlin/com/frollot/security/SecurityConfig.kt` (lignes 217-220)
**Modification** : Ajout de `http://localhost:8081`, `http://localhost:8082`, `http://127.0.0.1:8081`
dans la whitelist CORS dev. Le frontend Expo tourne sur le port 8081, pas 9090.
**Statut** : FAIT

#### C2 - API paths : auth.ts corrige de /api/auth/ vers /api/users/
**Fichier** : `frollot-app/src/api/auth.ts` (lignes 23-45)
**Modification** : 8 endpoints corriges : login, register, preRegister, completeRegistration,
verifyEmail, resendVerificationEmail, forgotPassword, resetPassword.
Tous passent de `/api/auth/...` a `/api/users/...` pour correspondre au backend
(`@RequestMapping("/api/users")` dans UserController.kt).
**Note** : `preRegister` (ligne 30) et `register` (ligne 27) pointent tous deux vers
`/api/users/register`. Seul `preRegister` est appele dans le code (authStore.ts:74).
`register` est exporte mais jamais consomme — pas de regression.
**Statut** : FAIT

#### DETTE TECHNIQUE OUVERTE

**D1 - Lien de verification email** -- FAIT
**Ce qui est acquis** :
- URL centralisee via propriete `app.frontend.base-url` (application.yml dev: `http://localhost:9090`,
  application-prod.yml: `https://frollot.com`).
- Branche conditionnelle dev/prod dans EmailVerificationService (lignes 299 et 357) via
  `emailConfigService.isDevelopmentProfile()`.
- `isDevelopmentProfile()` rendue `internal` dans EmailConfigurationService (ligne 134).
- En dev, l'URL generee est correcte : `http://localhost:9090/api/users/complete-registration?token=T`
  (confirme par logs backend).
- Activation testee via `curl -X POST` (contournement manuel) : fonctionne, accessToken retourne.
**Clic email resolu** : `@PostMapping` remplace par `@RequestMapping(method = [GET, POST])`
dans UserController.kt:108. Le clic dans l'email (GET) fonctionne desormais.
Teste end-to-end : inscription olsenfauldy@gmail.com → email recu → clic lien →
GET /api/users/complete-registration?token=849793 → 200 OK → compte active → login reussi.
Ligne 299 (sendRealEmail) : verifiee par lecture uniquement, non testee end-to-end.
**Gap residuel** : `resendVerificationEmail` dans auth.ts pointe vers /api/users/resend-verification
au lieu de /api/users/me/resend-verification, ET cet endpoint est protege par isAuthenticated
(inutilisable sans token sur l'ecran de verification). Bouton renvoyer desactive sur
l'ecran de verification.
**Statut** : FAIT (2026-06-10) — flux inscription+verification+login fonctionnel end-to-end.

---

### ETAPE 7 : Enrichir les ecrans existants - Features manquantes (5 jours)

**Objectif** : Parite fonctionnelle 100% avec KMP (180+ features a ajouter).

#### Jour 1 - HomeScreen + SalonDetailScreen

**HomeScreen** - ajouter :
- [ ] Category filtering avec selection visuelle (toggle on/off)
- [ ] City/location filtering avec dialog
- [ ] Nearby filter GPS (defaut Paris 48.8566, 2.3522)
- [ ] Debounced search (500ms delay)
- [ ] Infinite scroll pagination (LazyColumn equivalent = FlatList)
- [ ] Pull-to-refresh (RefreshControl)
- [ ] Empty state avec CTA "Creer mon salon" pour salon_owner
- [ ] Queue banner card avec gradient et CTA

**SalonDetailScreen** - ajouter :
- [ ] Cover photo upload/edit (salon owner seulement)
- [ ] Follow/Unfollow avec compteur followers
- [ ] Onglet Equipe avec specialites
- [ ] Onglet Posts (navigation vers SalonPostsScreen)
- [ ] Onglet Info
- [ ] Queue polling automatique (30s)
- [ ] Barre flottante bas (Suivre + Reserver)

#### Jour 2 - BookingScreen + SocialFeedScreen

**BookingScreen (booking/new.tsx)** - ajouter :
- [ ] Appel API `getAvailableSlots()` (au lieu de creneaux hardcodes)
- [ ] Selection staff avec filtrage par specialite
- [ ] Validation date (pas de dates passees)
- [ ] Champ notes/demandes speciales
- [ ] Ecran de succes apres confirmation

**SocialFeedScreen** - ajouter :
- [ ] Infinite scroll pagination (currentPage + hasMore)
- [ ] Pull-to-refresh
- [ ] Onglet Following (api.getFollowingFeed)
- [ ] Onglet Nearby (api.getPostsNearby avec GPS)
- [ ] Full-screen image viewer pour media
- [ ] Post options menu (edit, delete, report, share)
- [ ] Collection save dialog
- [ ] External share

#### Jour 3 - CreatePostScreen + ProfileScreen

**CreatePostScreen** - ajouter :
- [ ] Mention suggestions (@user avec debounce search)
- [ ] Hashtag suggestions (#tag avec search)
- [ ] Post visibility control (PUBLIC, FOLLOWERS, PRIVATE)
- [ ] Media type assignment (before, after, detail)
- [ ] Validation AVANT_APRES (exactement 2 images)
- [ ] Character counter

**ProfileScreen** - ajouter :
- [ ] Avatar upload avec preview et save
- [ ] Stats dynamiques par UserType :
  - Client : bookings, reviews, points
  - SalonOwner : salons, services, points
  - Hairstylist : bookings, services, points
- [ ] Sections menu : Favorites, Archives, Collections, Portfolios, Settings
- [ ] Logout avec confirmation dialog

#### Jour 4 - LoginScreen + RegisterScreen + Profiles

**LoginScreen** - ajouter :
- [ ] Detection email non verifie -> redirect EmailVerification
- [ ] Error codes specifiques (INVALID_CREDENTIALS, ACCOUNT_DISABLED, etc.)
- [ ] Auto-navigation sur succes (2s delay)

**RegisterScreen** - ajouter :
- [ ] Champ confirmation mot de passe avec validation
- [ ] Email validation format (@ et .)
- [ ] Password mismatch indicator
- [ ] Pre-registration flow (pas registration directe)

**Profiles (4 ecrans)** - ajouter :
- [ ] Follow/Unfollow sur tous les profils
- [ ] Collections display sur ClientProfile
- [ ] Pinned posts sur CoiffeurProfile
- [ ] Salons list sur SalonOwnerProfile

#### Jour 5 - Ecrans restants

**MyBookingsScreen** - ajouter :
- [ ] Filtre par statut (ALL/UPCOMING/PAST)
- [ ] Salon name display (avec cache)
- [ ] Cancel booking avec confirmation dialog
- [ ] Pull-to-refresh

**QueueManagementScreen** - ajouter :
- [ ] Auto-poll 15s (useEffect + setInterval)
- [ ] Dashboard stats (waiting, called, avg wait)
- [ ] Mutex conceptuel (flag isUpdating)
- [ ] Optimistic updates

**TrendingScreen** - ajouter :
- [ ] Period selector (24h, 7j, 30j)
- [ ] Infinite scroll pour posts
- [ ] HorizontalPager pour sections

**CommentsScreen** - ajouter :
- [ ] Pagination avec "load more"
- [ ] Comment deletion (own comments)
- [ ] Post preview en header

**Critere** : Chaque ecran RN reproduit 100% des fonctionnalites de son equivalent KMP.

#### Corrections d'alignement API frontend-backend (preambule etape 7, 2026-06-10)

4 bugs de path/methode identifies par audit croise src/api/ vs backend controllers.
Corriges avant toute implementation de feature pour eviter les API fantomes.

| Bug | Fonction | Fichier:ligne | Correction | Debloque |
|-----|----------|---------------|------------|----------|
| B8 | `getStaffBySpecialty` | `src/api/salons.ts:75` | `/specialty/` -> `/specialties/` (pluriel, conforme SalonStaffController:207) | 7.1 |
| B9 | `updateUserAvatar` | `src/api/users.ts:21` | `api.put` -> `api.patch` (conforme UserController:695 `@PatchMapping`) | 7.5 |
| B10 | `hasReviewForBooking` | `src/api/reviews.ts:21` | `/api/reviews/booking/{id}/exists` -> `/api/bookings/{id}/review/exists` (conforme ReviewController:185) | 7.4 |
| B11 | `cancelBooking` | `src/api/bookings.ts:26-27` | `api.put(.../cancel)` -> `api.delete(/api/bookings/{id})` (conforme BookingController:364). NOTE: le backend fait un SOFT DELETE (statut CANCELLED + cancelledAt, save sans suppression, BookingService:359-362). La reservation reste en base. | 7.4 |

Statut : FAIT (4/4 corriges)

**B12** - Mensonge de type API `getSalons` -- FAIT
**Fichier** : `src/api/salons.ts:15-16`
**Bug** : `getSalons` declarait `api.get<Salon[]>` mais le backend (SalonController:129)
retourne `PageResponse<SalonResponse>` (objet pagine avec `.content`). Le frontend recevait
un objet au lieu d'un tableau → crash `salons.slice is not a function` sur HomeScreen,
et rendu silencieusement vide sur ExploreScreen.
**Correction** : Type corrige en `api.get<PageResponse<Salon>>` + extraction `.content`
dans la fonction API. Tous les appelants (index.tsx, explore.tsx) recoivent desormais
un vrai `Salon[]` sans patch local. `getTrendingSalons` non concerne (retourne `List<>` cote backend).
**Appelants impactes** : index.tsx:54, explore.tsx:23 (corriges automatiquement par le fix source).

**B13** - `unifiedSearch` non type (`any`) -- FAIT
**Fichier** : `src/api/social.ts:164-165`
**Bug** : `api.get('/api/social/search')` sans generic → `any`. Backend retourne `SearchResponse`.
**Correction** : Generic ajoute `api.get<SearchResponse>(...)`, import `SearchResponse` ajoute.

**B14** - `getUserBookings` URL 404 + mauvais type -- FAIT
**Fichier** : `src/api/bookings.ts:23-24`
**Bug** : URL `/api/bookings/user/{userId}` n'existe pas. Backend = `/api/clients/{clientId}/bookings`
retournant `List<BookingResponse>` (pas pagine). Type TS declarait `PageResponse<BookingResponse>`.
**Correction** : URL corrigee `/api/clients/${userId}/bookings`, type corrige `BookingResponse[]`.
Appelant `bookings.tsx:33` corrige : `result.content` → `result` (plus de `.content`).

**B15** - `getSalonReviews` + `getAllSalonReviews` mauvaise URL -- FAIT
**Fichier** : `src/api/reviews.ts:8-12`
**Bug** : URL `/api/reviews/salon/{id}` et `/api/reviews/salon/{id}/all` n'existent pas.
Backend = `/api/salons/{id}/reviews` (ReviewController base `/api` + `/salons/{id}/reviews`).
**Correction** : URLs corrigees vers `/api/salons/${salonId}/reviews` et `.../reviews/all`.

**B16** - `getPostsBySalon` URL inversee -- FAIT
**Fichier** : `src/api/social.ts:24`
**Bug** : URL `/api/social/posts/salon/{id}` → 404. Backend = `/api/social/salons/{id}/posts`.
**Correction** : URL corrigee.

**B17** - `getFeed` mauvaise URL -- FAIT
**Fichier** : `src/api/social.ts:22`
**Bug** : URL `/api/social/posts/feed` → 404. Backend = `/api/social/feed` (SocialController:150).
**Correction** : URL corrigee.

**B18** - `getFollowingFeed` mauvaise URL -- FAIT
**Fichier** : `src/api/social.ts:136`
**Bug** : URL `/api/social/posts/following` → 404. Backend = `/api/social/feed/following` (SocialController:330).
**Correction** : URL corrigee.

**B19** - `uploadImage` retourne un objet au lieu d'un string -- FAIT
**Fichier** : `src/api/media.ts:27-31`
**Bug** : `api.post<string>('/api/media/upload')` declarait un retour `string`, mais le backend
(MediaController:84) retourne `Map<String, String>` avec `{ url, path, uploadedBy, filename }`.
Le frontend passait l'objet entier comme `mediaUrl` dans `createPost` → backend rejectait avec
`Cannot deserialize String from Object`.
**Correction** : Type corrige en `{ url: string; path: string; uploadedBy: string; filename: string }`,
extraction `data.url` pour retourner la string URL.

**B20** - Images media non affichees (URL 10.0.2.2 injoignable en web) -- FAIT
**Cause** : Le backend MediaController:93 hardcode `http://10.0.2.2:$serverPort` comme base URL
des fichiers uploades. Cette URL est l'alias Android emulator pour localhost, injoignable depuis
un navigateur web. Les URLs sont stockees en base dans cette forme, et retournees telles quelles
par tous les endpoints de lecture (feed, salon, profil).
**Correction frontend** : Utilitaire `src/utils/media.ts` exporte `resolveMediaUrl(raw)` qui :
- Path relatif (`/uploads/...`) → prefixe avec API_BASE_URL du client courant
- URL absolue `http://10.0.2.2:9090/...` → reecrit le host vers API_BASE_URL
- Autre URL absolue → retourne telle quelle
- null/undefined → retourne undefined
**Branchements** : 3 composants partages couvrent la majorite des affichages :
- `PostCard.tsx` (4 points : imageUrl, media[0].mediaUrl, media[1].mediaUrl, singleImage)
- `Avatar.tsx` (1 point : imageUrl)
- `SalonCard.tsx` (1 point : imageUrl)
Les 14 ecrans qui affichent des images directement beneficient indirectement via ces composants.
Tous les ecrans restants ont ete branches sur resolveMediaUrl (15 points dans 11 fichiers) :
post/[id].tsx (2), portfolio/[id].tsx (2), trending.tsx (2), portfolios.tsx (1),
favorites/[userId].tsx (1), salon/[id].tsx (1), explore.tsx (1), profile/coiffeur (1),
profile/salon (1), profile/owner (2), profile/client (1).
3 URIs locales (picker) non touchees : create-salon.tsx, create-portfolio.tsx, create-post.tsx.
Dette B20 CLOSE — plus aucun ecran n'affiche d'image backend sans resolveMediaUrl.
**uploadImage** : aligne pour retourner `data.path` (relatif) au lieu de `data.url` (absolue),
de sorte que les futurs uploads stockent des paths relatifs resolus a l'affichage.
**Amelioration backend suggeree (non appliquee)** : MediaController devrait cesser de hardcoder
`10.0.2.2` et utiliser le context de la requete ou retourner un path relatif.
Ne bloque pas le frontend — le frontend est autonome grace a resolveMediaUrl.

**B21** - 6 URLs de profil fausses dans profiles.ts -- FAIT
**Fichier** : `src/api/profiles.ts:15-31`
**Bug** : Toutes les URLs utilisaient `/api/profiles/...` alors que le backend
(SocialController) sert les profils sur `/api/social/coiffeurs/{id}/profile`,
`/api/social/clients/{id}/profile`, `/api/social/owners/{id}/profile`,
`/api/social/salons/{id}/profile`. 6 fonctions corrigees (3 GET + 2 PUT + 1 GET salon).

**B22** - Consolidation patron modal : stopPropagation sur les cartes internes -- FAIT
**Bug** : Les modals utilisant un Pressable overlay + TouchableOpacity activeOpacity={1}
comme carte enfant laissaient les taps remonter a l'overlay sur React Native Web,
fermant le modal avant l'execution de l'action. Fix profil (7.5) corrigeait un seul ecran.
**Correction** : Remplacement de TouchableOpacity activeOpacity={1} par
Pressable onPress={(e) => e.stopPropagation()} sur la carte interne de chaque modal.
**Ecrans corriges** :
- `bookings.tsx` : dialog annulation reservation
- `social.tsx` : dialog collection (+ import Pressable ajoute)
- `index.tsx` : dialog filtre ville (+ import Pressable ajoute)
- `PostCard.tsx` : modal menu options + modal reactions (View → Pressable)
- `profile.tsx` : deja corrige (reference)

#### Decoupage valide (diagnostic 2026-06-10, KMP vs RN)

Ecrans deja complets (retires du perimetre) : TrendingScreen, CommentsScreen.
Aucun gap API bloquant : tous les endpoints necessaires existent dans src/api/.
React Query reporte a l'etape 11 : on continue en useState/useEffect.

**7.1 - BookingScreen** (`app/booking/new.tsx`) -- FAIT
- [x] Staff selection par specialite (step 1, API `getStaffBySpecialty` + option "N'importe qui")
- [x] Available slots API (creneaux hardcodes remplaces par `getAvailableSlots`, avec loading + empty state)
- [x] Champ notes/demandes speciales (step summary, lie a `CreateBookingRequest.notesClient`)
- [x] Ecran succes avec actions ("Voir ma reservation" + "Retour a l'accueil", remplace Alert)
- [x] Gestion d'erreur handleBook (corrige apres review : erreur affichee dans errorCard sur
  step summary, remplace le setNotes hack qui n'affichait rien. Etat bookingError + errorContainer.)
Flux complet: Service -> Staff -> Date/Time (API) -> Summary+Notes -> Success
Pattern theming inline respecte (1 design-fixed: shadowColor). 0 hex non marque.
Timezone: le creneau selectionne est envoye tel quel (slot.datetime = LocalDateTime backend,
ex: "2026-06-15T10:00:00") comme bookingDatetime dans createBooking. Pas de decomposition
HH:mm ni de recomposition via new Date().setHours().toISOString(). Le backend parse le meme
LocalDateTime sans conversion de fuseau. Decalage horaire elimine.

**7.2 - HomeScreen** (`app/(tabs)/index.tsx`) -- FAIT
- [x] Category filtering fonctionnel (chip "Tous" + 5 categories, appel `getSalons({ category, city })`
  ou `getTrendingSalons` si aucun filtre. useEffect avec flag ignore sur changement de categorie.)
- [x] City filter dialog (Modal avec TextInput, chip ville sous les categories, bouton clear)
- [x] Empty/Error states (reutilise EmptyState + ErrorState de src/components/lists/, etat hasError)
- [x] FAB "Creer mon salon" pour salon_owner quand vide (via EmptyState actionLabel/onAction,
  conditionne sur user.userType === 'salon_owner')
- [ ] Nearby filter GPS — NON IMPLEMENTE. Le KMP hardcode Paris (48.8566, 2.3522), pas de vraie
  geoloc. Pas reproduit. A implementer avec expo-location si necessaire, hors scope 7.2.
Egalement corrige : rating/reviewCount hardcodes (4.8/214) remplaces par item.averageRating/item.reviewCount.
Pattern theming inline respecte. Toutes couleurs brutes sont design-fixed (banner, shadows, dialog overlay).
Refactor post-review: deux useEffect dupliques fusionnes en un seul (deps: selectedCategory,
cityFilter, refreshTick). Fonction onRefresh morte supprimee. Une seule logique de chargement.

**7.3 - SocialFeedScreen** (`app/(tabs)/social.tsx`) -- FAIT
- [x] PostType filter chips (6 filtres: Tous, Avant/Apres, Realisation, Tendance, Conseil, Inspiration.
  Filtrage client-side sur le contenu pagine. Visible uniquement sur tab "Tous".)
- [ ] Onglet Nearby (GPS) — NON IMPLEMENTE. expo-location non installe dans package.json.
  Pas de hardcode Paris. A implementer avec expo-location si necessaire, hors scope 7.3.
- [x] Search bar avec suggestions (debounced 300ms, `unifiedSearch`, flag ignore dans cleanup.
  Resultats groupes par type: posts, salons, utilisateurs. Navigation sur tap.)
- [ ] Comment preview inline — NON IMPLEMENTE. Le KMP charge getCommentsByPost par post visible
  dans la FlatList (N appels simultanes). Cout reseau excessif pour un gain cosmétique.
  A implementer en etape 11 (performance/cache) si souhaite.
- [x] Reaction emoji picker (6 types: LIKE, LOVE, WOW, INSPIRANT, MAGNIFIQUE, BRAVO.
  Popup ancre en position:absolute au-dessus du bouton like, dans le PostCard.
  Declenche par long-press sur like. Fermeture au tap en dehors. Appel `addReaction`.)
- [x] Post options menu (dropdown ancre en position:absolute a droite du bouton "...",
  dans le PostCard. Supprimer (own post), Voir le post, Signaler. Fermeture au tap en dehors
  via Pressable absoluteFill. Suppression optimiste via `deletePost`.)
- [x] Collection save dialog (Modal avec liste des collections utilisateur via
  `getCollectionsByUser`. Tap sur collection → `addPostToCollection`.)
- [ ] External share — NON IMPLEMENTE (KMP inacheve, pas de ShareSheet natif).
- [ ] Full-screen image viewer — NON IMPLEMENTE (P2, cosmétique).
Egalement corrige : catch muet remplace par ErrorState avec retry.
Pattern theming inline respecte. 1 design-fixed (dialog overlay).

**7.4 - MyBookingsScreen** (`app/(tabs)/bookings.tsx`) -- FAIT
- [x] Filtre par statut (Toutes/A venir/Passees) avec compteurs. Filtrage client-side sur
  la liste complete (B14 confirme pas de troncature). A venir = !isPast && !CANCELLED && !COMPLETED.
  Passees = isPast || COMPLETED || CANCELLED.
- [x] Cancel booking avec Modal de confirmation (pattern coherent avec 7.2/7.3).
  Appel `cancelBooking` (B11 soft delete). Mise a jour optimiste du statut dans la liste.
- [x] "Laisser un avis" par booking COMPLETED. Bouton affiche sur tout booking COMPLETED,
  navigation → `/create-review?salonId=&salonName=&bookingId=&serviceName=`. Pas de
  verification N+1 `hasReviewForBooking` au chargement (meme objection N+1 que comment
  preview 7.3 — 40 bookings = 40 requetes). Le doublon d'avis est gere au tap par le
  backend (rejet 409 si avis existe deja). Dependance backend suggeree : ajouter champ
  `hasReview: Boolean` dans BookingResponse pour eviter le bouton quand inutile, sans N+1.
Egalement corrige : catch muet → ErrorState avec retry. EmptyState avec message contextuel
par filtre. LoadingState au chargement initial.
Pattern theming inline respecte. 1 design-fixed (modal overlay).

**7.5 - ProfileScreen** (`app/(tabs)/profile.tsx`) -- FAIT
- [x] Avatar upload avec preview et save. Tap → expo-image-picker (meme mecanisme que
  create-post), crop carre 1:1. Preview inline avec boutons Annuler/Enregistrer.
  Upload via `mediaApi.uploadImage` (B19, retourne path) + `usersApi.updateUserAvatar`
  (B9, PATCH). Image affichee via `resolveMediaUrl` (B20).
- [x] Stats dynamiques par UserType. 1 seul appel API par role via les profils enrichis
  backend (0 N+1, compteurs pre-calcules) :
  - client: `getClientProfile` → bookingsCount, collectionsCount, postsCount
  - salon_owner: `getSalonOwnerProfile` → salonsCount, followersCount, postsCount
  - hairstylist: `getCoiffeurProfile` → followersCount, postsCount, totalLikes
  Annulation requete obsolete via flag ignore.
Egalement corrige : deconnexion migree de Alert.alert vers Modal de confirmation (coherent
avec 7.4). Decouverte et correction de B21 (6 URLs profil fausses dans profiles.ts).
Pattern theming inline respecte. 1 design-fixed (modal overlay).

**7.6 - CreatePostScreen** (`app/create-post.tsx`) -- FAIT
- [x] @mention suggestions. Detection du token actif a la position du curseur via
  onSelectionChange + regex /@(\w*)$/. Debounce 300ms + flag ignore. Appel `searchUsers`.
  Liste inline avec avatar (resolveMediaUrl), nom, email. Tap → insertion du display name
  dans le texte + enregistrement de l'id dans `mentionedTags` (CreateTagRequest[]).
  Les tags sont envoyes dans `createPost({ tags })` pour que le backend enregistre
  les mentions (taggedType: "user", taggedId: uuid). Le texte @NomPrenom est decoratif ;
  la resolution/notification passe par le champ `tags` du DTO.
  Reconciliation a la publication : seuls les tags dont le @displayName est encore present
  dans le contenu sont envoyes. Si l'utilisateur efface la mention du texte, le tag est
  ecarte. Le displayName est stocke dans _displayName a cote de l'id pour le filtrage.
  Limitation connue : deux utilisateurs avec le meme displayName produiraient un faux
  positif (tag conserve pour le mauvais utilisateur). Acceptable et preferable au bug
  inverse (notifier un absent).
- [x] #hashtag suggestions. Meme logique avec regex /#(\w*)$/, appel `suggestHashtags`.
  Liste inline avec # colore, nom, usageCount. Tap → insertion du hashtag.
- [x] Compteur de caracteres. Limite 5000 (confirmee dans CreatePostRequest.validate(),
  SocialDto.kt:54). Affiche X / 5000. Alerte visuelle : normal → warning (>90%) → error
  (>100%). Bouton Publier desactive si depassement.
Logique de remplacement : tokenStart = cursorPos - token.length, before + prefix+value+" " + after.
Fallback curseur : si onSelectionChange ne fire pas, cursorPos = content.length.
Limitation connue : edition en milieu de texte depend de onSelectionChange. Sur les navigateurs
ou cet evenement ne fire pas, la detection de token revient en fin de texte (fallback).
Hashtags : name sans # en base (confirme : balayage, bob, brun), prefixe # ajoute par
insertHashtag. Pas de double #.
Pattern theming inline respecte. Aucune regression des features existantes (type, visibilite,
images, avant/apres, upload).

**7.7 - SalonDetailScreen** (`app/salon/[id].tsx`) -- FAIT
- [x] Cover photo upload (owner only). Tap cover → expo-image-picker → uploadImage (path)
  + updateSalonCoverPhoto. Badge camera en bas a droite si owner. Loading indicator pendant upload.
- [x] Staff tab visible owner-only. Tab Equipe filtre par `isOwner` (user.id === salon.ownerId).
- [x] Posts tab. Charge `getPostsBySalon` (B16 corrige) au tap sur l'onglet Posts.
  Affiche les posts via PostCard. Chargement paresseux (pas de N+1, 1 appel pagine).
- [x] Queue leave. Bouton "Quitter" dans le banner queue si l'utilisateur est en file
  (isInQueue = entry.clientId === user.id && status === WAITING). Appel `leaveQueue`.
- [x] Boutons owner dans la floating bar. Owner voit "Reservations" (→ /owner-bookings?salonId=)
  + "File d'attente" (→ /queue-management?salonId=) au lieu de "Suivre" + "Reserver".
Aucune regression des onglets Services/Avis/Info et de la barre existante.
Pattern theming inline respecte. Toutes couleurs brutes design-fixed.

**7.8 - LoginScreen + RegisterScreen** (`app/(auth)/login.tsx`, `app/(auth)/register.tsx`) -- FAIT

Features fonctionnelles :
- [x] Login: detection 403 + message "verifiee" → redirect vers email-verification.
  Lie a D1 : la detection frontend fonctionne, mais le flux email complet reste PARTIEL
  (clic email GET → 405, resend inutilisable sans auth).
- [x] Login: messages d'erreur specifiques affiches inline (pas Alert) :
  401 → "Email ou mot de passe incorrect", 403 → "Compte desactive",
  403+verifiee → redirect, 429 → "Trop de tentatives".
- [x] Login: auto-navigation succes (2s delay + card succes verte).
- [x] Register: confirmation password + indicateur de non-correspondance en temps reel.
- [x] Register: validation email format (@ et .) avec check icon vert.

Re-skin design v2 (extension, tracee separement) :
- [x] Register: hero compact 150px avec gradient prune + marque F glassmorphism + "Frollot"
- [x] Register: cartes de type de compte (3 colonnes, icone cercle, label, sous-label,
  check_circle si selectionne, border 2px primary)
- [x] Register: champs avec labels au-dessus + icones (badge, mail, lock) + fond surface
- [x] Register: indicateur de force du mot de passe (4 barres, visuel uniquement, ne bloque pas)
- [x] Register: texte legal + "Deja un compte ? Se connecter"

Ecran email-verification :
- [x] Bouton "Renvoyer" desactive (endpoint /api/users/me/resend-verification protege par
  auth + URL incorrecte dans auth.ts). Remplace par message informatif. Bouton retour login actif.
  Gap regroupe avec D1 dans le plan.

DETTE D1 etendue : resendVerificationEmail (mauvaise URL /resend-verification vs /me/resend-verification
+ protege par isAuthenticated) fait partie du flux email casse. A traiter avec D1.

Pattern theming inline respecte. Toutes couleurs brutes design-fixed (hero, brand, shadow).

**7.9 - QueueManagementScreen** (`app/queue-management.tsx`) -- FAIT
- [x] Optimistic updates sur call-next et remove (avec rollback).
  Call-next : premiere entree WAITING passe a CALLED immediatement.
  Remove : entree retiree de la liste immediatement.
  En cas d'echec : rollback au snapshot pre-action + erreur affichee inline.
  Poll suspendu pendant l'action (stopPoll/startPoll) pour eviter conflit
  entre mise a jour optimiste et poll concurrent.
Decouverte et correction de B23 :
- B23a : `removeQueueEntry` pointait vers DELETE `/queue/entries/{id}` (inexistant).
  Corrige vers POST `/queue/leave` avec `{ entryId }`.
- B23b : `LeaveQueueRequest` TS avait `clientId` au lieu de `entryId` (conforme au backend).
- Salon detail `leaveQueue` corrige pour trouver l'entryId de l'utilisateur dans la file.

**B24** - Salon detail : 3 corrections visuelles/UX -- FAIT
- B24a : coverBtn fond clair `rgba(255,255,255,0.85)` remplace par fond sombre `rgba(0,0,0,0.4)`,
  icones (back, share, heart) passees en `#FFFFFF` -- tous `// design-fixed`.
- B24b : GO_BACK guard ajoutee : `router.canGoBack() ? router.back() : router.replace('/(tabs)')`.
  Corrige le crash/noop quand la page est accedee par URL directe (web).
- B24c : OutlineButton "File d'attente" (owner) et "Suivre" (visitor) : `backgroundColor: colors.surface`
  retire. Le OutlineButton utilise son style par defaut (transparent + contour visible).

---

### ETAPE 8 : Creer les 14 ecrans manquants (revise 2026-06-10)

Inventaire croise (plan + KMP + liens morts RN) valide le 2026-06-10.
Corrections par rapport au decoupage initial (« 13 ecrans ») :
- **CollectionDetail RETIRE** : existe deja (`collections/[id].tsx`). Erreur du plan initial.
- **CollectionsList AJOUTE** : liste des collections, presente en KMP (`Collections(userId)`,
  CollectionsScreen.kt), oubliee du plan initial. RN n'a que le detail.
- Les **4 ecrans legaux comptes separement** (Terms, Privacy, HelpCenter, Contact).
- **PAIEMENT RETIRE DE L'ETAPE 8** : PaymentFlowScreen, PaymentHistoryScreen et
  PaymentMethodsScreen sont ABANDONNES pour l'instant (feature paiement abandonnee).
  NE PAS recreer ces ecrans, ni leurs routes, ni leurs entrees de menu.
  En particulier : l'entree « Moyens de paiement » du SettingsScreen KMP ne doit
  PAS etre reprise dans le Settings RN.

#### Famille A - API frontend deja prete (6 ecrans)

| # | Ecran | Route | Source KMP | Statut |
|---|-------|-------|-----------|--------|
| 1 | **SettingsScreen** | `settings/index.tsx` | SettingsScreen.kt | **FAIT** (2026-06-10) |
| 2 | **ReportScreen** | `report.tsx` | ReportScreen.kt | **FAIT** (2026-06-10) |
| 3 | **ArchivesScreen** | `archives/[userId].tsx` | ArchivesScreen.kt | **FAIT** (2026-06-10) |
| 4 | **ChangeEmailScreen** | `settings/change-email.tsx` | ChangeEmailScreen.kt | **FAIT** (2026-06-11, S4) |
| 5 | **SalonPostsScreen** | `salon/[id]/posts.tsx` | SalonPostsScreen.kt | A faire (seul restant famille A) |
| 6 | **CollectionsListScreen** | `collections/user/[userId].tsx` | CollectionsScreen.kt | **FAIT** (2026-06-11, S5 + B26) |

#### Famille B - Ecrans statiques (4 ecrans)

Contenu legal genere comme modele, a faire valider plus tard.

| # | Ecran | Route | Statut |
|---|-------|-------|--------|
| 7 | **TermsOfServiceScreen** | `settings/terms.tsx` | A faire |
| 8 | **PrivacyPolicyScreen** | `settings/privacy.tsx` | A faire |
| 9 | **HelpCenterScreen** | `settings/help.tsx` | A faire |
| 10 | **ContactSupportScreen** | `settings/contact.tsx` | A faire |

#### Famille C - API backend a creer d'abord (4 ecrans)

| # | Ecran | Route | API backend manquante | Statut |
|---|-------|-------|----------------------|--------|
| 11 | **SecuritySettingsScreen** | `settings/security.tsx` | 2FA, sessions actives | A faire |
| 12 | **ChangePhoneScreen** | `settings/change-phone.tsx` | OTP telephone | A faire |
| 13 | **BlockedUsersScreen** | `settings/blocked-users.tsx` | blocage utilisateurs | A faire |
| 14 | **RequestVerificationScreen** | `settings/verification.tsx` | demande de verification | A faire |

**Critere** : 14 ecrans presents, 0 lien mort silencieux (toute cible inexistante est
marquee « Bientot » et desactivee, jamais un tap qui ne fait rien).

#### Suivi Etape 8

- **S1 (2026-06-10) : SettingsScreen cree** (`settings/index.tsx`). Hub de navigation pure,
  aucun appel API. Sections : Compte / Confidentialite & securite / Apparence / Support / Legal.
  - Apparence fonctionnelle des le jour 1 : theme tri-state (Systeme/Clair/Sombre) + langue
    (5 langues) via `preferencesStore` (store local, pas d'API) — seule section du Settings
    KMP qui etait reellement fonctionnelle, conservee.
  - Les switches/dropdowns cosmetiques du KMP (notifications, confidentialite, reseau social,
    contenu & medias — etat local jamais persiste) ne sont PAS reproduits : c'etait du faux.
  - Entrees vers ecrans non crees (familles A/B/C) : ligne desactivee + badge « Bientot ».
    Activer une entree = renseigner sa `route` (1 ligne) quand l'ecran est cree.
  - Deconnexion : option B retenue apres validation. Modal extrait en composant partage
    `src/components/common/LogoutConfirmModal.tsx` (confirmation + logique logout +
    redirect login). Consomme par profile.tsx (Modal inline supprime, zero duplication)
    et par settings/index.tsx (bouton outline error en bas, au-dessus de la version).
    Patron modal B22 respecte (overlay Pressable + carte stopPropagation). S1 COMPLETE.
  - Entree « Profil » du KMP omise (circulaire : Settings est ouvert depuis le profil).
  - « Supprimer le compte » (API delete du KMP) reporte a SecuritySettingsScreen (famille C).

- **S2 (2026-06-10) : ReportScreen cree** (`report.tsx`). Ecran de signalement generique.
  - API verifiee de bout en bout : `moderationApi.reportContent()` -> POST `/api/social/reports`
    (ModerationController), DTO `CreateReportRequest` identique RN/backend. Enums
    `ReportedEntityType` (POST/COMMENT/USER/SALON) et `ReportReason`
    (INAPPROPRIE/SPAM/FAUX/COPYRIGHT/AUTRE) en correspondance exacte. Rien a corriger.
  - Parametres generiques `entityType` + `entityId` : pret pour signaler commentaire,
    utilisateur ou salon sans modification (seul le post est branche pour l'instant).
  - Contexte du post signale charge via `socialApi.getPostById()` (auteur, contenu, image) ;
    facultatif, l'ecran reste utilisable si le chargement echoue.
  - Motifs affiches en libelles lisibles + descriptions (jamais l'enum brut), details
    limites a 1000 caracteres avec compteur, erreur visible (carte errorContainer),
    etat d'envoi (spinner), ecran de confirmation de succes.
  - Lien mort n°2 resolu : `onReport={() => {}}` de social.tsx branche vers
    `/report?entityType=POST&entityId=...`. Menu options du post inchange (no regression).
  - **Test live valide (2026-06-10)** : signalement envoye sur un post reel du backend
    (POST /api/social/reports avec JWT) -> HTTP 201 Created, status PENDING, aucun 400/422.
    Apostrophes/accents verifies dans report.tsx. ReportScreen DEFINITIVEMENT VALIDE.
    (Utilisateur de test `olsenkampala+reporttest@gmail.com` + 1 report AUTRE laisses en
    base dev — a nettoyer si besoin.)

- **S3 (2026-06-10) : ArchivesScreen cree** (`archives/[userId].tsx`). Dernier lien mort
  actif du profil resolu.
  - **2 mensonges corriges a la source dans `src/api/social.ts`** (le backend etait pret,
    le client RN etait faux — jamais teste) :
    - `unarchivePost` : etait `POST /posts/{id}/unarchive` (n'existe pas) -> corrige en
      `DELETE /posts/{id}/archive` (SocialController.kt).
    - `getArchivedPosts` : etait `GET /posts/archived/{userId}` (n'existe pas) -> corrige en
      `GET /users/{userId}/archives` (owner-only, 403 sinon). `archivePost` etait correct.
  - Reutilisation de `PostCard` : ajout d'un prop optionnel `onUnarchive` (entree menu
    « Desarchiver » affichee uniquement si fourni — zero impact sur les autres ecrans).
  - Desarchiver ET supprimer passent par un modal de confirmation B22 (icone, texte
    explicite, erreur visible dans le modal, spinner pendant l'action).
  - Mise a jour optimiste : retrait immediat de la liste, **rollback** sur la liste
    precedente si le serveur refuse, message d'erreur affiche.
  - Pagination page/size 20 + infinite scroll + pull-to-refresh (fidele au KMP).
  - Etats partages `LoadingState` / `ErrorState` (avec retry) / `EmptyState`
    (« Aucune archive » / « Les posts que vous archivez apparaitront ici »).
  - Lien mort n°3 resolu : entree « Archives » de profile.tsx branchee vers
    `/archives/${user.id}`. Retour avec garde B24b (canGoBack ? back : replace tabs).
  - Note KMP : pas de suppression dediee dans les archives KMP ; ici le delete du menu
    PostCard (posts possedes) est conserve avec confirmation, coherent avec le feed.

- **B25 (2026-06-10) : `changeEmail` URL fausse + type incomplet** -- FAIT (corrige a la source,
  decouvert pendant le diagnostic preparatoire ChangeEmailScreen).
  - `src/api/auth.ts:50-51` : URL etait `PUT /api/security/change-email` (n'existe pas, 404
    garanti). Backend reel : `PUT /api/users/me/email` (UserController.kt:883). Corrige.
  - `src/types/user.ts` : `ChangeEmailResponse` declarait `{ message }` ; le backend renvoie
    `{ success: Boolean, message: String, newEmail: String? }` (SecurityDto.kt:177). Corrige.
  - `ChangeEmailRequest` (`{ newEmail, password }`) etait deja conforme.
  - SUSPICION non traitee (meme famille `/api/security/*` jamais testee, a verifier avant
    tout usage) : `changePassword`, `changePhone`, `deleteAccount`, `getActiveSessions`,
    `revokeSession`, `revokeAllOtherSessions` dans auth.ts pointent tous vers
    `/api/security/...` alors que le backend semble servir ces routes sous `/api/users/...`
    (ex. revoke sessions vu dans UserController:860). A auditer lors de la famille C
    (SecuritySettingsScreen).

- **S4-backend (2026-06-11) : flux "pending email" avec re-verification** -- FAIT, TESTE,
  VALIDE (prealable backend a l'ecran ChangeEmailScreen ; architecture validee par
  l'utilisateur : endpoint de confirmation DEDIE, flux d'inscription STRICTEMENT INTOUCHE).
  - **Migration `V040__add_pending_email_to_users.sql`** : colonne `pending_email VARCHAR(255) NULL`
    sur `users`. Appliquee (Flyway "now at version v040").
  - **`User.kt`** : champ `pendingEmail` ajoute. Les colonnes token existantes
    (`email_verification_token`, `email_verification_token_expires_at`) sont reutilisees
    (sans risque : les autres ecrivains de ces colonnes excluent les utilisateurs verifies).
  - **`UserService.kt`** : `changeEmail` (switch immediat, dangereux) REMPLACE par :
    - `requestEmailChange(userId, newEmail, password)` : mot de passe + unicite + MX check,
      stocke `pendingEmail` + token OTP 6 chiffres (24h), envoie a la NOUVELLE adresse.
      Email actif et `emailVerified` inchanges. Une nouvelle demande ECRASE proprement
      la precedente (pendingEmail + token) -> l'ancien code devient invalide.
    - `confirmEmailChange(userId, token)` : valide le token contre celui de l'utilisateur
      authentifie UNIQUEMENT (pas de lookup global -> zero collision avec l'inscription),
      switch email = pendingEmail, nettoie pending + token, `emailVerified` reste true.
  - **`UserController.kt`** : `PUT /me/email` reecrit (demande) + NOUVEL endpoint
    `POST /me/email/confirm` (authentifie, `ConfirmEmailChangeRequest{token}`).
    Erreurs : 400 (token invalide/expire, mauvais mdp, pas de demande), 409 (email pris).
    188 mappings au demarrage (187 avant = preuve endpoint enregistre).
  - **`EmailVerificationService.kt`** : ajout PUR (`sendEmailChangeVerification` +
    template `email/email-change_fr.html`). `sendVerificationEmail` / `verifyToken` /
    `resendVerificationEmail` (inscription) NON TOUCHES.
  - **DETTE i18n (Etape 9)** : template email-change FR uniquement (gros OTP, zero lien
    cliquable -> elimine la classe de bug GET/POST 405 de D1). A decliner en EN/ES/DE/AR
    avec les autres templates lors de l'Etape 9.
  - **Tests curl complets (2026-06-11, utilisateur dedie `olsenkampala+emailchange1@gmail.com`)** :
    - Negatifs : mauvais mdp -> 400 "Mot de passe incorrect" ; email pris
      (olsenfauldy@gmail.com) -> 409 ; confirm sans demande -> 400 "Aucune demande..." ;
      mauvais token -> 400 "Code de verification invalide". TOUS OK.
    - Demandes successives : 2 demandes d'affilee -> tokens T1=493621 puis T2=735151 ;
      confirm T1 -> 400 invalide (ecrase), confirm T2 -> 200. OK.
    - Bout en bout : avant confirm, login ancien email -> 200 ; apres confirm T2,
      login nouvel email (`+emailchange2`) -> 200 (preuve `emailVerified` true, car le
      login renvoie 403 sinon), login ancien email -> 401. OK.
    - Sessions : l'access token emis AVANT le changement fonctionne toujours apres
      (JWT subject = userId, verifie dans JwtTokenProvider.kt:168 avant codage). OK.
    - (Utilisateur de test `olsenkampala+emailchange2@gmail.com` laisse en base dev.)
  - RESTE A FAIRE (prochaine phase, apres validation) : ecran RN deux etapes
    `settings/change-email.tsx` (formulaire + saisie OTP), `confirmEmailChange` dans
    `src/api/auth.ts`, activation de l'entree Settings. -> FAIT, voir S4-frontend.

- **S4-frontend (2026-06-11) : ChangeEmailScreen cree** (`app/settings/change-email.tsx`),
  deux etapes sur le MEME composant (state `step: 'request' | 'confirm' | 'success'`).
  - **`src/api/auth.ts`** : ajout `confirmEmailChange(token)` -> `POST /api/users/me/email/confirm`
    (authentifie, body `{ token }`), reutilise le type `ChangeEmailResponse` (le backend renvoie
    le meme DTO). Verifie au passage : `changeEmail` = bien la version corrigee B25
    (`PUT /api/users/me/email`) et `ChangeEmailResponse{success,message,newEmail?}` conforme.
  - **Etape 1 (demande)** : email actuel en lecture seule (boite verrouillee, depuis `user.email`),
    nouvel email (regex format + difference avec l'actuel, case-insensitive), mot de passe via
    `PasswordTextField` partage. Erreurs backend affichees telles quelles dans une carte
    errorContainer (400 mdp incorrect, 409 email pris) — zero catch muet.
  - **Etape 2 (OTP)** : carte d'honnetete (tertiaryContainer) « le changement n'est PAS encore
    effectif, l'adresse actuelle reste active » + adresse de destination affichee. Champ code
    6 chiffres (filtre numerique, maxLength 6, gros style letter-spacing). Confirmer ->
    `confirmEmailChange` ; succes -> `setUser({...user, email: resp.newEmail})` dans l'authStore
    (l'app n'affiche plus l'ancien email), password remis a '' (hygiene), etat succes avec
    retour Settings. Erreurs code invalide/expire visibles.
  - **« Renvoyer le code »** : rappelle `changeEmail({newEmail, password})` — le backend ecrase
    proprement l'ancien token (teste S4-backend : T1 rejete, T2 accepte). N'utilise PAS
    `resendVerificationEmail` (casse, D1). Message de confirmation « l'ancien code n'est plus
    valide » apres renvoi.
  - **Gestion du mot de passe entre etapes (decision validee)** : garde en state LOCAL React
    le temps du flux uniquement — jamais persiste, jamais dans un store, meurt au demontage.
    Meme niveau d'exposition que la saisie elle-meme ; re-demander n'apporterait aucune
    securite reelle et serait punitif en UX.
  - **« Modifier l'adresse »** : retour etape 2 -> etape 1 avec newEmail + password preserves
    (pas de repartir de zero). Bouton retour header contextuel : etape 2 -> etape 1, sinon
    quitte avec garde B24b (fallback `/settings`).
  - **Settings active** : `settings/index.tsx` ligne « Changer l'email » -> `route:
    '/settings/change-email'` (badge « Bientot » retire automatiquement par le pattern RowDef).
  - Conventions respectees : theming inline strict (`useTheme()`), composants partages
    (TextField/PasswordTextField/PrimaryButton/TextButton), KeyboardAvoidingView,
    lisible clair/sombre (uniquement tokens de roles).
  - Verifications : `tsc --noEmit` -> zero erreur sur change-email.tsx / settings/index.tsx /
    auth.ts (erreurs restantes preexistantes, hors perimetre) ; route web
    `GET /settings/change-email` -> 200 (Metro compile).

- **B26 (2026-06-11) : les 9 URLs de `collectionsApi` etaient fausses** -- FAIT (corrige a la
  source dans `src/api/portfolios.ts`, decouvert pendant le diagnostic CollectionsListScreen).
  - Le backend sert les collections dans SocialController, base `/api/social` —
    `grep "api/collections"` dans tout le backend = ZERO resultat. Toutes les fonctions RN
    pointaient vers `/api/collections/...` (404 garanti) :
    - `getCollectionsByUser` : `/api/collections/user/{userId}` -> reel
      `GET /api/social/users/{userId}/collections?includePrivate=` (URL ET forme fausses —
      la fonction etait presumee saine car « deja utilisee en 7.3 », c'etait faux) ;
    - create/update/delete/getById/addPost/removePost/getPosts/getPublic : prefixe
      `/api/social` manquant partout. Tout corrige et verifie contre SocialController.kt.
  - Mensonge de type en plus : `CollectionPostResponse` RN declarait `{postId}` ; le backend
    embarque le post complet `{id, collectionId, post: PostResponse, orderIndex, addedAt}`
    (pas de N+1 necessaire). Corrige dans `src/types/portfolio.ts` + type de retour de
    `getCollectionPosts` (`PageResponse<CollectionPostResponse>`).
  - CONSEQUENCE RETROACTIVE : le dialog « sauvegarder dans une collection » de social.tsx (7.3)
    et les chargements de collections des profils client/owner etaient silencieusement
    casses (404) depuis le debut — gueris par cette correction a la source, MAIS le dialog 7.3
    consommait peut-etre l'ancienne forme : a re-tester (note pour la passe de verification).
  - **Tests curl (2026-06-11, user test emailchange2)** : GET liste -> 200 `[]` ;
    POST create -> 201 (id reel) ; GET byId -> 200 ; GET posts -> 200 (forme Page
    `content`/`last` conforme) ; DELETE -> 200. Tous les endpoints corriges valides en live.

- **S5 (2026-06-11) : CollectionsListScreen cree** (`app/collections/user/[userId].tsx`).
  Ecran n°6 famille A — l'ecran oublie du plan initial (KMP Collections(userId)).
  - **Lien FAUX corrige** (pire qu'un lien mort) : profile.tsx:99 poussait
    `/collections/${user.id}` qui matchait `collections/[id].tsx` (le DETAIL) avec un userId
    a la place d'un collectionId. Corrige -> `/collections/user/${user.id}` (le segment
    statique `user/` prime sur `[id]` dans expo-router, pas de conflit de route).
  - **Detail fantome repare au passage** : `collections/[id].tsx` ne chargeait JAMAIS rien
    (commentaire mensonger « pas de getById dans notre API » — il existe), posts toujours
    vides, « Collection vide » a vie. Repare a minima : chargement reel
    (`getCollectionById` + `getCollectionPosts` en parallele), header collection
    (nom/description/compteur), cartes posts (image via `resolveMediaUrl`, auteur, contenu,
    tap -> `/post/{id}`), pagination infinite scroll, etats Loading/Error(retry)/Empty
    partages, garde B24b (l'ancien back nu est remplace).
  - **Liste** (parite KMP) : cartes cover 80px (resolveMediaUrl, icone sinon), nom, badge
    categorie en tokens de roles (INSPIRATION=primary, PORTFOLIO=secondary,
    TENDANCE=tertiary, PERSONNEL=error — parite KMP), cadenas si privee, description
    2 lignes, compteur posts. `includePrivate` seulement si owner. Pull-to-refresh.
  - **Creation** (owner uniquement, FAB + action de l'EmptyState) : modal B22 — nom
    (requis, maxLength 200), description (maxLength 2000), chips categorie, switch public.
    Insertion en tete de liste au succes. CHOIX assume : champ « URL de cover » en texte
    brut du KMP OMIS (saisie d'URL brute = anti-UX, pas un vrai picker) — la cover
    s'affiche quand elle existe.
  - **Suppression** (owner uniquement) : modal B22 de confirmation + mise a jour optimiste
    avec rollback si le serveur refuse, erreur visible dans le modal. (Mieux que le KMP qui
    supprimait sans confirmation.)
  - **Edition** : NON implementee — parite KMP (le KMP avait un `onEdit` TODO vide).
    A reconsiderer si demande.
  - Erreurs backend affichees telles quelles (`error?.response?.data?.message`), zero catch
    muet, flag `ignore` dans les useEffect.
  - Verifications : `tsc --noEmit` -> zero erreur sur les fichiers touches (portfolios.ts,
    portfolio.ts, profile.tsx ligne route, les 2 ecrans collections) ; routes web
    `/collections/user/x` et `/collections/x` -> 200 (Metro compile).

- **B27 + retrait reactions (2026-06-11) : likes erratiques gueris a la racine** -- FAIT.
  Symptome utilisateur : compteurs de likes incoherents (retombent a 0/1, sautent de +1/+2).
  DB verifiee saine (post_likes + compteur + reconciliation auto SocialService.kt:1186-1192) —
  le frontend mentait. Trois bugs cumules :
  1. **Mapping de champs faux (racine)** : `PostResponse` TS declarait `isLiked`, `isFavorite`,
     `isShared`, `authorType`, `userReaction`, `salonId`, `salonName`, `isArchived` — AUCUN
     de ces noms n'existe dans le JSON reel (verifie live sur GET /posts/trending). Les vrais
     noms : `isLikedByCurrentUser`, `isFavoritedByCurrentUser`, `isSharedByCurrentUser`,
     `authorUserType`, `isPinned`. Consequence : coeurs jamais hydrates au chargement.
     Corrige dans `src/types/social.ts` + renommages partout (PostCard, social.tsx,
     post/[id].tsx, archives/[userId].tsx, trending.tsx). Bonus : le badge Salon/Coiffeur
     du PostCard redevient visible (authorType etait toujours undefined).
  2. **Like = source de verite serveur** : les ecrans jetaient la reponse de toggleLike et
     appliquaient un ±1 local aveugle (inversions de toggle, ecarts de 2 au refresh).
     Remplace par une FUSION SELECTIVE de la reponse : `{ ...p, isLikedByCurrentUser,
     likesCount }` uniquement — la reponse de toggleLike est PARTIELLE (pas de media,
     sharesCount=0 hardcode), remplacer le post entier effacait l'image (bug existant
     dans post/[id].tsx et trending.tsx, corrige au passage).
  3. **Reactions emoji retirees (decision utilisateur, style Instagram/TikTok)** : les
     reactions cote serveur (table post_reactions) ne touchent JAMAIS likesCount, mais le
     frontend simulait +1 like par reaction (likes fantomes). Supprime proprement :
     enum `ReactionType`, fonctions API `addReaction`/`removeReaction`/`getReactionsByPost`,
     prop `onReaction`, popup/modal de reactions, `onLongPress` du bouton like, handlers
     `handleReaction`, styles orphelins. Le menu « ... » du PostCard est intact.
     **Endpoints backend DORMANTS (non supprimes, plus aucun appelant frontend)** :
     `POST/DELETE/GET /api/social/posts/{postId}/reactions` (SocialController).
  - Catch muet du like branche sur un Toast visible (social.tsx, post/[id].tsx,
    archives/[userId].tsx — Toast ajoute la ou absent). PERIMETRE STRICT : seul le like ;
    les autres catch muets, le partage mort, B28 (URL favoris fausse), B29 (URLs pin
    fausses), l'archivage sans bouton et la dualite signet favori/collection restent
    a traiter dans des increments dedies.
  - NOTE B28 anticipee : `handleFavorite` de post/[id].tsx fait encore `setPost(updated)`
    (efface les media) — a corriger avec B28.
  - Verifications : `tsc --noEmit` -> zero erreur sur les fichiers touches ; JSON backend
    re-verifie live (cles conformes) ; zero reference morte aux anciens noms (grep).

- **B28 (2026-06-11) : ecran Favoris casse (URL fausse + 404 avale) gueri** -- FAIT.
  - **URL corrigee a la source** (`src/api/social.ts`, seul appelant : favorites/[userId].tsx) :
    le front appelait `GET /posts/favorites/{userId}` qui N'EXISTE PAS (verifie live : 500,
    jamais une page de favoris) ; backend reel `GET /users/{userId}/favorites`
    (SocialController.kt:458, verifie live : 401 sans token = route existante et protegee).
    Commentaire « backend reel » ajoute contre les regressions. Reponse backend COMPLETE
    (Page de PostResponse avec media/tags/hashtags, SocialService.kt:1287-1321) — l'ecran
    a tout ce qu'il faut une fois l'URL juste. Contrainte owner-only : 403 si
    userId != utilisateur authentifie (seul lien d'entree : profile.tsx -> soi-meme).
  - **favorites/[userId].tsx** : le catch muet de `loadFavorites` rendait l'ecran
    « Aucun favori » A VIE (404 avale, aucun ErrorState — l'ecran n'en avait meme pas).
    Remplace par `ErrorState` partage avec retry, et message honnete dedie en cas de 403
    (« favoris prives ») plutot qu'une erreur generique trompeuse. `handleUnfavorite` passe
    en mise a jour optimiste avec ROLLBACK VISIBLE (le post revient dans la liste) + Toast
    d'erreur si le serveur refuse (plus d'echec invisible sur l'action principale de l'ecran).
  - **post/[id].tsx `handleFavorite`** : meme bug que B27 — `setPost(updated)` avec la
    reponse PARTIELLE de toggleFavorite (pas de media, SocialService.kt:1272-1280) effacait
    l'image au tap signet. Corrige en fusion selective `{ ...prev, isFavoritedByCurrentUser }`
    (pas de compteur cote favoris) + catch branche sur le Toast existant (B27).
  - PERIMETRE STRICT respecte : dualite signet (collections dans le fil / favoris au detail),
    B29 (pin) et partage mort non touches — increments dedies a venir.
  - Verifications : `tsc --noEmit` -> zero erreur sur les fichiers touches ; route web
    `/favorites/x` -> 200 (Metro compile) ; ancienne URL backend 500 vs nouvelle 401
    (protegee, donc existante) — forme exacte a confirmer en test manuel authentifie.

- **B30 (2026-06-11) : dualite signet resolue — signet = favori PARTOUT** -- FAIT.
  DECISION UTILISATEUR : meme icone, meme comportement que le detail du post (modele
  Instagram : save rapide unique + collections comme organisation a cote). Avant : le
  signet du fil ouvrait le dialog collections, celui du detail togglait le favori, celui
  des archives etait MORT (onBookmark jamais passe) — trois comportements, zero coherence.
  - **PostCard.tsx** : nouvelle prop optionnelle `onSaveToCollection` -> entree de menu
    « ... » « Ajouter a une collection » (meme pattern conditionnel que onUnarchive).
    Signet teinte `colors.tertiary` quand actif (avant : changeait de forme mais restait
    gris — le detail teintait deja, aligne).
  - **social.tsx** : signet -> `handleBookmark` = toggleFavorite avec fusion selective
    (`isFavoritedByCurrentUser` seul, reponse partielle, pas de compteur favoris) + toast
    erreur. Le dialog collections N'EST PAS supprime : redeclenche depuis le menu « ... »
    (`onSaveToCollection` -> openCollectionDialog), code du dialog reutilise tel quel.
  - **archives/[userId].tsx** : signet mort branche (meme handleBookmark + toast) —
    archiver son post n'empeche pas de le garder en favori.
  - Hors perimetre, non touches : detail du post (la reference), Tendances (cartes maison
    sans signet — a revoir avec le partage mort), B29 pin, bouton « Archiver » manquant,
    fusion backend favoris/collections (non necessaire pour l'UX).
  - Rappel backend : les collections n'exposent RIEN dans PostResponse (pas d'etat
    hydratable) ; les favoris exposent `isFavoritedByCurrentUser` — seule option
    permettant une icone pre-remplie fiable, d'ou la decision.
  - Verifications : `tsc --noEmit` -> zero erreur sur les fichiers touches ; routes web
    `/social` et `/archives/x` -> 200 (Metro compile).

- **B31 (2026-06-11) : partage = partage EXTERNE natif partout** -- FAIT.
  DECISION UTILISATEUR : le bouton partage ouvre le menu natif du systeme (WhatsApp, SMS,
  mail, copier...) via l'API `Share` de React Native. Le repartage interne facon Facebook
  est REPORTE en chantier dedie (voir « REPARTAGE-PROFIL A VENIR » ci-dessous).
  Avant : icone morte dans le fil et les archives (onShare jamais passe), toggle interne
  share/unshare au detail (incoherent et catch muet).
  - **Helper unique `src/utils/share.ts`** : `sharePostExternally(post)` — payload TEXTE
    SEUL : « extrait (200 car. max) » + auteur + « sur Frollot ». PAS d'URL fabriquee :
    aucune page publique de post ni deep link n'existe (regle anti-faux-liens) ; le jour
    ou une URL publique existera, on l'ajoute a cet unique endroit. +
    `isShareCancellation()` : l'annulation utilisateur n'est PAS une erreur (AbortError
    sur web via navigator.share ; resolution silencieuse sur natif) -> silencieux normal ;
    seul un VRAI echec produit un toast visible (web sans navigator.share par ex.).
  - **Branche partout** : social.tsx + archives/[userId].tsx (`onShare` enfin passe au
    PostCard) ; post/[id].tsx : le toggle interne est REMPLACE par le partage externe
    (comportement identique aux trois endroits).
  - **Compteur masque (choix anti-compteur-casse)** : `sharesCount` mesure les partages
    internes, que le bouton ne declenche plus jamais — un chiffre fige a cote de l'action
    aurait l'air casse. Retire du PostCard ; teinte `isSharedByCurrentUser` retiree au
    detail (etat interne non pilote par l'action). Les deux reviendront avec le
    repartage-profil, alimentes par ce qu'ils mesurent vraiment.
  - **Bug de body corrige a la source** (`src/api/social.ts`) : sharePost envoyait
    `{ content }` alors que le backend attend `{ sharedContent }` (SharePostRequest,
    SocialDto.kt:88) — le commentaire de partage etait silencieusement perdu.
  - **API DORMANTES (non supprimees, plus aucun appelant UI)** : `sharePost`/`unsharePost`
    frontend + endpoints backend `POST/DELETE /posts/{postId}/share`,
    `GET /posts/{postId}/shared`, `GET /posts/{postId}/shares` — reserves au
    repartage-profil. NB backend : sharePost REJETTE si deja partage (pas un toggle),
    compteur avec reconciliation auto (sain, verifie SocialService.kt:1510-1655).
  - Verifications : `tsc --noEmit` -> zero erreur sur les fichiers touches ; routes web
    `/social`, `/archives/x`, `/post/x` -> 200 (Metro compile).

- **REPARTAGE-PROFIL — A VENIR (chantier dedie, ne pas perdre)** :
  republication d'un post sur le profil de l'utilisateur facon Facebook, avec commentaire
  optionnel (le backend stocke deja `sharedContent` <= 500 car. dans `post_shares`),
  visible par ses abonnes. Necessite son propre diagnostic puis :
  1. Backend : brancher `PostShareRepository.findPostsSharedByUser` (requete existante,
     aujourd'hui MORTE — aucun appelant) sur un endpoint « posts repartages par X » ;
     injecter les reposts dans le fil des abonnes et sur le profil (aujourd'hui AUCUN
     feed ne rediffuse les partages — partager n'a aucun effet visible hormis le compteur).
  2. Frontend : action de repartage (avec dialog commentaire, parite KMP « Partager dans
     l'app »), affichage des reposts (carte « X a repartage » + commentaire), reactivation
     du compteur sharesCount et de la teinte isSharedByCurrentUser masques en B31,
     reutilisation des API dormantes sharePost/unsharePost (body deja corrige).
  3. Les groupes : encore plus tard, apres le repartage-profil.

- **CARTOGRAPHIE DES ACTIONS D'UN POST — REFERENCE FIGEE (validee utilisateur, 2026-06-11)** :
  ne pas devier sans consultation. Vaut pour TOUS les ecrans a posts (fil, detail, archives,
  futur SalonPosts).
  - **Barre d'engagement** (toujours visible, 4 boutons, NE PAS en ajouter) :
    j'aime · commenter · partager (externe natif, B31) · favori (signet, B30).
  - **Menu « ... »** (tiroir) : voir le post · ajouter a une collection · archiver
    (posts possedes uniquement, B32) · epingler (A VENIR, B29) · signaler · supprimer
    (posts possedes uniquement). Le futur repartage-profil ira aussi dans ce menu.

- **B32 (2026-06-11) : entree « Archiver » ajoutee au menu « ... »** -- FAIT.
  L'ecran Archives (S3) etait inalimentable : aucun bouton n'archivait nulle part.
  - **API confirmee** : `POST /posts/{postId}/archive` (front correct depuis le debut,
    contrairement a unarchive/getArchives corriges en S3). Reponse partielle (pas de
    media) — sans effet : on retire le post du fil, on ne le remplace pas. Idempotent.
  - **PostCard.tsx** : prop optionnelle `onArchive`, entree visible si fournie ET
    `isOwn` (meme condition que Supprimer : `post.authorId === currentUserId`) —
    n'apparait PAS sur les posts d'autrui, conformement a la cartographie.
  - **social.tsx** : retrait optimiste du fil + toast succes « Post archive » ;
    rollback visible + toast erreur si le serveur refuse. PAS de modal de confirmation
    (action reversible — Desarchiver existe dans Archives — et idempotente) ; pas de
    « Annuler » dans le toast (le composant partage n'a pas de bouton d'action).
  - **DECOUVERTES BACKEND a traiter plus tard (non bloquantes, notees ici)** :
    1. L'archivage est PAR UTILISATEUR (table post_archives user+post), pas par auteur :
       archiver son post le masque de SON fil (getFeed filtre, SocialService.kt:549-554)
       mais PAS du fil des autres — pas un archivage facon Facebook. Chantier backend si
       on veut le masquage global.
    2. Le filtre archives n'existe QUE dans getFeed : Tendances et fil Suivis ne
       l'appliquent pas (un post archive peut y reapparaitre, meme pour l'archiveur).
    3. AUCUNE verification d'ownership cote backend (n'importe qui peut archiver
       n'importe quel post = mute personnel de fait) — l'UI restreint aux posts possedes ;
       porte ouverte a une future fonction « Masquer ce post » sur les posts d'autrui.
  - **Detail du post NON traite (choix assume)** : post/[id].tsx n'a AUCUN menu « ... »
    (ni Supprimer ni Signaler non plus — ecran en retard sur la cartographie). A aligner
    dans un increment dedie « menu ... au detail » plutot que d'y greffer une seule entree.
  - Verifications : `tsc --noEmit` -> zero erreur sur les fichiers touches ; `/social`
    -> 200 (Metro compile).

---

### ETAPE 9 : Completer les traductions i18n - 5 langues (2 jours)

**Objectif** : 867 cles dans les 5 langues (actuellement 213 max en EN/FR).

#### 9.1 - Extraire les cles du KMP (automatise)
Ecrire un script Node.js qui parse les fichiers `strings_*.kt` et genere les JSON :

```
Source KMP:
  strings_en.kt (867 cles) -> en.json
  strings_fr.kt (885 cles) -> fr.json
  strings_es.kt (867 cles) -> es.json
  strings_de.kt (867 cles) -> de.json
  strings_ar.kt (867 cles) -> ar.json
```

#### 9.2 - Sections a ajouter (toutes manquantes en RN)

| Section | Cles approx |
|---------|-------------|
| settings | 116 |
| payment | 100 |
| booking (detail) | 100 |
| salon (management) | 80 |
| security | 70 |
| profile | 60 |
| social (feed) | 50 |
| components | 100 |
| enums | 40 |
| help | 30 |
| portfolio | 30 |
| reviews | 20 |
| verification | 20 |
| **TOTAL manquant** | **~816** |

#### 9.3 - Verification
Script de validation : chaque cle presente en FR doit exister dans les 4 autres langues.
```bash
node scripts/check-i18n-keys.js
# Output: OK (0 missing) ou FAIL (liste des cles manquantes)
```

**Critere** : 867/867 cles presentes dans les 5 langues. 0 cle manquante.

---

### ETAPE 10 : Navigation avancee (1.5 jours)

#### 10.1 - AppDrawer (role-based)
Recreer le drawer KMP avec sections :
- Marketplace (Home)
- Social (Feed, Trending)
- Rendez-vous (Mes RDV)
- Compte (Profil, Notifications, Favoris, Archives, Collections)
- Salon Owner : Mes Salons, Nouveau Salon, Creer Post, Gestion RDV
- Hairstylist : Mes Portfolios, Nouveau Portfolio
- Admin : Dashboard, Users
- Global : Parametres, Aide, Deconnexion

#### 10.2 - Auth guards
- Redirect vers login si non authentifie
- Redirect vers home si deja authentifie et sur login/register
- Session restoration au lancement (initializeFromStorage)
- Language sync avec backend apres login

#### 10.3 - Deep links
- Payment return URL (Stripe checkout)
- Email verification link
- Password reset link
- Scheme: `frollot://`

**Critere** : Navigation complete, auth guards fonctionnels, deep links testables.

---

### ETAPE 11 : Gestion offline + Performance (1.5 jours)

#### 11.1 - Cache layer
- React Query (TanStack Query v5) pour le cache + sync
- staleTime et cacheTime par type de donnee
- Favoris/Archives en cache local (AsyncStorage)

#### 11.2 - Network monitoring
- Detecter online/offline
- Fallback sur cache quand offline
- Banner "Mode hors-ligne" visible

#### 11.3 - Performance
- FlashList au lieu de FlatList pour les listes longues
- Memoization des composants PostCard, SalonCard
- Image caching avec expo-image
- Lazy loading des ecrans

**Critere** : App utilisable offline (lecture). Scroll 60 FPS sur feed social.

---

### ETAPE 12 : Tests unitaires et d'integration (3 jours)

#### Jour 1 - Tests unitaires infrastructure (~80 tests)

| Module | Tests | Outil |
|--------|-------|-------|
| API client (interceptors, refresh) | 15 | Jest + MSW |
| Auth store (Zustand) | 10 | Jest |
| Preferences store | 5 | Jest |
| Types/validation (Zod schemas) | 20 | Jest |
| Utils (format, validation) | 15 | Jest |
| i18n (cles presentes) | 10 | Jest |
| Theme (tokens corrects) | 5 | Jest |

#### Jour 2 - Tests composants (~40 tests)

| Module | Tests | Outil |
|--------|-------|-------|
| PrimaryButton, SecondaryButton | 5 | React Native Testing Library |
| StandardTextField, PasswordTextField | 5 | RNTL |
| SalonCard, PostCard | 5 | RNTL |
| UserAvatar | 3 | RNTL |
| RatingBar | 3 | RNTL |
| ListEmpty/Loading/Error | 5 | RNTL |
| Toast | 3 | RNTL |
| BookingStepper | 5 | RNTL |
| ProfileHeaderCard | 3 | RNTL |
| AppDrawer | 3 | RNTL |

#### Jour 3 - Tests d'integration ecrans (~49 tests)

Un test par ecran : monte le composant, mock API, verifie rendu + interactions.

**Critere** : 150+ tests passent. Couverture > 60%.

---

### ETAPE 13 : Tests E2E - 12 scenarios (2 jours)

#### Jour 1 - Scenarios critiques (6 E2E)

| # | Scenario | Ecrans traverses |
|---|----------|------------------|
| E01 | Inscription complete | Register -> EmailVerification -> Login |
| E02 | Connexion + deconnexion | Login -> Home -> Logout -> Login |
| E03 | Restauration session | Kill app -> Reopen (auto-login) |
| E04 | Refresh token auto | Login -> attendre -> action (refresh transparent) |
| E05 | Recherche + detail salon | Home -> Search -> SalonDetail |
| E06 | Reservation complete | SalonDetail -> Booking -> Payment |

#### Jour 2 - Scenarios avances (6 E2E)

| # | Scenario | Ecrans traverses |
|---|----------|------------------|
| E07 | File d'attente | SalonDetail -> QueueManagement -> Join |
| E08 | Creation de post | SocialFeed -> CreatePost -> Feed |
| E09 | Interaction sociale | Feed -> Like + Comment + Share |
| E10 | Upload d'image | CreatePost -> pick image -> upload |
| E11 | Changement de langue | Settings -> FR -> ES -> verification |
| E12 | Mot de passe oublie | Login -> ForgotPassword -> ResetPassword |

**Outil** : Maestro ou Detox
**Critere** : 12/12 scenarios passent a 100%.

---

### ETAPE 14 : QA, polish, dark mode, RTL (2 jours)

#### 14.1 - Dark mode (0.5 jour)
- Tester les 49 ecrans en dark mode
- Corriger les couleurs hardcodees
- Verifier les contrastes WCAG AA

#### 14.2 - RTL Arabe (0.5 jour)
- Activer I18nManager.forceRTL pour l'arabe
- Tester les 49 ecrans en AR
- Corriger les layouts qui ne s'inversent pas

#### 14.3 - Polish general (1 jour)
- Animations transitions entre ecrans
- Splash screen correct
- Error boundaries (crash gracieux)
- Pull-to-refresh partout ou il existait en KMP
- Pagination fonctionnelle sur toutes les listes
- Upload d'image (camera + galerie)
- Deep links Stripe payment return
- Push notifications FCM (expo-notifications)

**Critere checklist zero regression** :
- [ ] 12/12 E2E passent
- [ ] 163/163 endpoints API repondent
- [ ] 49/49 ecrans rendent sans crash
- [ ] 5/5 langues completes (867 cles chacune)
- [ ] Dark mode sur tous les ecrans
- [ ] RTL arabe fonctionnel
- [ ] Upload image fonctionne
- [ ] Token refresh automatique
- [ ] Session restauree apres kill app

---

### ETAPE 15 : Build production + Beta (1.5 jours)

#### 15.1 - Configuration production
- Variables d'environnement (API URL prod)
- Sentry integration (crash reporting)
- Analytics (optional)

#### 15.2 - Builds
- Android : `eas build --platform android --profile production`
- iOS : `eas build --platform ios --profile production`
- Verifier taille APK < 50MB
- Verifier performance < 2s premier affichage

#### 15.3 - Distribution beta
- TestFlight (iOS)
- Google Play Internal Testing (Android)
- Feedback beta -> corrections -> release

#### 15.4 - Rollback
- Archiver la derniere version KMP (tag git)
- Backend ne change PAS -> compatible avec les deux apps
- Conservation app KMP pendant 3 mois minimum

---

## DEPENDANCES ENTRE ETAPES

```
ETAPE 1 (Bugs) ──────────> PREREQUIS POUR TOUT
    |
ETAPE 2 (Types) ─┐
ETAPE 3 (API)  ──┼──> ETAPE 4 (Composants) ──> ETAPE 6 (Design v2)
ETAPE 5 (Prefs)──┘                          ──> ETAPE 7 (Features)
                                            ──> ETAPE 8 (Ecrans manquants)

ETAPE 9 (i18n) ──> Peut etre fait en parallele de 6-7-8

ETAPE 6+7+8+9 terminés ──> ETAPE 10 (Navigation)
                        ──> ETAPE 11 (Offline/Perf)
                        ──> ETAPE 12 (Tests unit)

ETAPE 10+11+12 terminés ──> ETAPE 13 (E2E)
                         ──> ETAPE 14 (QA)

ETAPE 13+14 terminés ──> ETAPE 15 (Build/Beta)
```

## PARALLELISATION POSSIBLE

Si plusieurs developpeurs :
- Dev A : Etapes 2+3 (types + API) pendant que Dev B fait Etape 4 (composants)
- Dev A : Etape 7 (features ecrans) pendant que Dev B fait Etape 8 (ecrans manquants)
- Dev C : Etape 9 (i18n) en parallele des etapes 6-8
- Dev A : Etape 12 (tests) pendant que Dev B fait Etape 14 (polish)

---

## CRITERES GO/NO-GO POUR RELEASE

### GO (tous doivent etre remplis)
- [ ] 12/12 E2E passent
- [ ] 0 crash sur 1h d'utilisation continue
- [ ] 49/49 ecrans fonctionnels
- [ ] 867/867 cles i18n dans 5 langues
- [ ] Dark mode operationnel
- [ ] Token refresh fonctionne
- [ ] Upload image fonctionne
- [ ] Paiement Stripe fonctionne (sandbox)
- [ ] < 2s premier affichage
- [ ] 60 FPS scroll feed
- [ ] APK < 50MB

### NO-GO (bloque la release)
- Un scenario E2E echoue
- Un crash reproductible
- Token refresh ne fonctionne pas
- Paiement ne fonctionne pas
- > 5% cles i18n manquantes
- < 30 FPS scroll

---

## RESUME EXECUTIF

| Metrique | Valeur |
|----------|--------|
| Bugs critiques a corriger | 6 |
| Types a creer/completer | 6 nouveaux + 4 a completer |
| Endpoints API a ajouter | 24 (dans 4 nouveaux fichiers) |
| Composants a creer | 40 |
| Ecrans a creer | 13 |
| Ecrans a enrichir | 36 (180+ features) |
| Cles i18n a ajouter | ~3500 (5 langues x ~700 manquantes) |
| Tests a ecrire | 150+ unitaires + 49 integration + 12 E2E |
| Maquettes design v2 a appliquer | 5 ecrans |
| Estimation totale | ~31 jours dev |
