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
LOT B SOCIAL : Tags salon enrichis + affichage PostCard            ~0.5j [FAIT]
FIX PostCard : date relative + metaRow une ligne (ellipsis tag)          [FAIT]
LOT C BACKEND : publier au nom du salon (author_type+salonId, perm)      [FAIT]
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
- [x] Onglet Posts (navigation vers SalonPostsScreen) — « Voir tout » -> `/salon/{id}/posts` (S6)
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
| 5 | **SalonPostsScreen** | `salon/[id]/posts.tsx` | SalonPostsScreen.kt | **FAIT** (2026-06-12, S6) |
| 6 | **CollectionsListScreen** | `collections/user/[userId].tsx` | CollectionsScreen.kt | **FAIT** (2026-06-11, S5 + B26) |

#### Famille B - Ecrans statiques (4 ecrans)

Documents juridiques REELS ancres droit gabonais (S7) — relecture juridique a prevoir
avant production (check-lists « A FAIRE AVANT PRODUCTION » en tete de chaque fichier).

| # | Ecran | Route | Statut |
|---|-------|-------|--------|
| 7 | **TermsOfServiceScreen** | `settings/terms.tsx` | **FAIT** (2026-06-12, S7) |
| 8 | **PrivacyPolicyScreen** | `settings/privacy.tsx` | **FAIT** (2026-06-12, S7) |
| 9 | **HelpCenterScreen** | `settings/help.tsx` | **FAIT** (2026-06-12, S7) |
| 10 | **ContactSupportScreen** | `settings/contact.tsx` | **FAIT** (2026-06-12, S7) |

#### Famille C - API backend a creer d'abord (4 ecrans)

| # | Ecran | Route | API backend manquante | Statut |
|---|-------|-------|----------------------|--------|
| 11 | **SecuritySettingsScreen** | `settings/security.tsx` | aucune (backend complet, B25b) | **FAIT** (2026-06-12, S8) |
| 12 | **ChangePhoneScreen** | `settings/change-phone.tsx` | OTP telephone (provider SMS) | **REPORTE** — chantier futur complet (decision S8 : pas de demi-feature sans verification SMS) |
| 13 | **BlockedUsersScreen** | `settings/blocked-users.tsx` | blocage utilisateurs | A faire (**S11**) |
| 14 | **RequestVerificationScreen** | `settings/verification.tsx` | demande de verification | A faire (**S10**) |

> Numerotation actee (2026-06-12) : **S9 = chantier 2FA TOTP** (S9a socle backend FAIT,
> S9b interception login, S9c desactivation, S9d ecrans RN), **S10 = verification**,
> **S11 = blocage utilisateurs**.

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

- **S6 (2026-06-12) : SalonPostsScreen cree** (`app/salon/[id]/posts.tsx`).
  **CLOT LA FAMILLE A : 6/6 ecrans faits.**
  - **Sonde de routing — issue A (coexistence)** : `app/salon/[id].tsx` et
    `app/salon/[id]/posts.tsx` coexistent SANS deplacement ni collision. Preuve : les deux
    routes enregistrees dans le bundle Metro (require.context `./salon/[id].tsx` +
    `./salon/[id]/posts.tsx`), zero erreur bundler, `/salon/{id}` et `/salon/{id}/posts`
    -> 200. NB diagnostic : en web dev le serveur repond 200 a TOUTE url (SPA shell, meme
    `/route/bidon`) — un 200 seul ne prouve rien, la preuve est dans la table de routes
    du bundle.
  - **Verif `getSalonServices` (pre-dropdown)** : CORRECTE, pas de B37. Frontend
    `salons.ts:41-42` GET `/api/salons/{id}/services` type `SalonService[]` == backend
    `SalonServiceController` base `/api/salons/{salonId}/services` + `@GetMapping`
    -> `List<ServiceResponse>` nu (deja prouvee par l'onglet Services 7.7).
  - **Ecran** : racine FlatList (PAS de ScrollView parent — ce qui bridait l'onglet 7.7),
    infinite scroll (page++ tant que !last, garde anti-reponse-perimee par cle de filtres)
    + pull-to-refresh + flag `ignore` au cleanup des useEffect.
  - **Jeu de handlers COMPLET** copie du patron social.tsx (cartographie figee B27->B34) :
    onLike/onBookmark (fusion SELECTIVE B27, jamais le full-replace du KMP qui efface les
    media), onShare (externe natif B31), onComment, onPress, onSaveToCollection
    (CollectionPickerModal B34), onArchive (B32, optimiste+rollback), onPin (B29,
    optimiste+rollback, message limite 3 affiche tel quel), onDelete, onReport + Toast.
    DEPASSE le KMP qui n'avait ni pin, ni signaler, ni supprimer, ni menu « ... ».
  - **Filtres parite KMP** (cote serveur via params confirmes de getPostsBySalon) :
    chips PostType (memes 6 chips que le fil social), tri Recents/Populaires
    (SortBy RECENT/POPular), services en chips horizontales (« Tous services » + chaque
    service) — ADAPTATION assumee du dropdown KMP (pas de DropdownMenu RN, chips plus
    directes). Changement de filtre = reset page 0 (reponses obsoletes jetees).
  - **Header** : salon recharge via `getSalonById` (PAS de param de route — l'URL directe
    fonctionne), titre « Posts · {nom} », retour garde B24b ; cover du salon en tete de
    liste avec nom en overlay (parite header visuel KMP). FAB « + » owner-only
    (`user.id === salon.ownerId`) -> navigation simple `/create-post` (aucune capacite
    nouvelle dans create-post). Etats partages Loading/Error(retry)/Empty. Theming inline
    strict (3 design-fixed : gradient cover, nom blanc sur cover, ombre FAB).
  - **Point d'entree branche** (`app/salon/[id].tsx`, onglet Posts) : ligne « Voir tout -> »
    en tete d'apercu -> `router.push('/salon/{id}/posts')`. L'apercu reste INCHANGE
    (page 0, 2 handlers) : zero regression 7.7. L'ecran dedie n'a plus aucun lien mort.
  - Verifications : `npx tsc --noEmit` -> zero NOUVELLE erreur (les 12 preexistantes
    register/index/profile hors perimetre, identiques a B34) ; bundle web recompile
    (8,2 Mo, ecran present) ; `/salon/{id}` et `/salon/{id}/posts` -> 200.

- **B38 (2026-06-12) : debordement horizontal du detail salon** -- FAIT.
  Symptomes : scroll horizontal parasite, bouton « Reserver une prestation » coupe,
  scroll vertical sale (diagonal + scrollbar web par-dessus).
  - **Cause racine** : prop `full` de Button = `width: '100%'` (Button.tsx:79-81) utilisee
    DANS une row (floating bar du detail salon, a cote de « Suivre » + gap + padding)
    -> contenu plus large que l'ecran -> overflow. Le scroll vertical « vulgaire » etait
    une consequence (viewport elargi).
  - **Correctif** : dans la floating bar, `flex` au lieu de `full` (styles `barBtn`
    flex 1 / `barBtnWide` flex 1.7, minWidth 0, padding reduit) sur les DEUX rangees
    (owner et visiteur) ; label de Button passe en `numberOfLines={1}` (un bouton ne
    wrappe jamais sur 2 lignes dans sa hauteur fixe 48) ; ScrollView du detail en
    `showsVerticalScrollIndicator={false}`. La semantique de `full` (hors row) inchangee.
  - Verifs : tsc zero nouvelle erreur, `/salon/{id}` -> 200 (Metro compile).

- **S7 (2026-06-12) : famille B complete (4/4) — ecrans legaux/support REELS,
  droit gabonais** -- FAIT. Etape 8 : familles A (6/6) + B (4/4) faites, reste famille C.
  - **Changement de cadrage (decision utilisateur)** : pas des coquilles placeholder mais
    de vrais documents de production, longs, ancres dans le droit gabonais verifie par
    recherche web (Etape 1 diagnostique validee avant redaction). REGLE DURE appliquee :
    zero reference legale inventee — tout numero de loi/article cite provient d'une source
    consultee ; les principes non sources sont rediges en clair sans numero.
  - **Corpus juridique verifie** (sources en commentaire d'en-tete de chaque fichier) :
    loi n°001/2011 modifiee par loi n°025/2023 (JO n°218 Bis lu en entier — PDF dans les
    tool-results de la session ; articles VERIFIES : 7-8 APDPVP, 43-44 acces, 50-53
    rectification/effacement, 55 limitation, 58-59 portabilite, 60-62 opposition,
    66 profilage, 70 liceite) ; ordonnance n°0011/PR/2026 (reseaux sociaux : majorite
    numerique 16 ans, identification NIP, signalement 24h/72h — s'applique a Frollot !) ;
    loi n°025/2021 transactions electroniques ; reglement CEMAC n°04/18 services de
    paiement (Airtel/Moov Money) ; AUDCG OHADA 2010 ; TVA 18 % (DGI). Constat : PAS de
    code de la consommation gabonais, donc PAS de droit de retractation legal verifie.
  - **Decisions commerciales appliquees (tranchees par Olsen)** : Premium 10 000 FCFA TTC
    (TVA 18 % incluse), duree determinee SANS reconduction tacite ; remboursement = garantie
    contractuelle VOLONTAIRE 48 h si Premium non utilise (presentee comme engagement
    commercial, pas comme droit legal). PIN mobile money jamais collecte/stocke — seuls
    montant/date/statut/reference conserves.
  - **4 ecrans crees** : `settings/terms.tsx` (CGU+CGV, 15 articles, sommaire cliquable
    scrollTo via onLayout) ; `settings/privacy.tsx` (12 sections, droits avec articles
    verifies + recours APDPVP, mineurs <16 ans) ; `settings/help.tsx` (FAQ accordeon
    6 categories dont pas-a-pas Airtel/Moov, liens croises terms/privacy/contact) ;
    `settings/contact.tsx` (mailto via Linking, 6 objets pre-categorises dont « Donnees
    personnelles (APDPVP) » -> renvoi procedure privacy §8, delais 24h/72h annonces).
    Corps des textes SANS marqueur visible — les incertitudes et coordonnees a remplacer
    (RCCM, NIF, siege, emails reels, points a confirmer par juriste) sont dans le
    commentaire `/* A FAIRE AVANT PRODUCTION */` en tete de chaque fichier.
  - **Entrees Settings activees** (`settings/index.tsx`, pattern RowDef S1) : help,
    contact, terms, privacy ont leur `route:` — plus aucun badge « Bientot » sur ces 4,
    garde B24b sur chaque ecran (fallback `/settings`).
  - Verifs : tsc 12 erreurs preexistantes (register/index/profile, identiques B34), zero
    nouvelle ; les 4 routes presentes dans la table require.context du bundle Metro
    (preuve S6, pas un simple 200) ; `/settings` non regresse.

- **S8 palier 1 (2026-06-12) : B25 SOLDEE — correction a la source des 6 API
  `/api/security/*` (B25b) + 6 URLs admin moderation (B25c)** -- FAIT, TESTE CURL.
  Decisions utilisateur prealables : 2FA EXCLU (chantier futur, zero trace backend) ;
  ChangePhoneScreen EXCLU et REPORTE en chantier futur complet (OTP SMS requis, aucun
  provider SMS backend — pas de demi-feature) ; deleteAccount INCLUS dans security.tsx.
  - **B25b (`src/api/auth.ts` + `src/types/user.ts`)** : les 6 URLs `/api/security/*`
    etaient inventees (AUCUN SecurityController). Corrigees contre UserController reel :
    changePassword `PUT /api/users/me/password` (:732) ; changePhone `PUT /api/users/me/phone`
    (:992, DORMANTE — reservee chantier futur, aucun appelant UI) ; deleteAccount
    `DELETE /api/users/me` (:1034, POST->DELETE, corps via config axios `{data}`) ;
    getActiveSessions `GET /api/users/me/sessions` (:780) ; revokeSession
    `DELETE /api/users/me/sessions/{id}` (:807) ; revokeAllOtherSessions
    `DELETE /api/users/me/sessions` (:842 — le `/others` invente aurait matche
    `/{sessionId}`). Types completes (SecurityDto.kt) : +success sur ChangePassword/
    ChangePhone/DeleteAccount Response, +success/revokedCount sur RevokeSessionResponse,
    +newPhone? sur ChangePhoneResponse. SessionInfo/SessionsListResponse deja conformes.
  - **PIEGE X-Refresh-Token (UserController:783, :845)** : sans ce header,
    getActiveSessions ne marque aucune session courante et revokeAllOtherSessions revoque
    TOUT y compris la session courante (auto-deconnexion, :854-857). Cable dans auth.ts via
    `tokenManager.getRefreshToken()` (client.ts:147, jamais loggue) ; revokeAllOtherSessions
    REFUSE de partir sans refresh token (throw explicite). **+ correction backend** :
    `X-Refresh-Token` ajoute a la whitelist CORS `allowedHeaders` (SecurityConfig.kt:245)
    — sans quoi le preflight web rejetait les 2 appels sessions.
  - **B25c (`src/api/moderation.ts`)** : 6 routes admin pointaient vers
    `/api/social/moderation/*` (base inventee). Base reelle : `/api/social/reports`
    (ModerationController:25). handleReport, moderateContent, getModerationActions,
    appealModeration, handleAppeal, getPendingAppeals corrigees. Routes DORMANTES
    (aucun appelant UI), corrigees pour ne pas laisser de dette.
  - **Tests curl (compte archtest.lecteur + compte jetable s8del clone en DB puis detruit)** :
    login -> GET /me/sessions avec X-Refresh-Token : 200, isCurrent=true/currentSessionId
    sur la session du login ; PUT /me/password : 200 {success:true} puis refresh de l'ancien
    token -> 401 (revocation :753 confirmee, purge les 14 sessions accumulees) ; re-login x2
    -> GET sessions : 2 sessions, courante correcte ; DELETE /sessions/94 : 200
    revokedCount=1 ; +2 logins puis DELETE /sessions avec header : 200 revokedCount=2 et la
    session courante SURVIT (refresh 200) ; PUT /me/phone (confirmation URL seule) : 200 ;
    DELETE /me confirmDeletion=false : 400 refus explicite ; confirmDeletion=true : 200,
    compte efface en base (hard delete verifie SQL). Mot de passe archtest restaure
    (Diag1234!). Re-login post-delete : 429 RateLimitFilter (rate limit IP sur /login apres
    nombreux logins de test — pas un bug).
  - Piege shell note : git-bash corrompt `!` dans les corps JSON curl (`\!` envoye ->
    JsonParseException 500) ; contournement octal `printf '\041'`.

- **S8 palier 2 (2026-06-12) : SecuritySettingsScreen cree** (`settings/security.tsx`).
  Famille C : 1/4 fait (security) ; verification et blocked-users restent (S9, S10) ;
  change-phone REPORTE chantier futur complet (decision : pas de changement de numero sans
  verification SMS — provider a integrer) ; 2FA EXCLU (chantier futur, zero trace backend).
  - **3 sections** (parite KMP + deleteAccount reporte depuis S1) :
    1. Changer le mot de passe : 3 PasswordTextField, regles de force live (>=8 = seule
       regle backend SecurityDto.kt:18, different de l'actuel, confirmation identique),
       avertissement PREALABLE « deconnecte tous vos appareils » ; au succes (le backend
       revoque tous les refresh tokens, UserController:753) -> modal de succes puis
       `logout()` (purement local, authStore:83) + redirect login. Erreur backend telle
       quelle (carte errorContainer).
    2. Sessions actives : liste (icone par deviceType, libelle deviceName/browser/OS,
       IP+location, derniere activite), badge « Cet appareil » sur isCurrent (header
       X-Refresh-Token cable palier 1), revoke par session (bouton masque sur la
       courante), « Deconnecter les autres appareils (n) » desactive si seule la courante,
       confirmation modal B22, re-fetch serveur apres revoke-all. Etats
       LoadingState/ErrorState(retry)/EmptyState + bouton refresh.
    3. Zone danger : carte bordure error, modal 1 (avertissement + mot de passe) puis
       modal 2 (confirmation finale explicite) ; `confirmDeletion:true` envoye SEULEMENT
       apres la 2e confirmation ; erreur (mot de passe faux, 400) re-affichee a l'etape 1 ;
       au succes logout local + redirect login.
  - Entree Settings « Securite » activee (`route: '/settings/security'`), sous-titre
    « Mot de passe, sessions, suppression » (2FA retire). L'entree « Numero de telephone »
    reste desactivee badge « Bientot » (chantier futur).
  - Verifs : tsc 12 erreurs preexistantes (identiques S7), zero nouvelle ;
    `./settings/security.tsx` present dans la table require.context du bundle Metro
    (preuve S6) ; garde B24b ; theming inline strict (seul design-fixed : overlay modal).

- **S8b (2026-06-12) : plafond de 5 sessions actives par utilisateur (demande utilisateur
  apres test en main — trop d'appareils listes)** -- FAIT, TESTE CURL.
  - **Backend** (`RefreshTokenService.kt`) : `MAX_ACTIVE_SESSIONS = 5` (companion object) ;
    `enforceSessionLimit(userId)` appele en tete de `createRefreshTokenWithDeviceInfo`
    AVANT la creation : si >= 5 sessions valides, revoque les moins recemment utilisees
    (tri `lastUsedAt ?: createdAt`) pour que le total post-creation reste <= 5.
    Politique : eviction LRU, JAMAIS de refus de login (un utilisateur ayant perdu ses
    anciens appareils ne doit pas etre enferme dehors).
  - **Surete rotation** : `rotateRefreshToken` revoque l'ancien token AVANT d'appeler la
    creation (meme transaction, auto-flush Hibernate pre-requete JPQL) -> une rotation a
    plafond plein n'evince RIEN (verifie curl : totalCount reste 5 apres rotation).
    Course residuelle acceptee : 2 logins strictement simultanes peuvent depasser
    temporairement (pas de verrou) ; le login suivant re-ecrete.
  - **Frontend** (`security.tsx`) : phrase d'information sous « Sessions actives »
    (« Maximum 5 appareils : au-dela, la session la moins recemment utilisee est
    automatiquement deconnectee »).
  - **Tests curl (archtest, sessions remises a zero en DB)** : 5 logins -> totalCount=5
    (ids 100-104) ; logins 6-7 (apres fenetre rate limit login 5/min/IP, RateLimitFilter:56)
    -> totalCount=5, ids 102-106 (100-101 evincees LRU) ; refresh du token evince -> 401 ;
    refresh du token recent -> 200 ; re-liste apres rotation -> toujours 5.

- **S9a (2026-06-12) : 2FA TOTP (RFC 6238) — socle backend + activation en deux temps** --
  FAIT, TESTE CURL INTEGRALEMENT. Numerotation actee : S9 = 2FA (S9a-S9d),
  verification = S10, blocage utilisateurs = S11. >>> Login NON touche (S9b) <<<.
  - **Migration V044** (`V044__create_two_factor_tables.sql`) : `user_two_factor(user_id
    CHAR(36) PK/FK CASCADE, secret_encrypted VARCHAR(512), enabled BOOL DEFAULT FALSE,
    created_at, confirmed_at NULL)` + `two_factor_recovery_codes(id CHAR(36) UUID(),
    user_id FK CASCADE, code_hash VARCHAR(60), used_at NULL, created_at)`. Conventions
    V002/V024 (InnoDB, utf8mb4_unicode_ci).
  - **Chiffrement** (`security/TotpEncryptionService.kt`) : AES-256-GCM via javax.crypto
    (zero dependance). Cle env `TOTP_ENCRYPTION_KEY` (32 octets Base64, distincte de
    JWT_SECRET), declaree `app.security.totp.encryption-key` (application.yml) SANS
    fallback : init{} leve IllegalStateException si absente/invalide -> le backend
    REFUSE de demarrer, meme en dev (prouve : bootRun sans cle -> crash avec message
    explicite). Format stocke : Base64(IV 12 octets || ciphertext+tag GCM 128).
    GARDE cle perdue/changee : decrypt() log ERROR « Secret 2FA indechiffrable pour
    user X » + IllegalStateException explicite — jamais silencieux. Rotation de cle
    NON implementee (hors perimetre, documente dans le service). `.env.example` cree
    (backend/) avec consigne `openssl rand -base64 32`, sans valeur reelle.
  - **Service** (`service/TwoFactorService.kt`) : secret 20 octets SecureRandom ->
    Base32 RFC 4648 maison (~40 lignes encode+decode ; justification : pas de Base32
    dans le JDK, commons-codec injustifie pour 2 fonctions). TOTP HMAC-SHA1, pas 30 s,
    6 chiffres, fenetre ±1 pas, comparaison MessageDigest.isEqual sans court-circuit.
    10 codes de recuperation XXXX-XXXX (alphabet 32 car. sans O/0/I/1, 40 bits),
    haches BCrypt (PasswordEncoder existant), retournes EN CLAIR une seule fois au
    confirm. URI `otpauth://totp/Frollot:{email}?secret=...&issuer=Frollot&algorithm=
    SHA1&digits=6&period=30` (QR rendu client en S9d). setup() refuse si enabled=true ;
    ecrase une ligne non confirmee (semantique S4 pending_email) ; purge les codes
    residuels. confirm() exige un premier TOTP valide -> enabled=true + confirmed_at.
  - **Endpoints** (`controller/TwoFactorController.kt`, `dto/TwoFactorDto.kt`) :
    POST `/api/users/me/2fa/setup` -> {secret, otpauthUri} (SEULE exposition du secret
    en clair, tant que enabled=false) ; POST `/api/users/me/2fa/confirm` {code} ->
    {success, message, recoveryCodes[10]} ; GET `/api/users/me/2fa/status` -> {enabled}
    (jamais le secret). Couverts par anyRequest().authenticated() (SecurityConfig:187),
    aucun matcher ajoute. Erreurs metier -> 400 avec message clair (pas de 500 B36).
  - **Tests curl (archtest.lecteur, backend redemarre proprement, cle ephemere en env
    jamais affichee)** : bootRun SANS cle -> refus demarrage (message TOTP_ENCRYPTION_KEY
    explicite) ; AVEC cle -> « TOTP_ENCRYPTION_KEY validee (AES-256-GCM) », V044
    appliquee (schema 044) ; status initial -> 200 {enabled:false} ; setup -> 200
    secret+otpauthUri ; status -> false ; confirm code faux -> 400 clair ; confirm
    code calcule (script Python RFC 6238 local) -> 200 + 10 codes XXXX-XXXX ;
    status -> true ; re-setup -> 400 « deja activee » ; re-confirm -> 400 ; sans
    Bearer -> 401 ; SQL : secret_encrypted SANS le Base32 en clair, 10 code_hash
    `$2a$10$` (60 car.) ; double setup non confirme -> secrets differents, 1 seule
    ligne (ecrasement prouve). Donnees de test purgees (tables 2FA vides).
  - **ACTION UTILISATEUR REQUISE** : generer TOTP_ENCRYPTION_KEY (`openssl rand
    -base64 32`) et l'ajouter a backend/.env — la cle de test etait ephemere, le
    backend ne redemarrera pas sans elle.
  - Reste : S9b (interception login : jeton 2fa_pending + rejet filtre JWT + endpoint
    /api/users/login/2fa + compteur jti + RateLimitFilter), S9c (desactivation
    password+TOTP, purge), S9d (ecrans RN).

- **S9b (2026-06-12) : interception 2FA du login — defi 2fa_pending + /login/2fa** --
  FAIT, COMPILE, TESTE CURL INTEGRALEMENT (8/8). Backend pur, login des comptes
  sans 2FA strictement inchange.
  - **Durcissement filtre JWT (JwtAuthenticationFilter.kt:68-78)** : tout token
    portant un claim `type` (quelle que soit sa valeur) est refuse comme access
    token -> 401. Benefice collateral : ferme la faille latente du JWT type=refresh
    (30 j, generateRefreshToken) qui passait comme access token.
  - **JwtTokenProvider** : `getTokenType` (:378), `generateTwoFactorPendingToken`
    (:398, Pair<token,jti>, claims sub+jti+type=2fa_pending, 5 min),
    `validateTwoFactorPendingToken` (:421, signature + non expire + type strict).
  - **Interception login (UserController.kt:293-297)** : APRES clearLoginFailures,
    AVANT toute emission de token. Compte 2FA-active -> 200
    `{requiresTwoFactor:true, twoFactorToken}` (AuthResponse.twoFactorChallenge,
    champs @JsonInclude NON_NULL — invisibles pour les comptes sans 2FA), AUCUN
    access token, AUCUNE ligne refresh_tokens (prouve SQL). Les println sensibles
    du login (PII) ont ete remplaces par logger.debug sans PII.
  - **Endpoint public POST /api/users/login/2fa (UserController.kt:346)** :
    `TwoFactorLoginRequest{twoFactorToken, code}`. Ordre strict : 1) validation du
    jeton de defi, 2) compteur jti, 3) verifyLoginCode, 4) consommation du jeton +
    emission par le chemin EXISTANT (generateToken + createRefreshTokenWithDeviceInfo
    -> plafond S8b de 5 sessions automatique, device capture sur CETTE requete).
    >>> ECART vs plan initial : ajout de `/api/users/login/2fa` au permitAll
    (SecurityConfig.kt:93), OUBLIE a l'ecriture — sans lui, 401 avant le controleur
    (decouvert a la reprise post-crash, etat des lieux). <<<
  - **TwoFactorService.verifyLoginCode (:156)** : TOTP courant (fenetre ±1) OU code
    de recuperation (BCrypt, insensible casse/tiret, consomme usage-unique :
    used_at pose dans la MEME transaction).
  - **RateLimitFilter** : entree `/api/users/login/2fa` 5/min/IP (:58) + compteur
    par jti (:179-231, MAX_TWO_FACTOR_ATTEMPTS=5, TTL 6 min purge paresseuse,
    `registerTwoFactorAttempt`/`consumeTwoFactorChallenge` — le jeton est aussi
    tue apres succes, anti-rejeu).
  - **Tests curl (8/8 PASS, archtest.lecteur 2FA reconstitue via flux S9a)** :
    1) token forge type=refresh (signe avec JWT_SECRET, script Python) -> 401 /me,
    access normal -> 200 ; 2) non-regression login sans 2FA -> 200 vrais tokens ;
    3) defi -> 200 requiresTwoFactor+twoFactorToken, refresh_tokens 32->32 (SQL) ;
    4) etancheite : twoFactorToken sur /me -> 401 ; 5) 5 codes faux -> 401
    « incorrect », 6e essai -> 401 « Trop de tentatives » (jeton mort ; NB : le
    bucket IP 5/min repond 429 avant si rafale — deux couches) ; 6) re-login ->
    bon TOTP (script independant RFC 6238) -> 200 vrais tokens, 5 sessions actives
    max (SQL, eviction S8b) ; 7) code de recuperation MPXR-FW6A -> 200 tokens,
    used_at pose (SQL 10/1), rejeu du meme code -> 401 ; 8) jeton 2fa_pending
    forge expire -> 401 « invalide ou expire ».
  - Etat compte de test : archtest.lecteur 2FA ACTIVE (conserve pour S9c/S9d),
    9 codes de recuperation restants.
  - Reste : S9c (desactivation password+TOTP, purge), S9d (ecrans RN).

- **S9c (2026-06-12) : desactivation 2FA + regeneration des codes de recuperation** --
  FAIT, COMPILE, TESTE CURL INTEGRALEMENT. Backend pur. Apres S9c il ne reste que
  S9d (ecrans RN) pour clore le chantier 2FA.
  - **DELETE /api/users/me/2fa (AUTHENTIFIE, TwoFactorController)** : corps
    `TwoFactorDisableRequest{password, code}`. Exige les DEUX preuves : mot de passe
    (verifie EN PREMIER — un mot de passe faux ne consomme JAMAIS de code de
    recuperation) ET code via `verifyLoginCode` S9b (TOTP courant OU recovery non
    utilise ; un recovery deja consomme est refuse). `TwoFactorService.disable`
    @Transactional : verif code AVANT purge ; si le code etait un recovery, sa
    consommation et la purge sont dans la MEME transaction (rollback complet en cas
    d'echec). Purge EXPLICITE des deux tables (la cascade FK V044 ne joue qu'au
    DELETE du user) -> etat strictement identique a un compte sans 2FA. 2FA
    inactive -> 400 clair. Erreurs -> 400 avec message (pas de 500 B36).
  - **POST /api/users/me/2fa/recovery-codes/regenerate (AUTHENTIFIE)** :
    `TwoFactorRegenerateRequest{password, code}`. DECISION TRANCHEE : meme niveau
    de preuve que la desactivation (password + code TOTP OU recovery) — detenir un
    lot frais equivaut a detenir la 2FA ; un recovery consomme ici est de toute
    facon remplace par le nouveau lot. Supprime TOUS les anciens codes (utilises ou
    non), genere 10 nouveaux (clair une seule fois, BCrypt en base), secret TOTP
    INTACT (2FA reste active). 2FA inactive -> 400 clair.
  - **PIEGE DECOUVERT (corrige)** : le principal pose par JwtAuthenticationFilter
    est RECONSTRUIT depuis les claims JWT (getUserFromToken :103) -> passwordHash
    VIDE -> checkPassword echouait toujours (premier essai curl : 400 « Mot de
    passe incorrect » avec le bon mot de passe). Correction : rechargement BDD
    `userRepository.findById(user.id!!).orElse(user)` avant le check (meme pattern
    que le login). A RETENIR pour tout futur endpoint verifiant le mot de passe
    avec le principal.
  - **Interactions verifiees (lecture + SQL, scope non elargi)** :
    changePassword (UserService.kt:362-374) ne touche que password_hash -> 2FA
    survit, conforme. deleteAccount (UserService.kt:516) = hard delete
    userRepository.delete -> cascade FK V044 (ON DELETE CASCADE :17/:26) purge les
    deux tables — PROUVE : compte jetable s9c.jetable cree en SQL, 2FA activee
    (utf=1 rc=10), DELETE /me -> 200, SQL apres : users=0 utf=0 rc=0. Compte jetable
    auto-purge par sa propre suppression.
  - **Tests curl (tous PASS)** :
    a) disable password OK + TOTP -> 200 ; SQL utf=0 rc=0 (purge prouvee) ;
       status -> enabled:false ;
    b) re-login -> 200 vrais tokens directs, champ requiresTwoFactor ABSENT
       (le defi s'eteint avec la desactivation) ;
    c) apres reactivation : password FAUX + TOTP bon -> 400, status true ;
       password bon + code FAUX -> 400, status true ; disable via code de
       RECUPERATION -> 200, SQL utf=0 rc=0 ;
    d) disable quand 2FA inactive -> 400 « n'est pas activee » ;
    e) regenerate (password+TOTP) -> 200, 10 nouveaux codes ; SQL : exactement 10,
       0 utilises, 0 hash en commun avec l'ancien lot (comm -12) ; ANCIEN code au
       login/2fa -> 401 ; NOUVEAU code -> 200 vrais tokens ;
    f) regenerate quand 2FA inactive -> 400 clair.
  - Etat final compte de test : archtest.lecteur 2FA ACTIVE, secret regenere
    pendant les tests, lot final de 10 codes frais (0 utilise) — pret pour S9d.
  - Reste : S9d (ecrans RN : wizard activation QR + saisie au login + desactivation
    + regeneration/affichage des codes ; react-native-svg + qrcode-svg).

- **S9d-1 (2026-06-12) : gestion 2FA dans security.tsx (RN)** -- FAIT, tsc ZERO
  nouvelle erreur (baseline 12 preexistantes intacte), Metro compile security.tsx
  (chunk 200 + contenu verifie). IMPORTANT : le LOGIN n'est PAS branche — un
  utilisateur qui active la 2FA ici ne verra PAS de defi a la connexion RN tant
  que S9d-2 (ecran de saisie du code apres `requiresTwoFactor`) n'est pas fait.
  - **Dependances (npx expo install, versions alignees SDK 56)** :
    react-native-svg 15.15.4, react-native-qrcode-svg ^6.3.21, expo-clipboard
    ~56.0.4. Compatibles Expo Go (pas de module natif custom).
  - **Couche API** : `src/types/user.ts` (6 interfaces alignees TwoFactorDto.kt,
    commentees endpoint par endpoint) + `src/api/auth.ts` (getTwoFactorStatus,
    setupTwoFactor, confirmTwoFactor, disableTwoFactor — corps sur DELETE via
    `{ data }` axios, meme pattern B25b que deleteAccount —, regenerateRecoveryCodes).
  - **security.tsx, section « Double authentification »** (entre mot de passe et
    sessions) : statut via GET /2fa/status (LoadingState/ErrorState+retry) ;
    si active -> badge « Activee » + boutons Regenerer les codes / Desactiver ;
    sinon -> PrimaryButton « Activer la 2FA ».
    - **Wizard activation** : setup appele a l'ouverture (un setup non confirme est
      ecrase par le suivant — annuler est sans danger), QR `otpauthUri` (qrcode-svg,
      fond blanc design-fixed), secret en clair selectionnable + bouton copier
      (expo-clipboard) pour saisie manuelle, champ premier code 6 chiffres ->
      confirm -> bascule sur l'ecran des codes de recuperation.
    - **RecoveryCodesModal (composant UNIQUE activation + regeneration, zero
      duplication)** : 10 codes en grille 2 colonnes monospace selectionnables,
      bouton « Copier les 10 codes », avertissement errorContainer (« ne seront
      plus jamais affiches »), bandeau supplementaire « anciens codes plus
      valables » si regeneration. GARDE NON CONTOURNABLE : case « J'ai enregistre
      mes codes de recuperation » obligatoire — bouton Terminer desactive,
      onRequestClose refuse, pas de fermeture par l'overlay ; garde rearmee a
      chaque nouveau lot (useEffect sur codes).
    - **Desactivation / regeneration** : modals patron B22 (PasswordTextField +
      TextField code « 123456 ou XXXX-XXXX »), erreurs backend affichees telles
      quelles, zero catch muet, fermetures bloquees pendant l'appel.
  - **settings/index.tsx** : sous-titre Securite -> « Mot de passe, 2FA, sessions,
    suppression » (2FA reintegree, l'exclusion S8 est soldee).
  - Theming inline useTheme() strict ; seules couleurs brutes = overlay et fond
    blanc du QR, commentees `// design-fixed`.
  - **Reste : S9d-2 (defi au login RN)** : ecran de saisie TOTP/recovery apres
    reponse login `requiresTwoFactor=true` + appel POST /api/users/login/2fa
    avec le jeton 2fa_pending. Compte de test pret : archtest.lecteur 2FA ACTIVE.

- **S9d-2 (2026-06-12) : defi 2FA au login RN + fermeture de la classe « session
  zombie » — LE CHANTIER 2FA (S9a -> S9d) EST CLOS** -- FAIT, tsc zero nouvelle
  erreur (baseline 12), chunk Metro two-factor compile et verifie.
  - **Symptome corrige** : compte 2FA-actif -> login 200 SANS tokens (defi S9b,
    accessToken="" dans la reponse plate) -> l'ancien authStore stockait les
    chaines vides et posait isAuthenticated=true -> session zombie (accueil
    accessible, tous les /me/* en 401, profil vide).
  - **Diagnostic formes reelles (verifie backend, rien presume)** : defi a la
    RACINE du AuthResponse plat (`requiresTwoFactor` + `twoFactorToken`,
    @JsonInclude NON_NULL — absents hors defi) ; succes /login/2fa = MEME
    AuthResponse plat via fromUser qu'un login normal ; erreurs 401 = corps
    AuthResponse.error -> champ `message` ; jeton mort (expire 5 min / 5 essais
    epuises) = messages contenant « reconnecter » ; corps requete
    {twoFactorToken, code}. BONUS verifie : l'intercepteur 401 de client.ts:86
    (`url.includes('/users/login')`) couvre deja /users/login/2fa -> aucun
    refresh parasite sur les 401 du defi.
  - **authStore.ts — GARDE DEFENSIVE anti-zombie a la racine** : finalisation de
    session CENTRALISEE en UNE fonction `finalizeSession` (login normal ET succes
    2FA, etat final strictement identique) qui REFUSE d'entrer en etat authentifie
    si accessToken OU refreshToken est absent/vide (erreur visible, rien stocke)
    -> plus jamais d'isAuthenticated sans vrai token, meme sur reponse malformee
    future. `login` teste `requiresTwoFactor` AVANT tout stockage : si defi ->
    rien stocke, jeton garde dans `pendingTwoFactorToken` (EN MEMOIRE SEULE,
    jamais SecureStore/AsyncStorage, jamais dans l'URL — un refresh web le perd
    volontairement), sentinel rendu au caller. `loginTwoFactor(token, code)` ->
    POST /login/2fa -> finalizeSession. `clearTwoFactorChallenge` jette le jeton ;
    logout le purge aussi.
  - **login.tsx** : si `response.requiresTwoFactor` -> push `/(auth)/two-factor`
    et RETURN (pas de carte succes, pas de redirection accueil). Sinon
    comportement strictement inchange (non-regression comptes sans 2FA).
  - **app/(auth)/two-factor.tsx (nouveau, + Stack.Screen dans _layout)** : un
    champ unique TOTP 6 chiffres OU recovery XXXX-XXXX (chaine brute, le backend
    verifyLoginCode gere les deux), indice explicite. Valider -> loginTwoFactor ->
    accueil (meme chemin final qu'un login normal). Erreurs backend AFFICHEES
    TELLES QUELLES ; si message contient « reconnecter » -> etat jeton mort :
    champ desactive, bouton unique « Retour a la connexion » (reessayer serait
    inutile). ANTI double-soumission (guard submitting + loading + disabled — le
    compteur jti plafonne a 5 essais, un double-clic gacherait un essai).
    Annuler/back -> clearTwoFactorChallenge + garde B24b. Arrivee sans jeton
    (URL directe, refresh web) -> redirect login via useEffect (navigatedRef
    evite la double navigation au succes). Theming inline strict, composants
    partages (TextField/PrimaryButton/OutlineButton), zero catch muet.
  - **types/user.ts + api/auth.ts** : `requiresTwoFactor?`/`twoFactorToken?` sur
    AuthResponse (commentes : champs racine, JsonInclude NON_NULL),
    `TwoFactorLoginRequest`, `authApi.loginTwoFactor` (endpoint public).
  - Tests en main attendus (Olsen) : compte SANS 2FA -> accueil normal (non-
    regression) ; compte AVEC 2FA -> ecran de defi AVANT tout acces -> bon TOTP ->
    accueil profil PLEIN, /me/* 200, settings/security charge statut + sessions
    (zombie resolue) ; mauvais code -> erreur backend, reste sur l'ecran ;
    recovery valide -> passe ; annuler -> retour login propre.

- **S9-0 (2026-06-12) : Etape 9 i18n — OUTILLAGE + schema de nommage PROPOSE
  (rien d'applique)** -- FAIT, tsc 12 = baseline, AUCUN JSON de prod modifie
  (git status : seuls scripts/i18n/* nouveaux), aucun ecran touche.
  - **Cadre tranche avec Olsen (NON NEGOCIABLE)** : 5 langues FR -> EN -> es/de -> ar ;
    nommage PROPRE RN (le KMP = reference de comprehension/traduction SEULEMENT,
    pas de source de cles ni d'import automatique) ; es/de/ar GENERES puis RELUS
    par sous-agents ; 4 ecrans legaux EXCLUS (FR fait foi + bandeau) ; arabe = RTL
    natif complet en dernier (S9-4) ; templates email backend = chantier separe.
  - **scripts/i18n/extract-kmp-reference.js** : parse les strings_{fr,en,es,de,ar}.kt
    KMP (regex `"cle" to "valeur"` + desechappement Kotlin) -> kmp-reference.json
    HORS prod (jamais charge par l'app) : index par VALEUR FR -> {en,es,de,ar} +
    cles KMP en metadonnee indicative. Chiffres reels : fr 876 / autres 858 cles
    parsees, 727 valeurs FR distinctes, 99 groupes de doublons (meme texte FR sous
    plusieurs cles KMP), 56 divergences de traduction signalees au sein des groupes
    (ex. « Abonnes » -> Subscribers OU Followers selon l'ecran KMP — a arbitrer a
    la generation).
  - **scripts/i18n/check-keys.js** : parite des cles des 5 JSON de prod (src/i18n/),
    fr = reference, liste manquantes + orphelines par langue, exit 1 si ecart
    (pre-commit-ready). Baseline ACTUELLE rapportee (pas corrigee ici) :
    fr 201 / en 201 (0 ecart), es 66 (-135), de 66 (-135), ar 57 (-144),
    zero orpheline -> FAIL (414 ecarts) = point de depart attendu.
  - **Schema de nommage PROPOSE (en attente de validation Olsen)** : namespaces
    par zone fonctionnelle + `common.*` transverse (actions/etats/erreurs),
    camelCase, profondeur max 3, interpolation i18next `{{var}}`, pluriels via
    suffixes `_one/_other`. Les 201 cles fr/en existantes : recommandation =
    RENOMMER vers le schema final en S9-1 (cout faible : 174 appels t() au total).
  - **Reste** : validation du schema -> S9-1 (structuration des JSON) -> S9-2
    (externalisation FR par zone) -> S9-3 (EN puis es/de) -> S9-4 (ar + RTL).

- **Telephone increment 1 (2026-06-12) : reactivation du chemin backend changePhone —
  stockage DECLARATIF E.164 + visibilite phone_public (V045)** -- FAIT, backend pur,
  prouve curl (comptes phonetest.a/b crees puis purges). Aucune verification OTP/SMS
  (couche future). Le frontend (increments 2-3 : selecteur pays libphonenumber-js +
  ChangePhoneScreen) reste a faire — l'entree Settings garde son badge « Bientot ».
  - **(A) Audit d'etancheite — 9 surfaces UserResponse cartographiees** :
    proprietaire/admin (passees `includePrivatePhone=true`) = GET /me (UserController:561),
    PUT /me (:589), avatar (:759), admin getAllUsers (:86), admin verifyUser
    (VerificationService:129) ; publiques (defaut FILTRE de fromEntity, aucun edit) =
    search mentions (UserController:710), FollowService:191, SearchService:53,
    SocialService:2216. La regle vit en UN point : `UserResponse.fromEntity(user,
    includePrivatePhone=false par defaut)` -> `phoneNumber = if (includePrivatePhone
    || user.phonePublic) ... else null` — tout futur appelant est prive-par-defaut.
    Canal transactionnel : BookingDto.kt:132 `clientPhone = client.phoneNumber` lit
    l'ENTITE directement, ne passe pas par UserResponse -> le salon avec RDV voit
    toujours le numero, intact par construction (preuve code, pas de fixture booking).
    DECOUVERTE/DETTE SOLDEE : 2e chemin d'ecriture parallele PUT /me
    (UpdateProfileRequest.phoneNumber + UserService updateUserProfile) SANS validation/
    unicite/visibilite, zero appelant frontend -> SUPPRIME (chemin unique = changePhone).
    Hors perimetre signale : l'email reste expose sur les surfaces publiques (preexistant).
  - **(B) V045** : `users.phone_public BOOLEAN NOT NULL DEFAULT FALSE` (appliquee,
    flyway_schema_history success=1). phone_number varchar(20) UNIQUE NULL inchange.
  - **(C) changePhone durci (UserService)** : recharge BDD (piege principal JWT sans
    passwordHash), mdp verifie, normalisation `trim().takeIf{isNotEmpty}` -> blank=NULL
    (jamais '' — l'UNIQUE l'exige), E164_REGEX `^\+[1-9]\d{1,14}$` serveur (sinon 400),
    suppression -> phone_public force a false, doublon capture par **saveAndFlush DANS
    le try** (avec save() seul la violation UNIQUE n'eclaterait qu'au commit, hors du
    try) -> 400 « Ce numero est deja utilise. ». DTO resserres : ChangePhoneRequest
    {newPhone?, phonePublic=false, password}, Response renvoie newPhone+phonePublic.
  - **Tests curl (tous PASS, etat SQL verifie a chaque etape)** : stockage +24106123456
    -> 200, GET /me le montre, SQL E.164/phone_public=0 ; formats invalides 0612, abc123,
    +0241..., >16 car. -> 4x400 sans toucher la base ; doublon B->numero de A -> 400 ;
    visibilite : B cherche A prive -> cle phoneNumber ABSENTE du JSON (NON_NULL), A passe
    public -> PRESENTE, /me proprietaire toujours present meme prive ; suppression "" et
    null sur les DEUX comptes -> 200/200, SQL NULL/NULL (pas de collision UNIQUE sur ''),
    phone_public retombe a 0 (A etait public avant suppression).

- **Telephone increment 3 (2026-06-12) : ecran settings/change-phone.tsx —
  LE CHANTIER TELEPHONE (incr. 1 -> 3) EST CLOS** -- FAIT, tsc 12 erreurs =
  baseline exacte (zero nouvelle), chunk Metro /app/settings/change-phone -> 200
  (ChangePhoneScreen/PhoneNumberField/phonePublic presents), demo /app/dev/
  phone-demo -> 404 et 0 occurrence dans le bundle.
  - **Couche API resserree** : ChangePhoneRequest TS = { newPhone: string ('' =
    suppression, JAMAIS optionnel — l'envoi du champ est requis), phonePublic:
    boolean, password } aligne SecurityDto.kt ; ChangePhoneResponse + phonePublic ;
    User.phonePublic?: boolean (present sur /me, vue proprietaire). Commentaire
    « chantier futur OTP SMS » d'auth.ts remplace (numero declaratif, couche future).
  - **Ecran change-phone.tsx** : PREFILL via /me REFETCH (PAS le store —
    l'AuthResponse du login ne porte ni phoneNumber ni phonePublic ; le refetch
    alimente aussi le store via setUser), LoadingState + carte erreur + Reessayer.
    PhoneNumberField (incr. 2) pre-rempli (pays + format national deduits de
    l'E.164), Enregistrer desactive tant que (numero valide + mot de passe) n'est
    pas reuni. Switch visibilite HONNETE : prive = « Visible de vous seul »,
    public = « Visible sur votre profil public » + mention discrete du canal
    transactionnel (un salon avec RDV voit le numero meme en prive — comportement
    backend incr. 1, pas cache). PasswordTextField partage. Succes -> setUser
    local (phoneNumber + phonePublic, facon S4) + etat succes au message adapte
    (public/prive/supprime). Erreurs backend TELLES QUELLES en carte
    errorContainer (400 format / doublon « deja utilise » / mdp incorrect —
    prouves curl incr. 1). SUPPRESSION = intention SEPAREE : bouton distinct
    (visible seulement si un numero existe), modal B22 avec PasswordTextField
    PROPRE a la modale, newPhone:'' -> backend NULL + phonePublic=false.
    Retour header garde B24b. Theming inline strict.
  - **Settings active** : entree « Numero de telephone » recoit
    route:'/settings/change-phone' (badge « Bientot » retire par le pattern
    RowDef) ; sous-titre DYNAMIQUE = numero du store au formatInternational
    (libphonenumber-js) quand il existe, sinon libelle generique.
  - **Demo throwaway supprimee** : app/dev/phone-demo.tsx + dossier app/dev
    purges, zero reference restante (grep), route 404, absente du bundle.
  - Tests en main attendus (Olsen) : prefill d'un numero existant (pays + format
    corrects), saisie + visibilite + enregistrement, doublon -> 400 backend tel
    quel, mdp faux -> 400, suppression (modal + retour), badge « Bientot »
    disparu de Settings et sous-titre = numero formate apres enregistrement.

- **Telephone increment 2 (2026-06-12) : composants selecteur de pays + champ
  telephone international, REUTILISABLES (inscription/profils pro a venir)** -- FAIT,
  tsc 12 erreurs = baseline exacte (zero nouvelle), chunk Metro /app/dev/phone-demo
  compile (PhoneNumberField/CountryPickerModal/CountryFlag/flagcdn presents).
  - **Dependance** : `libphonenumber-js` (npm install — JS pur, pas un module natif,
    Expo Go OK). 245 pays via getCountries().
  - **Strategie drapeaux RETENUE : SVG flagcdn.com via expo-image** — docs Expo v56
    verifiees : SVG supporte Android/iOS/Web par expo-image ; CDN prouve curl
    (ga.svg -> 200 image/svg+xml). URL centralisee dans `getFlagUrl` (countries.ts) :
    bascule PNG w80 = UNE ligne si un device Android reel prouvait le contraire.
    Fallback monogramme (cercle primaryContainer + code ISO) sur onError — offline
    au 1er chargement = pas un trou. Cache disque auto expo-image ensuite.
  - **src/utils/countries.ts** : table statique de 245 noms FR (PAS Intl.DisplayNames
    — pari Hermes evite ; couverture verifiee par script : 245/245, zero manquant,
    zero orphelin), `Country {iso2,nameFr,callingCode}`, tri localeCompare 'fr',
    `getDefaultCountry()` = GA/+241, fallback code ISO si territoire sans nom,
    `searchCountries` insensible casse/ACCENTS (normalize NFD + strip diacritiques —
    supporte par Hermes) par nom FR OU indicatif (« gab » et « 241 » -> Gabon).
  - **src/components/phone/CountryPickerModal.tsx** (patron B22) : recherche en tete
    (TextField partage) filtrant en direct, FlatList ~250 lignes (initialNumToRender
    20, keyboardShouldPersistTaps), ligne = drapeau + nom FR + indicatif tabular-nums,
    selection marquee (fond primaryContainer + check), tap -> onSelect + fermeture,
    overlay/croix -> onClose (query reset). Theming inline strict.
  - **src/components/phone/PhoneNumberField.tsx** : bouton-pays (drapeau + indicatif +
    chevron, ouvre la modale) ACCOLE au champ national. Formatage A LA FRAPPE via
    AsYouType — PIEGE evite : formater seulement quand le texte S'ALLONGE (en
    suppression AsYouType re-insererait la ponctuation effacee). Chaque frappe ->
    onChangeE164(parsePhoneNumberFromString .number | null si vide) +
    onValidityChange(isValidPhoneNumber ; vide = valide). Changement de pays ->
    chiffres conserves reformates pour le nouveau pays. Sync depuis le parent via
    lastEmittedRef (ignore l'echo de sa propre emission ; pre-remplissage E.164 ->
    pays + format national deduits). Indicateur de validite DISCRET (check vert
    quand valide, rien d'agressif pendant la frappe). ZERO appel backend.
  - **Preuves logique (node)** : AsYouType GA '06123456' -> '06 12 34 56' ;
    parse('06 12 34 56','GA') -> +24106123456 valide ; memes chiffres en FR ->
    '06 12 34 56 78' -> +33612345678 (reformatage au changement de pays).
  - **app/dev/phone-demo.tsx** : ecran de demo THROWAWAY (en-tete explicite,
    aucune entree de navigation, URL directe /dev/phone-demo) montrant le champ +
    sortie E.164/validite en direct. A SUPPRIMER a l'increment 3. settings/
    change-phone et l'entree Settings (« Bientot ») PAS touches.
  - Piege Windows rencontre : watcher Metro ne voit pas un DOSSIER cree apres son
    demarrage (UnableToResolveError sur fichiers existants) -> redemarrage Metro.
  - **Reste : increment 3** = ecran settings/change-phone.tsx « ultra pro »
    branchant PhoneNumberField sur PUT /api/users/me/phone (+ phonePublic), DTO TS
    ChangePhoneRequest a resserrer, retrait du badge « Bientot », purge de la demo.

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

- **B33 (2026-06-11) : BACKEND — archivage GLOBAL facon Instagram** -- FAIT, teste curl.
  Remplace l'archivage par utilisateur (decouvertes B32 points 1-3) : un post archive est
  masque pour TOUS, reversible par son auteur uniquement. Chantier interrompu par crash
  machine puis repris : code retrouve intact, complete (favoris + 3 fuites), teste, trace.
  - **Migration V041** (`V041__add_global_archive_to_posts.sql`) : colonnes
    `posts.is_archived` (BOOLEAN NOT NULL DEFAULT FALSE) + `posts.archived_at`
    (DATETIME NULL) ; migration des archives owner-only existantes de `post_archives`
    vers le flag. Appliquee (Flyway schema 041). Table `post_archives` conservee
    DORMANTE (future fonction « Masquer ce post » sur les posts d'autrui) ;
    PostArchiveRepository conserve mais retire de SocialService.
  - **Post.kt** : champs `isArchived`/`archivedAt`, calques sur isHidden/isDeleted.
  - **archive/unarchive** (URLs INCHANGEES `POST`/`DELETE /posts/{postId}/archive`) :
    basculent le flag, idempotents, ownership obligatoire -> **403** si non-auteur
    (UnauthorizedAccessException, handler existant).
  - **Exclusion `!isArchived` sur TOUS les chemins de lecture** (SocialService sauf
    mention) : feed (2 branches connecte/anonyme) · following feed · trending (dans la
    requete SQL `findTrendingPosts`, PostRepository) · posts par user · posts par salon ·
    posts par hashtag · nearby · recherche (searchPostsByContent + searchPosts) ·
    pinned posts · **favoris (getFavoritesByUser — trou du plan initial, exclusion simple
    y compris pour l'auteur, pagination manuelle facon getFeed)** · posts mis en avant du
    salon (getSalonSocialProfile) · posts de collection (getCollectionPosts, mapNotNull —
    sinon getPostById y jetait un 404 qui cassait TOUTE la liste) · posts de portfolio
    (PortfolioService.getPortfolioPosts). Les recentPosts des 4 profils passent par
    getPostsByUser/getPostsBySalon -> couverts.
  - **getPostById** : post archive -> **404** pour tout non-auteur, **200** pour l'auteur.
  - **getArchivedPosts reecrit** : lit `isArchived = true` via
    `findByAuthorIdAndIsArchivedTrueOrderByArchivedAtDesc` ; URL
    (`GET /users/{userId}/archives`) et forme `Page<PostResponse>` INCHANGEES ->
    l'ecran Archives RN ne change pas.
  - **Tests curl (2 utilisateurs dedies en base : `archtest.auteur@frollot.test`
    hairstylist / `archtest.lecteur@frollot.test` client — inscrits par INSERT direct car
    EMAIL_ENABLED=false bloque la pre-inscription)** : avant archivage le post est
    visible (feed=1, trending=1, users/{A}/posts=1, favoris de B=1) ; B archive le post
    de A -> 403 ; A archive -> 200 ; apres : 0 partout, getPostById B -> 404, A -> 200,
    archives de A -> Page avec le post (totalElements=1), archives de A demandees par
    B -> 403 ; A desarchive -> 200 ; tout redevient 1. Cycle complet vert.
  - **BUG PREEXISTANT decouvert (NON corrige, hors chantier)** : le feed Suivis
    (`GET /feed/following`) est CASSE backend-wide — `follows.following_type` est
    `enum('user','salon','coiffeur')` en DB (minuscules) mais l'enum Kotlin
    `FollowingType` attend USER/SALON/COIFFEUR : l'ecriture passe (MySQL case-insensitive)
    mais TOUTE lecture jette `No enum constant FollowingType.coiffeur` -> 400. Le filtre
    archivage y est bien dans le code (getFollowingFeed). A traiter en increment dedie.

- **>>> POINT DE BASCULE B33 backend -> SECOND TEMPS frontend (menu detail + pin B29). <<<**
  En cas de crash : le backend ci-dessus est FAIT/teste/trace ; reprendre au frontend.

- **B34 (2026-06-11) — SECOND TEMPS frontend : menu detail + epinglage B29. FAIT.**
  - **B29 — URLs pin corrigees a la source** (`src/api/social.ts`) :
    `unpinPost` = DELETE `/posts/{id}/pin` (l'ancien POST /unpin n'existait pas) ;
    `getPinnedPosts` = GET `/users/{authorId}/pinned-posts` (l'ancien /posts/pinned/{id}
    n'existait pas). NB : la limite backend de 3 pins jette IllegalStateException mappee
    **HTTP 401** par le handler de SocialController — l'intercepteur axios fait un refresh
    inutile puis rejette avec le message backend intact (pas de logout) ; message affiche en toast.
  - **Dialog collection extrait** : `src/components/social/CollectionPickerModal.tsx`
    (patron B22, props postId/onClose/onFeedback, flag ignore au cleanup), exporte via index.ts.
    `social.tsx` nettoye (etats + dialog inline + styles supprimes), utilise le composant.
  - **Menu « ... » au detail du post** (`app/post/[id].tsx`, patron B22), cartographie figee :
    collection · archiver (isOwn -> archive puis goBack) · epingler/desepingler (isOwn) ·
    signaler (-> /report entityType POST) · supprimer (isOwn, Alert confirmation -> goBack).
    « Voir le post » omis (on y est deja).
  - **Dettes corrigees au detail** : garde B24b (`goBack()` = canGoBack ? back : replace('/(tabs)'))
    sur le header ET les retours post-action ; les 3 catch muets (loadMoreComments,
    submitComment, deleteComment) affichent desormais un toast avec le message backend.
  - **Epinglage B29 au fil ET au detail** : `handlePin` optimiste (bascule isPinned) +
    rollback + toast ; PostCard recoit prop `onPin`, entree menu Epingler/Desepingler
    (icone pin-outline/pin-off-outline) visible isOwn seulement.
  - **Section « Posts epingles » en TETE du profil coiffeur** (`app/profile/coiffeur/[id].tsx`) :
    la section existait deja (Promise.allSettled, non bloquante, masquee si vide) mais etait
    MORTE a cause de l'URL fausse — ranimee par la correction B29 et deplacee juste apres
    la profileCard (avant Bio).
  - Verifs : `npx tsc --noEmit` zero erreur dans les fichiers touches (12 erreurs
    preexistantes hors perimetre : register/index/profile) ; Metro 8081 repond 200.

- **B35 (2026-06-12) : BACKEND — correction du fil « Suivis » (bug enum following_type
  trace en B33). FAIT, teste curl.**
  - **Cause racine (STRUCTURELLE, pas un nettoyage de donnee)** : divergence de casse
    code/base. `Follow.kt` `@Enumerated(EnumType.STRING)` + enum Kotlin `FollowingType`
    en MAJUSCULES (USER/SALON/COIFFEUR) vs colonne V016 `ENUM('user','salon','coiffeur')`
    minuscules. A l'INSERT MySQL accepte 'SALON' (collation insensible a la casse) mais
    stocke 'salon' ; a la LECTURE `Enum.valueOf("salon")` (sensible a la casse) crashe.
    Tout follow, neuf ou ancien, etait touche — prouve en creant un follow via l'API
    (envoye SALON, stocke salon).
  - **Correction : migration `V042__fix_follows_following_type_enum_case.sql`** —
    colonne passee en `VARCHAR(20) NOT NULL` (coherent avec `length = 20` de l'entite)
    + `UPDATE follows SET following_type = UPPER(following_type)`. Meme motif et meme
    remede que V020 (reaction_type) et V025 (visibility) : on aligne la base sur le code,
    pas de couche de traduction. Zero changement de code, zero changement de contrat API
    (le JSON exposait deja SALON/COIFFEUR).
  - **4 endpoints repares, testes curl apres redemarrage avec V042** :
    `GET /feed/following` 400 -> **200** (posts du coiffeur suivi) ;
    `GET /coiffeurs/{id}/followers` 500 -> **200** ; `GET /users/{id}/following`
    500 -> **200** (followingType: "COIFFEUR") ; `DELETE .../follow` (unfollow)
    500 -> **200**. Non-regression : POST follow 200, `isFollowedByCurrentUser` true,
    `followersCount` 1 (exists/count n'ont jamais ete touches).
  - **Balayage des autres enums** : `following_type` etait la SEULE divergence de casse
    restante parmi tous les `@Enumerated(STRING)`. TaggedType/PostMediaType/
    PortfolioOwnerType/UserType/BookingStatus/StripePaymentStatus/QueueEntryStatus sont
    volontairement en minuscules cote code ; subscription_plan/salon_staff.role/
    posts.author_type/table legacy `services` ne sont pas mappes en enum par les entites.
  - NB diag : mot de passe du compte test `archtest.lecteur@frollot.test` pose a une
    valeur connue (hash bcrypt) pour permettre les tests curl authentifies.

- **B36 (2026-06-12) : correction des 500 reveles par le test in-app post-B35
  (ecran salon + bouton Suivre). FAIT, teste curl.**
  - **Contexte** : le test in-app de B35 a revele des 500 qui n'etaient PAS une
    regression de B35 mais 3 problemes distincts. NB : le handler d'exception
    generique du backend transforme les `NoHandlerFoundException` (404) en 500,
    ce qui masquait les routes inexistantes.
  - **(a) Mismatch d'URLs follow — frontend `src/api/social.ts`** : les 7 fonctions
    follow appelaient des routes inexistantes (`/api/social/follow/salon/{id}`...).
    Corrigees vers les vraies routes de `FollowController.kt` :
    `POST|DELETE /api/social/salons/{id}/follow`, `POST|DELETE /api/social/coiffeurs/{id}/follow`,
    `GET /api/social/users/{userId}/following`, `GET /api/social/salons|coiffeurs/{id}/followers`.
  - **(b) Mismatch d'URLs reviews — frontend `src/api/reviews.ts`** :
    `getSalonReviewStats` `/api/reviews/salon/{id}/stats` -> `/api/salons/{id}/reviews/stats`
    (endpoint existant, ReviewController.kt:206, DTO identique) ; idem
    `getClientReviews` `/api/reviews/client/{id}` -> `/api/clients/{id}/reviews`
    (meme classe de bug, trouve au passage).
  - **(c) Vrai bug backend — migration `V043__add_is_active_to_salon_staff.sql`** :
    l'entite `SalonStaff.kt` mappe `is_active` mais V001 ne l'avait pas creee
    -> "Unknown column 'ss1_0.is_active'" sur `GET /api/salons/{id}/staff`.
    Colonne `BOOLEAN NOT NULL DEFAULT TRUE` + index `idx_salon_staff_active` ajoutes.
  - **Tests curl apres redemarrage (Flyway v043)** : `GET /salons/{id}/staff`
    500 -> **200** ; `GET /salons/{id}/reviews/stats` **200** (DTO complet) ;
    cycle follow salon authentifie sur les routes corrigees : POST follow **200**
    (followingType SALON), followers **200**, DELETE unfollow **200**,
    feed/following **200**.
  - **A surveiller (non corrige)** : V001 `salon_staff.role` est `ENUM NOT NULL`
    sans defaut et n'est pas mappe par l'entite -> tout INSERT Hibernate dans
    `salon_staff` echouera. A traiter quand la creation de staff sera migree.

- **S9-1 (2026-06-12) : i18n — STRUCTURE posee : renommage des 201 cles fr/en vers le
  schema valide + report es/de/ar + mise a jour des call-sites t()** -- FAIT, tsc 12 =
  baseline (register/index/profile), check-keys : fr 201 / en 201 (0 ecart entre eux),
  es 66 / de 66 / ar 57 avec ZERO orpheline (preuve que le report du renommage est
  correct — chaque cle es/de/ar correspond a une cle fr). AUCUNE traduction nouvelle
  d'ecran, AUCUN texte en dur externalise (ce sera S9-2). Verification exhaustive
  (script node) : les 86 cles distinctes appelees par t() dans app/+src/ resolvent
  toutes en fr ET en (sondage visuel login/settings/booking OK). Aucun appel t()
  dynamique dans le code (que des litteraux) -> renommage par sed fiable.
  - **Restructuration `common.*`** : actions.{retry,cancel,save,delete,edit,confirm,
    back,next,done,search,seeAll,close,share,report,block,follow},
    states.{loading,error,noResults,empty,offline,following},
    fields.{email,password,newPassword,confirmPassword,firstName,lastName,optional},
    validation.{emailRequired,emailInvalid,passwordRequired,passwordTooShort,
    passwordsDoNotMatch}, words.{or,yes,no}.
  - **Table de correspondance (resume — identite pour les cles non citees)** :
    - common.X -> common.actions/states/words.X (21 cles, voir ci-dessus) ;
    - auth.{email,password,newPassword,confirmPassword,firstName,lastName} ->
      common.fields.* (transverses : create-staff, reset-password...) ;
      auth.{emailRequired,emailInvalid,passwordRequired,passwordTooShort,
      passwordsDoNotMatch} -> common.validation.* ;
    - auth.login -> auth.loginButton ; auth.register -> auth.registerTitle ;
      auth.createAccount -> auth.registerButton ; auth.welcome -> auth.welcomeTitle
      (welcomeBack inchangee) ; auth.{client,hairstylist,salonOwner} ->
      auth.userTypes.* ; auth.verifyEmail/-Desc -> verifyEmailTitle/-Hint ;
      auth.invalidCredentials -> auth.invalidCredentialsError ;
    - settings.settings -> settings.title ; profile.profile -> profile.title ;
      social.feed -> social.title ; social.{all,following,trending} ->
      social.tabs.* ; social.{content,postType,visibility} -> *Label ;
      social.writeComment -> social.writeCommentPlaceholder ;
      review.{title,rating,content} -> *Label ; salon.reviews -> review.reviewsTitle
      (utilisee sur les profils, pas que salon) ; salon.follow ->
      common.actions.follow et salon.following -> common.states.following
      (utilisees sur profils coiffeur/salon).
    - **5 DOUBLONS SUPPRIMES** (valeur identique a une cle survivante, call-sites
      rebrancher) : home.queueLive (=home.liveQueue), home.joinQueue
      (=salon.joinQueue), social.share (=common.actions.share), profile.settings
      (=settings.title), salon.verified (=verification.verified).
    - **2 PLURIELS NATIFS i18next** (seule entorse « valeurs inchangees », derivation
      triviale fr/en, es/de/ar n'avaient pas ces cles) : profile.followersCount ->
      _one « {{count}} abonne » / _other « {{count}} abonnes » (fin du « (s) ») ;
      review.totalReviews -> _one/_other (en : review/reviews ; fr : avis/avis).
      Call-sites inchanges (i18next resout _one/_other via {count}).
  - **Comptage** : fr 201 avant = 201 apres (-5 doublons, +2 pluriels, +3 nouvelles
    common) — coincidence arithmetique, contenu different. Nouvelles cles common
    AUTORISEES ajoutees (fr+en seulement, es/de/ar = trous pour S9-3) :
    common.states.empty, common.states.offline, common.fields.optional.
  - **Charpente S9-2** : namespaces vides poses dans fr.json : security, phone,
    report, collections, portfolio (objets {} — 0 cle au sens check-keys, pas
    d'ecart de parite).
  - **Call-sites** : ~160 appels mis a jour par table sed (63 regles, fichier
    temporaire, quote fermante = garde anti-prefixe ex. auth.welcome vs
    auth.welcomeBack) sur app/+src/ ; grep final : ZERO appel vers une ancienne
    cle renommee/supprimee.
  - **.gitignore** : + `scripts/i18n/kmp-reference.json` (regenerable, non versionne).
  - **Reste** : S9-2 (externalisation du texte en dur des ecrans, zone par zone,
    composants partages d'abord, FR d'abord) -> S9-3 (EN puis es/de generes+relus)
    -> S9-4 (ar + RTL natif).

- **S9-2-composants (2026-06-12) : i18n — externalisation du texte FR en dur des
  composants partages (src/components/ UNIQUEMENT, 1re passe S9-2)** -- FAIT.
  - **Inventaire** : 21 composants ; 10 TRAITES (texte propre au composant) :
    PostCard, CollectionPickerModal, LogoutConfirmModal, CountryPickerModal,
    PhoneNumberField, CountryFlag (a11y label decouvert en verification),
    ErrorState, SectionHeader, TextField (PasswordTextField), BookingStepper ;
    11 PROPS-ONLY laisses tels quels (le texte vient de l'ecran appelant, sera
    traduit aux call-sites lors des passes ecrans) : EmptyState, LoadingState,
    Toast, StatusBadge, Chip, Button, Card, Avatar, SalonCard, RatingStars,
    + index/re-exports.
  - **29 nouvelles cles fr+en (ZERO trou EN — toutes les traductions etaient
    courtes/sures)** : auth.{logoutConfirmTitle,logoutConfirmHint,logoutButton} ;
    social.authorTypes.{salonOwner,hairstylist}, social.menu.{viewPost,
    addToCollection,archive,unarchive,pin,unpin}, social.beforeAfter.{before,
    after} ; booking.steps.{service,date,stylist,summary} ; phone.{
    countryPickerTitle,searchPlaceholder,noCountryMatch,numberLabel,
    numberPlaceholder,countryButtonA11y,flagA11y} ; collections.{pickerTitle,
    pickerEmpty,addSuccess,loadError,addError}. Interpolation {{query}}/{{name}}/
    {{iso}}/{{code}} (escapeValue:false verifie en S9-1). es/de/ar NON touches
    (S9-3) ; namespaces security/report/portfolio restent vides.
  - **Cles REUTILISEES** (pas de doublon cree) : common.actions.{cancel,close,
    search,retry,seeAll,delete,report}, common.states.error, common.fields.password.
  - **Patron defaut-de-prop** : un defaut FR en destructuring ne peut pas appeler
    t() -> defaut supprime, `value ?? t('cle')` au rendu (ErrorState.message,
    SectionHeader.action, PhoneNumberField.label, BookingStepper.steps).
    PIEGE corrige : apres `steps ?? [...]` -> stepLabels, le separateur interne
    referencait encore `steps.length` (prop devenue optionnelle) -> stepLabels.length.
  - **Backend prioritaire conserve** (CollectionPickerModal) :
    `error?.response?.data?.message || t('collections.*Error')` — le message
    backend reste affiche tel quel, la cle n'est que le fallback reseau.
  - **Verifications** : check-keys fr 230 / en 230 (0 ecart, 0 orpheline ; es/de
    164 et ar 173 manquantes = perimetre S9-3, 0 orpheline) ; script node : les
    38 cles t() distinctes de src/components resolvent TOUTES en fr ET en ; grep
    accents/guillemets sur src/components : ne restent QUE des commentaires/
    docstrings FR (autorises) ; tsc = 12 erreurs baseline exactes ; Metro :
    rebundle web complet 200 (1696 modules) sans erreur apres edits.
  - **Reste S9-2** : passes ecrans app/ zone par zone (settings non-legaux,
    salon/booking, social/post, 2FA/verification/report).

- **S9-2-settings-lotA (2026-06-12) : i18n — externalisation FR de settings/index +
  change-email + change-phone** -- FAIT. (Decoupage valide : lot B = security.tsx
  seul, prompt suivant.)
  - **57 nouvelles cles fr+en, ZERO trou EN** :
    - settings.* (36) : yourSpace, soon, versionLabel ({{version}}), backToSettings,
      passwordConfirmPlaceholder (partage email+phone, saisie + modale),
      verificationRequest ; sections.{account,privacySecurity,support,legal,
      appearance} ; subtitles.{security,blockedUsers,verification,help,contact} ;
      themeModes.{system,light,dark} ; email.{intro,currentLabel,newLabel,
      newPlaceholder,newRequiredError,sameAsCurrentError,sendCodeButton,sendError,
      resendError,resendSuccess({{email}}),codeTitle,codeSentPrefix,
      pendingHint({{email}}),codeLabel,codeLengthError,codeInvalidError,
      resendButton,editAddressButton,successTitle,successPrefix,successSuffix}
      (PAS settings.changeEmail.* : la cle settings.changeEmail est deja une
      CHAINE, un objet homonyme serait un conflit JSON -> sous-groupe
      settings.email.*).
    - phone.* (18) : changeIntro, loadingCurrent, loadError, publicTitle,
      privateTitle, publicHint, privateHint, transactionalNote, saveError,
      savedPublicSuccess, savedPrivateSuccess, deleteButton, deleteConfirmTitle,
      deleteConfirmHint, deleteError, deletedSuccess, successTitle.
  - **Reutilisations** : phone.numberLabel (titre ecran + fallback sous-titre hub),
    common.actions.{retry,save,cancel,delete,confirm}, common.fields.password,
    common.validation.{emailInvalid,passwordRequired} (textes legerement normalises
    vs originaux, sens identique), settings.* deja peuples (changeEmail, security...).
  - **Patrons** : THEME_MODES = constante module -> labelKey + t() au rendu ;
    email en gras au milieu d'une phrase -> cles Prefix/Suffix autour du <Text>
    imbrique (codeSentPrefix, successPrefix/successSuffix) pour ne pas perdre le
    style ; fallbacks reseau = `data?.message || t('...')` (backend prioritaire,
    inchange) ; ENDONYMES de langues (Francais/English/...) laisses en dur
    (standard i18n) ; placeholder OTP "000000" neutre laisse tel quel.
  - **Verifications** : check-keys fr 287 / en 287 (0 ecart, 0 orpheline ;
    es/de 221, ar 230 manquantes = S9-3, 0 orpheline) ; 75 cles t() distinctes des
    3 ecrans resolvent TOUTES en fr ET en (script node) ; grep : ne restent que
    des commentaires FR ; security.tsx + terms/privacy/help/contact : git diff
    VIDE (intacts, prouve) ; tsc = 12 baseline ; Metro rebundle web 200
    (1696 modules) sans erreur.
  - **Reste** : lot B (security.tsx seul) puis salon/booking, social/post,
    2FA/verification/report.

- **S9-2-settings-lotB (2026-06-13) : i18n — externalisation FR de
  app/settings/security.tsx (1228 lignes, 4 blocs)** -- FAIT.
  - **55 cles security.* (fr+en), 0 trou EN, 0 orpheline** :
    - security.passwordPlaceholder (partage : delete-account, disable-2FA, regen-codes)
    - security.password.* (11) : currentLabel, currentPlaceholder, confirmPlaceholder,
      minLengthRule ({{count}}), differentRule, matchRule, changeError, revokeAllHint,
      successTitle, successHint, reloginButton.
    - security.sessions.* (16) : intro, loadingLabel, loadError, emptyTitle, emptyHint,
      currentBadge, deviceMobile, deviceTablet, deviceDesktop, deviceUnknown,
      lastActivity ({{date}}), createdAt ({{date}}), revokeAllButton,
      revokeAllConfirmTitle, revokeAllConfirmHint_one/_other ({{count}} PLURIEL),
      revokeButton, revokeError, revokeAllError.
    - security.twoFactor.* (25) : title, statusLoading, statusError, enabledBadge,
      enabledHint, disabledHint, enableButton, disableButton, regenButton,
      setupLoading, setupError, scanStep, manualKeyHint, codeStep, codeLabel,
      codeInvalidError, recoveryTitle, recoveryOldInvalid, recoveryHint, copyButton,
      copiedLabel, recoveryWarning, recoveryAck, disableConfirmTitle,
      disableConfirmHint, codeOrRecoveryLabel, codeOrRecoveryPlaceholder,
      disableError, regenConfirmTitle, regenConfirmHint, regenConfirmButton,
      regenError.
    - security.dangerZone.* (10) : title, deleteButton, warning, confirmTitle,
      confirmHint, continueButton, deleteError, finalTitle, finalHint, finalButton.
  - **Reutilisations** (pas de doublons crees) : common.actions.{cancel,confirm,done},
    common.fields.{password,newPassword,confirmPassword},
    common.validation.passwordsDoNotMatch, settings.{changePassword,
    sections.privacySecurity,security,activeSessions}.
  - **Pluriel** : revokeAllConfirmHint_one/_other ({{count}} sessions).
  - **Interpolation** : minLengthRule {{count}}, lastActivity/createdAt {{date}},
    revokeAllConfirmHint {{count}}.
  - **Erreurs backend** : pattern inchange (error?.response?.data?.message || t(...)),
    fallback i18n RN seulement ; AUCUNE erreur backend traduite cote client.
  - **Logique sensible INTACTE** (zero changement condition/state/hook/API/navigation) :
    revoke session, revoke all, delete account (double confirm), 2FA wizard QR,
    2FA confirm, 2FA disable, recovery codes regen — tous verifie par lecture.
  - **Note** : formatDate() (L55-59) garde 'fr-FR' + ' a ' en dur ; c'est un
    formateur de locale, pas un label — a traiter en lot cross-cutting date-formatting
    (hors perimetre i18n ecran).
  - **Verifications** : check-keys fr=360 / en=360, 0 ecart, 0 orpheline ;
    es/de/ar inchanges (S9-3) 0 orpheline ; grep JSX = 0 chaine FR en dur ;
    tsc = 12 baseline ; git diff scope = fr.json + en.json uniquement (security.tsx
    est untracked/nouveau). 4 legaux + 3 ecrans lot A INTACTS (prouve).
  - **ZONE SETTINGS = ENTIEREMENT EXTERNALISEE EN FR** (hors 4 legaux exclus a dessein).
  - **Reste** : salon/booking, social/post, profiles, ecrans auth restants.

- **S9-2-salon-booking (2026-06-13) : i18n — externalisation FR des ecrans
  zones SALON et BOOKING (7 ecrans, booking/[id].tsx deja fait)** -- FAIT.
  - **73 cles creees (fr+en 360->433), 0 trou EN, 0 orpheline** :
    - salon.* (14 nouvelles) : notFound, bookService, reservations, queueOpen ({{minutes}}),
      queueWaiting_one/_other ({{count}} PLURIEL), queueLeaving, noPosts, postsHeader,
      postsHeaderWithName ({{name}}), noPostsTitle, noPostsMessage, postsLoadError, allServices.
      Valeurs corrigees : salon.posts "Publications"->"Posts", salon.info "Informations"->"Info".
    - social.* (14 nouvelles) : postTypes.all, sort.recent, sort.popular, likeError, bookmarkError,
      shareUnavailable, postArchived, archiveError, postDeleted, deleteError, postUnpinned,
      postPinned, pinError. Accents corriges : postTypes.avantApres, postTypes.realisation.
    - booking.* (25 nouvelles) : title, salonLabel, choose, selectStaff, anyStaff, anyStaffHint,
      noStaffFound, availableSlots, noSlots, summaryLabel.{service,stylist,date,duration,price},
      notesLabel, notesPlaceholder, bookingError, success.{title,message,withStaff ({{name}}),
      viewBooking,backHome}, filter.{all,pending,confirmed,inProgress,completed,cancelled},
      loadError, total.
    - service.* (3) : createdSuccess ({{name}}), durationUnit, priceUnit.
    - staff.* (6) : notHairstylist, userNotFound, addedSuccess ({{name}}), alreadyMember,
      notAuthorized, autoCreateHint.
    - review.* (7) : ratingRequired, successMessage, rating.{bad,average,good,veryGood,excellent}.
  - **Reutilisations** (pas de doublons crees) : salon.book, salon.queue, salon.leaveQueue,
    salon.address, salon.description, review.reviewsTitle, review.totalReviews_one/_other,
    booking.steps.{service,stylist,date,summary}, booking.continue, booking.summary,
    booking.cancelBooking, booking.cancelConfirm, booking.noBookings, service.minutes,
    service.duration, service.price, service.category, service.serviceName, service.createService,
    staff.addStaff, staff.specialties, review.writeReview, review.ratingLabel, review.titleLabel,
    review.contentLabel, review.submit, common.actions.{seeAll,follow,confirm,done,cancel,retry},
    common.states.{error,following}, common.fields.{email,firstName,lastName,optional},
    social.postTypes.{avantApres,realisation,tendance,conseil,inspiration}.
  - **Pluriels** : salon.queueWaiting_one/_other ({{count}}), review.totalReviews (reutilise).
  - **Interpolations** : salon.queueOpen {{minutes}}, salon.postsHeaderWithName {{name}},
    booking.success.withStaff {{name}}, service.createdSuccess {{name}}, staff.addedSuccess {{name}},
    service.minutes {{count}}, review.totalReviews {{count}}, salon.queueWaiting {{count}}.
  - **Dates locale SIGNALEES (dette transverse, hors perimetre)** :
    booking/new.tsx:15-16 MONTHS/DAYS_SHORT arrays FR ;
    booking/new.tsx:124,174,301,357,409,454,469 formatage avec MONTHS/DAYS_SHORT ;
    booking/[id].tsx:158,179 toLocaleDateString/toLocaleTimeString ;
    owner-bookings.tsx:179 toLocaleDateString/toLocaleTimeString.
  - **Erreurs backend** : pattern inchange (error?.response?.data?.message || t(...)),
    fallback i18n RN seulement ; AUCUNE erreur backend traduite cote client.
    create-staff.tsx:83 msg?.includes('existe deja') = test conditionnel sur backend (non affiche).
  - **Logique metier INTACTE** (zero changement condition/state/hook/API/navigation) :
    reservation, choix service/staff/creneau, file d'attente join/leave, follow/unfollow,
    post actions (like/bookmark/share/archive/delete/pin), pagination, refresh — tous verifies.
  - **Fichiers couverts** : app/salon/[id].tsx, app/salon/[id]/posts.tsx, app/booking/new.tsx,
    app/create-service.tsx, app/create-staff.tsx, app/create-review.tsx, app/owner-bookings.tsx.
    app/booking/[id].tsx = deja externalise (aucune modif).
  - **Verifications** : check-keys fr=433 / en=433, 0 ecart, 0 orpheline ;
    es/de/ar inchanges (S9-3) 0 orpheline ; grep JSX = 0 chaine FR en dur
    (hors MONTHS/DAYS_SHORT date locale + condition backend create-staff:83) ;
    tsc = 12 baseline ; settings/*, src/components/*, 4 legaux INTACTS (prouve).
  - **Reste** : social/post, profiles, ecrans auth restants.

- **S9-2-social-post (2026-06-13) : i18n — externalisation FR des ecrans
  zones SOCIAL, POST et ecrans racine (7 ecrans)** -- FAIT.
  - **74 cles creees (fr+en 433->507), 0 trou EN, 0 orpheline** :
    - social.* (17 nouvelles) : publication, loadMore, commentError, loadCommentsError,
      deleteCommentError, deletePostTitle, deletePostConfirm, publishButton,
      visibility.{public,followers,private}, compose.{contentRequired,beforeAfterMinImages,media},
      hashtagCount_one/_other ({{count}} PLURIEL),
      trending.{posts,hashtags,salons,last24h,last7d,last30d}.
    - report.* (25 nouvelles, namespace PEUPLE) : overline, title, intro,
      entityType.{post,comment,user,salon}, postAuthor, reasonTitle,
      reasons.{inappropriate,inappropriateDesc,spam,spamDesc,fake,fakeDesc,copyright,
      copyrightDesc,other,otherDesc}, detailsTitle, detailsPlaceholder, submitButton,
      submitError, successTitle, successMessage.
    - collections.* (22 nouvelles, namespace ETENDU) : overline, detailOverline, emptyTitle,
      emptyOwnerMessage, emptyOtherMessage, createButton, newTitle, nameLabel, namePlaceholder,
      nameRequired, descriptionLabel, descriptionPlaceholder, categoryLabel, publicLabel,
      createError, deleteError, deleteTitle, deleteMessage ({{name}}), createAction,
      detailEmpty, detailEmptyMessage, postCount_one/_other ({{count}} PLURIEL),
      category.{inspiration,portfolio,tendance,personnel}.
  - **Reutilisations** (pas de doublons crees) : social.{likeError,bookmarkError,shareUnavailable,
    postArchived,archiveError,postDeleted,deleteError,postPinned,postUnpinned,pinError},
    social.menu.{addToCollection,archive,pin,unpin}, social.postTypes.*, social.{createPost,
    contentLabel,postTypeLabel,visibilityLabel,comments,noComments,noPosts,writeCommentPlaceholder},
    social.tabs.trending, common.actions.{cancel,delete,report,back,retry,confirm},
    common.states.error, collections.loadError, profile.collections.
  - **Pluriels** : social.hashtagCount_one/_other ({{count}}),
    collections.postCount_one/_other ({{count}}).
  - **Interpolations** : social.hashtagCount {{count}}, collections.postCount {{count}},
    collections.deleteMessage {{name}}.
  - **Dates locale** : AUCUNE nouvelle dette dans ce lot (pas de date formatting dans ces ecrans).
  - **Erreurs backend** : pattern inchange (error?.response?.data?.message || t(...)),
    fallback i18n RN seulement ; AUCUNE erreur backend traduite cote client.
  - **Logique sensible INTACTE** (zero changement condition/state/hook/API/navigation) :
    like/favorite/share/pin/archive/delete post, commentaire CRUD, creation post (mentions,
    hashtags, media upload, reconciliation tags), signalement, pagination, creation/suppression
    collection — tous verifies par lecture.
  - **Fichiers couverts** : app/post/[id].tsx, app/comments/[id].tsx, app/create-post.tsx,
    app/trending.tsx, app/report.tsx, app/collections/[id].tsx, app/collections/user/[userId].tsx.
  - **Verifications** : check-keys fr=507 / en=507, 0 ecart, 0 orpheline ;
    es/de/ar inchanges (S9-3) 0 orpheline ; grep JSX = 0 chaine FR en dur ;
    tsc = 12 baseline ; metro bundles 5/5 = 200 ;
    settings/*, src/components/*, salon/booking/*, 4 legaux INTACTS (prouve).
  - **Reste** : profiles, ecrans auth restants (two-factor, verify-email, auth en dur),
    tabs (social, explore, index), puis S9-3 (remplissage es/de generes/relus).

- **S9-2-auth-tabs-profils (2026-06-13) : i18n — externalisation FR de TOUS les ecrans
  restants (16 ecrans) + CLOTURE PHASE FR** -- FAIT.
  - **116 cles creees (fr+en 507->623), 0 trou EN, 0 orpheline** :
    - auth.* (32 nouvelles) : accountDisabled, tooManyAttempts, loginSuccessRedirect,
      legal.{continuingAccept,creatingAccept,terms,conjunction,privacy},
      register.{overline,headline,subtitle,accountTypeLabel,signUp,error,passwordPlaceholder,
      confirmPlaceholder,accountSub.{client,hairstylist,salonOwner},
      strength.{weak,medium,good,strong}},
      twoFactor.{title,subtitle,codeLabel,codePlaceholder,codeRequired,verifyButton,
      backToLogin,hint,verifyError,cancelA11y}.
    - home.* (22 nouvelles) : greetingMessage, searchBarPlaceholder,
      category.{all,cut,color,care,beard,styling}, loadError, noSalonTitle, noResultTitle,
      noSalonMessage, noResultMessage, createSalonButton, popularSalons,
      salonsCategory ({{category}}), queueOverline, queueTitle ({{name}}), queueButton,
      cityLabel, cityDialogTitle, cityDialogPlaceholder, cityApply.
    - social.* (10 nouvelles) : searchPlaceholder, searchPosts, searchSalons, searchUsers,
      feedLoadError, feedEmptyTitle, feedEmptyMessage, unarchiveTitle, unarchiveMessage,
      deletePostMessage.
    - booking.* (11 nouvelles) : tabFilter.{all,upcoming,past}, emptyTitle, emptyUpcoming,
      emptyPast, emptyAll, leaveReview, cancelModalTitle, cancelModalMessage,
      cancelKeep, cancelYes, cancelling.
    - profile.* (18 nouvelles) : stats.{bookings,collections,posts,salons,followers,likes},
      memberSince ({{year}}), pinnedPosts, bio, experience, viewFullSalon,
      archivesOverline, archivesTitle, archivesLoadError, archivesEmpty, archivesEmptyMessage,
      favoritesPrivate, favoritesLoadError, favoritesEmpty, removeFavoriteError.
    - portfolio.* (9 nouvelles, namespace PEUPLE) : title, create, empty, salonLabel,
      namePlaceholder, descriptionPlaceholder, publicLabel, created, createButton.
    - queue.* (8 nouvelles, namespace CREE) : loadError, callError, removeError, waiting,
      called, waitTime, callButton, removeButton.
  - **Reutilisations massives** : social.{likeError,bookmarkError,shareUnavailable,postArchived,
    archiveError,postDeleted,deleteError,postPinned,postUnpinned,pinError,menu.*,tabs.*,
    postTypes.*,title,noPosts,noComments,writeCommentPlaceholder,createPost,contentLabel,
    postTypeLabel,visibilityLabel}, auth.{loginButton,registerButton,welcomeBack,welcomeTitle,
    loginSubtitle,forgotPassword,userTypes.*,alreadyHaveAccount,invalidCredentialsError},
    common.{actions.*,fields.*,validation.*,words.or,states.*}, booking.{myBookings,loadError},
    profile.{favorites,archives,collections,portfolios}, settings.{title,logout},
    verification.verified.
  - **Fichiers couverts (16)** : app/(auth)/login.tsx, register.tsx, two-factor.tsx ;
    app/(tabs)/social.tsx, bookings.tsx, index.tsx, profile.tsx ;
    app/archives/[userId].tsx, favorites/[userId].tsx ;
    app/profile/{client,coiffeur,owner,salon}/[id].tsx ;
    app/queue-management.tsx, portfolios.tsx, create-portfolio.tsx.
  - **Fichiers confirmes DEJA FAITS (3)** : forgot-password.tsx, reset-password.tsx, explore.tsx.
  - **Conditions backend NON traduites (signalees)** : login.tsx:47 .includes('verifiee'),
    two-factor.tsx:86 .includes('reconnecter'), create-staff.tsx:83 .includes('existe deja').
  - **Dates locale SIGNALEES (dette transverse ajoutee)** :
    index.tsx:68 toLocaleDateString('fr-FR'),
    bookings.tsx:106 toLocaleDateString/toLocaleTimeString.
  - **Endonymes de langues** (settings/index.tsx:16-20, Francais/English/Espanol/Deutsch/العربية)
    = intentionnellement non traduits (chaque langue dans son propre nom).
  - **Verifications** : check-keys fr=623 / en=623, 0 ecart, 0 orpheline ;
    tsc = 12 baseline ; metro bundles 9/9 = 200 ;
    grep de cloture = ZERO chaine FR en dur affichee dans tout app/
    (hors 4 legaux exclus, hors commentaires, hors dette dates locale, hors conditions
    backend .includes, hors endonymes de langues).
  - **PHASE FR DE L'ETAPE 9 TERMINEE** — toute l'app (hors 4 ecrans legaux exclus a dessein)
    est externalisee en FR ; check-keys fr=en=623 alignes ; reste S9-3 (remplissage es/de
    generes + relus par sous-agents) puis S9-4 (ar + RTL natif).

- **S9-3-lot1 (2026-06-13) : i18n — traduction es+de des namespaces common/auth/home/verification
  + correction accents/umlauts des 66 cles existantes** -- FAIT.
  - **70 cles nouvelles par langue** (common 5 + auth 39 + home 23 + verification 3).
    NB: 70 et non 63 comme estime — les cles auth manquantes etaient 39, pas 32.
  - **Corrections accents existants** :
    - ES (~20 valeurs) : electronico→electrónico, Contrasena→Contraseña (×8), sesion→sesión (×3),
      Si→Sí, valido→válido, Dueno→Dueño, salon→salón, Categorias→Categorías,
      ajout ¿ en tete de 4 questions (forgotPassword, alreadyHaveAccount, noAccount, greetingMessage).
    - DE (~14 valeurs) : Loschen→Löschen, Bestatigen→Bestätigen (×3), Zuruck→Zurück (×3),
      Schliessen→Schließen, ungultig→ungültig (×2), Passworter→Passwörter, uberein→überein,
      Nahe→Nähe, Bestatigungs→Bestätigungs.
  - **Relecture par 2 sous-agents (ES + DE), corrections appliquees** :
    - ES : gender agreement password strength (Medio→Media, Bueno→Buena, Robusto→Robusta,
      car "contraseña" est feminin en espagnol).
    - DE : capitalisation "Buchen & folgen" → "Buchen & Folgen" ; semantique "Gefolgt" → "Abonniert"
      (Gefolgt = "suivi par qqn", pas "je suis") ; accord grammatical invalidCredentialsError
      "Ungültige E-Mail oder Passwort" → "Ungültige E-Mail oder falsches Passwort".
  - **Glossaire terminologique** :
    - ES : compte=cuenta, connexion=sesión, mot de passe=contraseña, email=correo electrónico,
      salon=salón, coiffeur=estilista, reservation=cita, abonne=seguidor, bienvenue=bienvenido/a,
      verification=verificación, code=código, authentification=autenticación.
    - DE : compte=Konto, connexion=Anmeldung, mot de passe=Passwort, email=E-Mail,
      salon=Salon, coiffeur=Friseur, reservation=Termin, abonne=Follower, bienvenue=Willkommen,
      verification=Verifizierung, code=Code, authentification=Authentifizierung. Registre=Sie.
  - **Interpolations verifiees (4/4 es, 4/4 de)** : auth.verifyEmailHint {{email}},
    home.greeting {{name}}, home.salonsCategory {{category}}, home.queueTitle {{name}}.
  - **Pluriels** : aucune cle _one/_other dans les namespaces du lot.
  - **Libelles longs signales (allemand surtout)** :
    - home.queueButton "Der Warteschlange beitreten" (+56% vs FR) — risque debordement bouton.
    - home.createSalonButton "Meinen Salon erstellen" (+47%).
    - common.actions.retry "Erneut versuchen" (+78%) — acceptable dans un snackbar large.
    - auth.loginButton ES "Iniciar sesión" (+56% vs "Connexion") — standard inevitable.
  - **Etat check-keys apres lot 1** : es=136 cles, 487 manquantes, 0 orpheline ;
    de=136 cles, 487 manquantes, 0 orpheline. JSON valides. tsc=12 baseline inchangee.
    git diff --stat = UNIQUEMENT es.json + de.json.
  - **Reste S9-3** : lots 3-6 (salon+booking, settings+security, profile+collections+report+phone,
    review+payment+service+staff+portfolio+queue). ar inchange (S9-4).

- **S9-3-lot2 (2026-06-14) : i18n — traduction es+de du namespace social (77 cles/langue)**
  - **Correction retroactive Lot 1 ES** : 3 slashs de genre supprimes (auth.welcomeBack
    "Bienvenido/a de nuevo" → "Te damos la bienvenida de nuevo", auth.welcomeTitle
    "Bienvenido/a" → "Te damos la bienvenida", home.greetingMessage "¿listo/a..." →
    "¿todo listo...").
  - **77 cles social ajoutees** a es.json et de.json. Toutes les sous-structures preservees
    (tabs, authorTypes, menu, beforeAfter, postTypes, sort, visibility, compose, trending).
  - **Terminologie social ajoutee au glossaire** :
    - ES : publication=publicación, commentaire=comentario, aimer=Me gusta,
      favori=favorito, partager=compartir, fixer=fijar, archiver=archivar,
      tendance=tendencia, hashtag=hashtag, abonné=seguidor.
    - DE : publication=Beitrag, commentaire=Kommentar, aimer=Gefällt mir,
      favori=Favorit, partager=Teilen, fixer=Anheften, archiver=Archivieren,
      tendance=Trend, hashtag=Hashtag, abonné=Follower.
  - **Interpolations** : {{count}} dans hashtagCount_one/_other — verifie OK (es+de).
  - **Pluriels** : hashtagCount_one/other — regles es/de respectees.
  - **Slashs de genre ES Lot 2** : aucun (toutes formulations neutres ou feminines
    naturelles via "publicación").
  - **Registre Sie DE** : verifie, aucun du-form.
  - **Correctifs post-relecture** : DE "Lösen" → "Loslösen" (unpin, coherence avec
    Anheften) ; DE "Das Feed" → "Der Feed" (genre masculin) ; DE likeError reformule
    sans guillemets allemands (echappement JSON).
  - **Libelles longs signales (non tronques, a tester en main)** :
    - DE publishButton "Veröffentlichen" (15 car vs FR 7) — risque bouton compose.
    - DE menu.addToCollection "Zu einer Sammlung hinzufügen" (29 car vs FR 24).
    - DE trending.last24h "24 Std." (7 car vs FR 3) — risque chip compact.
    - ES tabs.following "Siguiendo" (9 car vs FR 6) — mineur.
  - **Etat check-keys apres lot 2** : es=213 cles, 410 manquantes, 0 orpheline ;
    de=213 cles, 410 manquantes, 0 orpheline. JSON valides. tsc=12 baseline inchangee.
    git diff --stat = UNIQUEMENT es.json + de.json + plan.
  - **Reste S9-3** : lots 4-6 (settings+security, profile+collections+report+phone,
    review+payment+service+staff+portfolio+queue). ar inchange (S9-4).

- **S9-3-lot3 (2026-06-14) : i18n — traduction es+de des namespaces salon (30 cles) + booking
  (69 cles) = 99 cles/langue**
  - **Terminologie salon+booking ajoutee au glossaire** :
    - ES : service=servicio, creneau=horario, file d'attente=cola, avis=reseña,
      annuler=cancelar, confirmer=confirmar, etape=paso, resumen=resumen.
    - DE : service=Leistung, creneau=Zeitfenster, file d'attente=Warteschlange,
      avis=Bewertung, annuler=stornieren, confirmer=bestätigen, etape=Schritt,
      resume=Übersicht. Coiffeur=Friseur/in.
  - **Coherence terminologique verifiee** : reservation=cita(ES)/Termin(DE) partout
    (zero reserva/Buchung/Reservierung) ; service=servicio/Leistung partout (zero
    prestación/Dienstleistung) ; creneau=horario/Zeitfenster ; queue=cola/Warteschlange.
  - **Interpolations** : {{minutes}} (queueWait, queueOpen), {{count}} (queueWaiting),
    {{name}} (postsHeaderWithName, success.withStaff), {{current}}+{{total}} (step) —
    toutes verifiees OK (es+de).
  - **Pluriels** : queueWaiting_one/_other — formes identiques (miroir du FR source,
    le nombre seul suffit en contexte compact). Acceptable.
  - **Slashs de genre ES** : 0. Formulations neutres appliquees (anyStaffHint =
    "Primera persona disponible").
  - **Registre Sie DE** : verifie, aucun du-form.
  - **Correctifs post-relecture** : DE joinQueue "In die Warteschlange einreihen"
    (31 car, +72%) → "Anstellen" (9 car) ; DE leaveQueue "Warteschlange verlassen"
    (23 car, +53%) → "Verlassen" (9 car).
  - **Libelles longs signales (non tronques, a tester en main)** :
    - DE booking.steps.summary "Übersicht" (9 car vs FR "Recap" 5 car, +80%) — stepper.
    - DE salon.queueOpen "Warteschlange offen · ~{{minutes}} Min. Wartezeit" — long
      mais texte informatif (pas un bouton).
  - **Etat check-keys apres lot 3** : es=312 cles, 311 manquantes, 0 orpheline ;
    de=312 cles, 311 manquantes, 0 orpheline. JSON valides. tsc=12 baseline inchangee.
    git diff --stat = UNIQUEMENT es.json + de.json + plan.
  - **Reste S9-3** : lots 4b-6 (security, profile+collections+report+phone,
    review+payment+service+staff+portfolio+queue). ar inchange (S9-4).

- **S9-3-lot4a (2026-06-14) : i18n — traduction es+de du namespace settings (61 cles/langue)**
  - **Terminologie settings ajoutee au glossaire** :
    - ES : parametres=ajustes, theme=tema, langue=idioma, mode sombre=modo oscuro,
      mode clair=claro, systeme=sistema, notifications=notificaciones, session=sesión,
      badge=insignia.
    - DE : parametres=Einstellungen, theme=Design, langue=Sprache, mode sombre=Dunkelmodus,
      hell/dunkel=Hell/Dunkel, systeme=System, notifications=Benachrichtigungen,
      session=Sitzung, apparence=Design (aligne avec theme).
  - **Interpolations** : {{version}} (versionLabel), {{email}} (resendSuccess, pendingHint)
    — verifiees OK (es+de).
  - **Slashs de genre ES** : 0.
  - **Registre Sie DE** : verifie, aucun du-form.
  - **Correctifs post-relecture** :
    - DE subtitles.verification raccourci (36→33 car, -"Das").
    - DE sections.appearance "Darstellung" → "Design" (aligne avec settings.theme).
  - **Note ES** : email.currentLabel/newLabel/successTitle utilisent "correo" (sans
    "electronico") — choix delibere pour labels courts, coherent avec FR source "Email".
  - **Libelles longs signales** :
    - ES changeEmail "Cambiar correo electronico" (26 car vs FR 15) — ligne parametres.
    - DE subtitles.verification "Verifizierungsabzeichen erhalten" (33 car vs FR 24).
  - **Etat check-keys** : es=373 cles, 250 manquantes, 0 orpheline ; de=373, 250, 0.
    JSON valides. tsc=12 baseline inchangee.
  - **Reste S9-3** : lots 5-6. ar inchange (S9-4).

- **S9-3-lot4b (2026-06-14) : i18n — traduction es+de du namespace security (73 cles/langue)**
  - **ZONE SETTINGS+SECURITY ES/DE COMPLETE** (lots 4a+4b = 134 cles/langue).
  - **Terminologie security ajoutee au glossaire** :
    - ES : code de recuperation=código de recuperación, zone de danger=zona de peligro,
      revoquer/deconnecter=cerrar sesión (bouton+erreur aligne), 2FA=autenticación en
      dos pasos, wiederherstellungscode=—.
    - DE : code de recuperation=Wiederherstellungscode, zone de danger=Gefahrenzone,
      deconnecter=abmelden (bouton+erreur aligne), 2FA=Zwei-Faktor-Authentifizierung.
  - **Coherence sous-titre Lot 4a** verifiee : contraseña/Passwort, sesión/Sitzung,
    eliminación/Löschung, 2FA — identiques entre settings.subtitles.security et
    security.*.
  - **Interpolations** : {{count}} (minLengthRule, revokeAllConfirmHint), {{date}}
    (lastActivity, createdAt) — verifiees OK (es+de).
  - **Pluriels** : revokeAllConfirmHint_one/_other — regles es/de correctes (verbe +
    nom changent : sesión/sesiones, Sitzung/Sitzungen).
  - **Sens des avertissements** : danger zone warning/confirmHint/finalHint preservent
    exactement la severite et l'irreversibilite du FR. Aucune edulcoration.
  - **Slashs de genre ES** : 0. **Registre Sie DE** : 0 du-form.
  - **Correctifs post-relecture** : revokeError aligne avec le verbe du bouton (ES
    "revocar" → "cerrar", DE "widerrufen" → "abmelden").
  - **Libelles longs signales** :
    - ES reloginButton "Iniciar sesión de nuevo" (23 car vs FR 14) — bouton post-mdp.
    - ES revokeAllButton "Cerrar sesión en los otros dispositivos" (39 car vs FR 32).
    - DE enableButton "Zwei-Faktor-Authentifizierung aktivieren" (41 car vs FR 35).
  - **Etat check-keys** : es=446 cles, 177 manquantes, 0 orpheline ; de=446, 177, 0.
    JSON valides. tsc=12 baseline inchangee.
  - **Reste S9-3** : lot 6 (review+payment+service+staff+portfolio+queue). ar inchange (S9-4).

- **S9-3-lot5 (2026-06-14) : i18n — traduction es+de des namespaces profile (30) + collections
  (32) + report (25) + phone (24) = 111 cles/langue**
  - **Terminologie ajoutee au glossaire** :
    - ES : profil=perfil, archive=archivo, insignia=insignia, reporte=reporte,
      autoría=autoría (neutre pour postAuthor), indicativo=indicativo, público/privado
      (phone coherent), prestación=prestación.
    - DE : profil=Profil, archiv=Archiv, abzeichen=Abzeichen, meldung=Meldung,
      vorwahl=Vorwahl, öffentlich/privat (phone coherent), leistung=Leistung.
  - **Coherence terminologique verifiee** : signaler=reportar/melden, colección/Sammlung,
    favorito/Favorit, seguidor/Follower, publicación/Beitrag, salón/Salon — tous
    coherents avec les lots precedents. Aucun doublon synonymique.
  - **Interpolations** : {{count}} (followersCount, postCount), {{year}} (memberSince),
    {{name}} (addSuccess, deleteMessage), {{query}} (noCountryMatch), {{name}}+{{code}}
    (countryButtonA11y), {{iso}} (flagA11y) — toutes verifiees OK (es+de).
  - **Pluriels** : followersCount_one/_other (seguidor/seguidores, Follower/Follower),
    postCount_one/_other (publicación/publicaciones, Beitrag/Beiträge) — corrects.
  - **Slashs de genre ES** : 0. **Registre Sie DE** : 0 du-form.
  - **Correctifs post-relecture** : transactionalNote corrige dans les 2 langues —
    le modal/subjonctif (ES "podrá ver"/"tengas", DE "kann...einsehen") remplace par
    l'indicatif present (ES "ve"/"tienes", DE "sieht") pour preserver le ton declaratif
    factuel du FR ("voit"). Avertissement non edulcore.
  - **Libelles longs signales** :
    - DE viewFullSalon "Vollständigen Salon ansehen →" (29 car vs FR 22, +32%).
    - DE transactionalNote (126 car vs FR 100, +26%) — texte informatif, pas un bouton.
  - **Etat check-keys** : es=557 cles, 66 manquantes, 0 orpheline ; de=557, 66, 0.
    JSON valides. tsc=12 baseline inchangee.
  - **Reste S9-3** : lot 6 (dernier).

- **S9-3-lot6 (2026-06-14) : i18n — traduction es+de des namespaces review (17) + payment (12)
  + service (10) + staff (10) + portfolio (9) + queue (8) = 66 cles/langue — DERNIER LOT**
  - **Terminologie lot 6 ajoutee au glossaire** :
    - ES : reseña, nota (rating), pago/pagado, monto, servicio, estilista, integrante
      (addStaff neutre), portfolio, cola, llamar/retirar (queue actions).
    - DE : Bewertung, Note (rating), Zahlung/bezahlt, Betrag, Leistung, Friseur,
      Mitglied, Portfolio, Warteschlange, aufrufen/entfernen (queue actions).
  - **Coherence terminologique verifiee** (0 doublon) : avis=reseña/Bewertung,
    service=servicio/Leistung, file=cola/Warteschlange, coiffeur=estilista/Friseur,
    paiement=pago/Zahlung — tous identiques aux lots precedents.
  - **Correctif cross-namespace** : DE home.searchBarPlaceholder "Dienstleistung" →
    "Leistung" (glossary violation detectee par relecture finale).
  - **Interpolations** : {{rating}} (averageRating), {{count}} (totalReviews, minutes),
    {{name}} (createdSuccess, addedSuccess) — verifiees OK (es+de).
  - **Pluriels** : totalReviews_one/_other corrects (reseña/reseñas,
    Bewertung/Bewertungen).
  - **Genre ES** : 0 slash. Formulations neutres appliquees (addStaff="Añadir
    integrante", addedSuccess reflexif "se ha añadido").
  - **Registre Sie DE** : verifie, 0 du-form.
  - **Libelles longs signales** :
    - DE review.submit "Bewertung veröffentlichen" (25 car vs FR 15, +67%).
    - DE review.writeReview "Bewertung schreiben" (19 car vs FR 14, +36%).
    - DE payment.pay "Bezahlen" (9 car vs FR 5) — inevitable.
  - **Etat check-keys** : **es=623, de=623, 0 manquante, 0 orpheline** — PARITE TOTALE.
    JSON valides. tsc=12 baseline inchangee.

- **=== S9-3 TERMINEE === es et de complets (623 cles chacun, parite fr/en/es/de totale).**

- **S9-4a-diagnostic (2026-06-14) : pluriels CLDR arabes + ajustement check-keys.js**
  - i18next v26.3.1 + compatibilityJSON v4 : 6 suffixes CLDR pour ar confirmes
    (_zero, _one, _two, _few, _many, _other). Aucune modif i18next requise.
  - check-keys.js : ajout de isPluralVariant() qui tolere les formes de pluriel CLDR
    supplementaires (ar) comme non-orphelines SI la base existe en _one/_other cote fr.
    Verifie : aucune cle fr/en/es/de ne se termine par _zero/_two/_few/_many en dehors
    des vrais compteurs (12 cles = 6 bases x _one/_other). Parite S9-3 inchangee apres
    modif (es=de=623/0/0).

- **S9-4a-lot1 (2026-06-14) : i18n arabe — common + auth + tabs + home + verification
  (136 cles, dont 57 existantes reecrites en UTF-8)**
  - **Correction encodage** : les 57 cles ar heritees etaient stockees en \uXXXX. Tout le
    fichier reecrit en vrais caracteres UTF-8 arabes (1796 caracteres arabes, 0 escape).
  - **Glossaire ar etabli** :
    - حساب=compte, تسجيل الدخول=connexion, كلمة المرور=mot de passe,
      البريد الإلكتروني=email, صالون=salon, مصفف شعر=coiffeur, عميل=client,
      رمز=code, حذف=supprimer, إلغاء=annuler, تأكيد=confirmer, بحث=rechercher,
      متابعة=suivre, فئة=catégorie, التحقق=vérification (action),
      توثيق/موثّق=vérification (badge).
  - **Interpolations** : {{email}} (verifyEmailHint), {{name}} (greeting, queueTitle),
    {{category}} (salonsCategory) — verifiees OK.
  - **Pas de pluriels CLDR** dans ce lot (les compteurs sont dans social/salon/review/
    profile/collections/security — lots suivants).
  - **Encodage** : aucun mojibake, aucun \uXXXX, caracteres latins uniquement pour les
    marques (Google Authenticator) et les variables.
  - **Localisation** : villes adaptees (Rabat, Dar el-Beida, Alger au lieu de Paris/Lyon).
  - **Relecture** : 0 critique, 2 notes mineures (states.following masculin singulier =
    convention app arabe standard ; greetingMessage مستعد masculin = defaut standard).
  - **Etat check-keys** : ar=136 cles, 487 manquantes, 0 orpheline. JSON valide.
    tsc=12 baseline inchangee.
  - **Reste S9-4a** : lots 3-6 ar. Puis S9-4b : RTL natif.

- **S9-4a-lot2 (2026-06-14) : i18n arabe — namespace social (77 cles + 4 formes CLDR)**
  - **Premier compteur CLDR arabe** : social.hashtagCount — 6 formes generees et validees :
    - _zero: {{count}} منشور (n=0, singulier)
    - _one: {{count}} منشور (n=1, singulier)
    - _two: {{count}} منشوران (n=2, duel)
    - _few: {{count}} منشورات (n=3-10, pluriel brise)
    - _many: {{count}} منشورًا (n=11-99, tamyiz accusatif — la forme la plus difficile)
    - _other: {{count}} منشور (n=100+, singulier)
  - **Glossaire ar social ajoute** : منشور=publication, تعليق=commentaire, إعجاب=like,
    المفضلة=favori, أرشفة=archiver, تثبيت=épingler, الرائج=tendance, الهاشتاغات=hashtags,
    المتابعون=abonnés, الموجز=fil/feed, وسائط=média.
  - **Interpolations** : {{count}} present dans les 6 formes CLDR. 0 mismatch.
  - **Encodage** : 0 escape, 0 mojibake. Diacritiques corrects (tanwin, shadda).
  - **Relecture** : 0 critique, 0 warning. Coherence lot1 confirmee.
  - **Etat check-keys** : ar=217 cles (136+77+4 CLDR), 410 manquantes, 0 orpheline.
    JSON valide. tsc=12 baseline inchangee.
  - **Reste S9-4a** : lots 4-6 ar. Puis S9-4b : RTL natif.

- **S9-4a-lot3 (2026-06-14) : i18n arabe — salon (30) + booking (69) = 99 cles + 4 CLDR**
  - **2e compteur CLDR** : salon.queueWaiting — 6 formes avec منتظر (waiting person) :
    - _zero: {{count}} منتظر (singulier)
    - _one: {{count}} منتظر (singulier)
    - _two: {{count}} منتظران (duel ـان)
    - _few: {{count}} منتظرين (pluriel masculin 3-10)
    - _many: {{count}} منتظرًا (tamyiz accusatif 11-99)
    - _other: {{count}} منتظر (singulier 100+)
  - **Glossaire ar metier ajoute** : موعد/مواعيد=rendez-vous(chose),
    حجز=reservation(action), خدمة=service, فترة=creneau, طابور=file d'attente
    (coherent avec home.liveQueue lot1), مصفف=coiffeur (coherent), تقييم=avis.
  - **Coherence terminologique** : distinction correcte موعد(appointment)/حجز(booking)
    miroir du FR rendez-vous/reservation. 0 doublon synonymique.
  - **Interpolations** : {{minutes}} x2, {{name}} x2, {{current}}+{{total}}, {{count}} x6
    — toutes verifiees OK.
  - **Encodage** : 0 escape, 0 mojibake. Diacritiques corrects.
  - **Relecture** : 0 critique, 0 bloquant. CLDR valide.
  - **Etat check-keys** : ar=320 cles, 311 manquantes, 0 orpheline. tsc=12.
  - **Reste S9-4a** : lots 4b-6 ar (250 cles + CLDR restants). Puis S9-4b : RTL.

- **S9-4a-lot4a (2026-06-14) : i18n arabe — namespace settings (61 cles)**
  - **Glossaire ar settings** : الإعدادات=parametres, المظهر=theme/apparence,
    اللغة=langue, الإشعارات=notifications, الوضع الداكن=mode sombre, فاتح=clair,
    داكن=sombre, النظام=systeme, الأمان=securite, الجلسة=session, الحذف=suppression,
    التحقق بخطوتين=2FA (aligne avec auth.twoFactor.title lot1).
  - **Correctif post-relecture** : subtitles.security المصادقة الثنائية → التحقق بخطوتين
    (coherence avec auth.twoFactor.title deja etabli au lot1).
  - **Interpolations** : {{version}} (versionLabel), {{email}} x2 (resendSuccess,
    pendingHint) — verifiees OK.
  - **Encodage** : 0 escape, 0 mojibake.
  - **Etat check-keys** : ar=381 cles, 250 manquantes, 0 orpheline. tsc=12.
  - **Reste S9-4a** : lots 5-6 ar. Puis S9-4b : RTL.

- **S9-4a-lot4b (2026-06-14) : i18n arabe — namespace security (73 cles + 4 CLDR)**
  - **ZONE SETTINGS+SECURITY ar COMPLETE** (lots 4a+4b = 134 cles + 4 CLDR).
  - **3e compteur CLDR** : revokeAllConfirmHint — 6 formes avec جلسة (FEMININ) :
    - _zero: {{count}} جلسة (singulier feminin)
    - _one: {{count}} جلسة (singulier)
    - _two: {{count}} جلستان (duel feminin ـتان)
    - _few: {{count}} جلسات (pluriel feminin sain 3-10)
    - _many: {{count}} جلسةً (tamyiz accusatif feminin 11-99)
    - _other: {{count}} جلسة (singulier 100+)
  - **Glossaire ar security** : رمز استرداد=code de recuperation, منطقة الخطر=zone de
    danger, فصل=deconnecter (boutons security), إلغاء=revoquer (settings-level).
  - **Coherence sous-titre lot 4a** verifiee : كلمة المرور, التحقق بخطوتين, الجلسات,
    الحذف — identiques.
  - **Avertissements** : severity/irreversibilite preservees fidellement (فوري ونهائي,
    لا يمكن التراجع, لا رجعة فيه, لا توجد أي طريقة لاستردادها).
  - **Interpolations** : {{count}} (minLengthRule + 6 CLDR), {{date}} x2 — OK.
  - **Encodage** : 0 escape, 0 mojibake.
  - **Etat check-keys** : ar=458 cles, 177 manquantes, 0 orpheline. tsc=12.
  - **Reste S9-4a** : lot 6 ar (dernier). Puis S9-4b : RTL.

- **S9-4a-lot5 (2026-06-14) : i18n arabe — profile (30) + collections (32) + report (25)
  + phone (24) = 111 cles + 8 CLDR (followersCount + postCount)**
  - **4e+5e compteurs CLDR** :
    - followersCount (متابع, masculin) : _zero/_one متابع, _two متابعان, _few متابعين,
      _many متابعًا, _other متابع.
    - postCount (منشور) : IDENTIQUE a hashtagCount lot2 (verifie programmatiquement).
  - **Glossaire ar ajoute** : الملف الشخصي=profil, المفضلة=favoris, الأرشيف=archives,
    المجموعات=collections, الشارات=badges, معارض الأعمال=portfolios, نبذة=bio,
    الإشراف=moderation, إبلاغ=signaler (coherent common.actions.report), بلاغ=signalement,
    عام=public, خاص=prive (coherent social.visibility), رقم الهاتف=telephone.
  - **Phone transactionalNote** : ton declaratif preserve ("يرى الصالون" = le salon VOIT,
    indicatif present, pas de modal). Coherent avec le correctif es/de du lot5.
  - **Interpolations** : {{year}}, {{name}} x2, {{query}}, {{name}}+{{code}}, {{iso}},
    {{count}} x12 (CLDR) — toutes verifiees OK.
  - **Encodage** : 0 escape, 0 mojibake.
  - **Etat check-keys** : ar=577 cles, 66 manquantes, 0 orpheline. tsc=12.
  - **Reste S9-4a** : lot 6 (dernier).

- **S9-4a-lot6 (2026-06-14) : i18n arabe — review (17) + payment (12) + service (10)
  + staff (10) + portfolio (9) + queue (8) = 66 cles + 4 CLDR — DERNIER LOT AR**
  - **6e et dernier compteur CLDR** : totalReviews (تقييم, masculin) :
    - _zero: {{count}} تقييم (singulier)
    - _one: {{count}} تقييم (singulier)
    - _two: {{count}} تقييمان (duel)
    - _few: {{count}} تقييمات (pluriel sain feminin — standard pour تقييم)
    - _many: {{count}} تقييمًا (tamyiz accusatif)
    - _other: {{count}} تقييم (singulier)
  - **Glossaire ar lot6** : تقييم=avis (coherent lot3), خدمة=service (coherent lot3),
    طابور=file d'attente (coherent), مصفف=coiffeur (coherent), دفع=paiement,
    المبلغ=montant, مدفوعات=paiements, عضو=membre, التخصصات=specialites,
    معرض أعمال=portfolio (coherent lot5).
  - **Coherence** : 0 doublon synonymique sur 6 lots.
  - **Interpolations** : {{rating}}, {{count}} x7 (minutes + 6 CLDR), {{name}} x2 — OK.
  - **Encodage** : 0 escape, 0 mojibake.
  - **Etat check-keys** : **ar=647 cles (623 + 24 CLDR), 0 manquante, 0 orpheline.
    OK (0 ecart). PARITE TOTALE 5 langues.**
    tsc=12 baseline inchangee.

- **=== S9-4a TERMINEE === arabe complet (623 cles de reference + 24 formes CLDR
  supplementaires = 647 cles). Parite TOTALE fr/en/es/de/ar, 5 langues a parite.
  6 compteurs CLDR en 6 formes chacun (hashtagCount, queueWaiting, revokeAllConfirmHint,
  followersCount, postCount, totalReviews). 3 genres declines : masculin (منشور, متابع,
  تقييم), feminin (جلسة), masculin participe actif (منتظر).
  Reste S9-4b : RTL natif (I18nManager.forceRTL + audit flexDirection des ecrans) —
  phase CODE, le dernier morceau de l'etape 9.**

- **S9-4b-diagnostic (2026-06-14) : audit RTL — lecture + test faisabilite + cartographie**

  **(A) ETAT DU SOCLE i18n/RTL :**
  1. src/i18n/index.ts : langue choisie par getLocales()[0].languageCode (expo-localization)
     au boot, AUCUN usage de I18nManager. L'arabe est declare en ressource mais aucune
     logique RTL n'existe.
  2. preferencesStore.ts : langue persistee en AsyncStorage (frollot_language), lue au
     demarrage, appliquee via i18next.changeLanguage(). AUCUN forceRTL nulle part dans le
     codebase (0 occurrence de I18nManager/forceRTL/allowRTL/isRTL).
  3. app/_layout.tsx : root layout initialise auth+prefs stores, charge les fonts, AUCUNE
     logique RTL. C'est ici (ou dans preferencesStore.initialize) que forceRTL doit aller.
  4. Expo SDK 56.0.9, React Native 0.85.3, lance via Expo Go (npx expo start).

  **(B) FAISABILITE forceRTL — CONCLUSION :**
  - I18nManager.forceRTL est disponible dans RN 0.85 / Expo SDK 56.
  - **Expo Go SUFFIT** pour voir le vrai rendu RTL (pas besoin de development build).
  - CONTRAINTE : forceRTL est un flag natif qui ne prend effet qu'au REDEMARRAGE de l'app
    (pas au hot reload). En Expo Go : fermer l'app completement + rouvrir = redemarrage.
  - IMPLEMENTATION REQUISE :
    * preferencesStore.initialize : appeler I18nManager.allowRTL(true) +
      I18nManager.forceRTL(lang === 'ar') au boot, AVANT le premier rendu.
    * preferencesStore.setLanguage : appeler forceRTL(lang === 'ar') + declencher un
      redemarrage (Updates.reloadAsync() ou demander a l'utilisateur de relancer).
    * NE PAS appeler forceRTL dans un useEffect (trop tard, l'arbre est deja rendu).
  - RESULTAT : **Expo Go suffit. Pas de build natif necessaire. Le test visuel est
    possible apres un redemarrage manuel de l'app dans Expo Go.**

  **(C) CARTOGRAPHIE DE L'AUDIT LAYOUT :**
  - **70 fichiers** au total : 50 ecrans (app/) + 20 composants (src/components/).
  - **Occurrences de patterns RTL-sensibles :**
    | Pattern | Occurrences | Fichiers |
    |---|---|---|
    | flexDirection: 'row' | 218 | 57 |
    | marginLeft/Right, paddingLeft/Right | 89 | 31 |
    | left:/right: (positions absolues) | 27 | 15 |
    | textAlign: 'left'/'right' | 2 | 2 |
    | Icones directionnelles (chevron/arrow) | 60 | 38 |
    | I18nManager.isRTL (existant) | 0 | 0 |
    | **TOTAL** | **~396** | **~65 uniques** |

  - **PROPOSITION DE DECOUPAGE EN VAGUES :**
    | Vague | Zone | Ecrans | Fichiers |
    |---|---|---|---|
    | V1 | Auth | login, register, forgot-pwd, reset-pwd, 2FA, email-verif | 6 |
    | V2 | Tabs | home, social, explore, bookings, profile | 5 |
    | V3 | Salon+Booking | salon/[id], salon/posts, booking/[id], booking/new, create-salon | 5 |
    | V4 | Social/Post | create-post, post/[id], comments, trending, report | 5 |
    | V5 | Settings | index, security, change-email, change-phone, contact, help, terms, privacy | 8 |
    | V6 | Profile/Collections | 4 profils + collections/[id] + collections/user + favorites + archives | 8 |
    | V7 | Misc | create-review/service/staff/portfolio, portfolios, portfolio/[id], owner-bookings, queue-mgmt | 8 |
    | V8 | Composants partages | PostCard, SalonCard, BookingStepper, PhoneNumberField, Button, TextField, Toast, Chip, etc. | 20 |
    | | **Total** | | **65** |

    NOTE : V8 (composants) devrait etre fait EN PREMIER car les corrections se propagent
    a tous les ecrans qui les utilisent, reduisant le travail des vagues 1-7. Apres V8,
    une passe de verification par vague est plus legere.

- **S9-4b-vague1 (2026-06-14) : RTL — activation forceRTL + composants partages**
  - **Activation forceRTL** :
    * preferencesStore.ts : applyRTL(lang) appele dans initialize() AVANT le 1er rendu
      (I18nManager.allowRTL + forceRTL).
    * setLanguage : detecte si un changement de direction est necessaire
      (lang==='ar' !== I18nManager.isRTL), appelle applyRTL, et affiche un Alert
      demandant de fermer/rouvrir l'app (expo-updates absent -> pas de reloadAsync).
    * Pour les 4 langues LTR : forceRTL(false) = zero regression.
  - **Composants traites (20 fichiers, 5 modifies)** :
    | Composant | Correction |
    |---|---|
    | PostCard.tsx | left→start (baLabel+swapIcon), paddingRight→paddingEnd (menuOverlay) |
    | SalonCard.tsx | right→end (favoriteBtn position) |
    | CountryPickerModal.tsx | marginLeft→marginStart (separator) |
    | PhoneNumberField.tsx | paddingLeft/Right→Start/End, borderRightWidth→borderEndWidth, borderRightColor→borderEndColor, marginRight→marginEnd |
    | Toast.tsx | left/right→start/end (position absolue symetrique) |
    | Avatar.tsx | INCHANGE (deja neutre) |
    | BookingStepper.tsx | INCHANGE (deja neutre) |
    | Button.tsx | INCHANGE (paddingHorizontal symetrique) |
    | Card.tsx | INCHANGE (padding symetrique) |
    | Chip.tsx | INCHANGE (paddingHorizontal symetrique) |
    | CollectionPickerModal.tsx | INCHANGE (textAlign center) |
    | CountryFlag.tsx | INCHANGE |
    | EmptyState.tsx | INCHANGE (centre) |
    | ErrorState.tsx | INCHANGE (centre) |
    | LoadingState.tsx | INCHANGE (centre) |
    | LogoutConfirmModal.tsx | INCHANGE (textAlign center) |
    | RatingStars.tsx | INCHANGE (etoiles symetriques) |
    | SectionHeader.tsx | INCHANGE (row auto-inverse) |
    | StatusBadge.tsx | INCHANGE (paddingHorizontal) |
    | TextField.tsx | INCHANGE (paddingHorizontal) |
  - **Aucun isRTL conditionnel** dans cette vague (pas d'icone directionnelle dans les
    composants partages — les chevrons/fleches sont dans les ecrans app/).
  - **tsc** : 12 baseline inchangee. **check-keys** : parite inchangee. **JSON** : intouche.
  - **Reste S9-4b** : vagues V1-V7 (50 ecrans app/). Les composants partages sont
    desormais RTL-safe, les corrections se propagent automatiquement.

- **S9-4b-vague-v1 (2026-06-14) : RTL — ecrans Auth (6 fichiers + _layout)**
  - **_layout.tsx** : INCHANGE (pure navigation Stack, pas de layout directionnel).
  - **login.tsx** : brandContainer left→start, heroPlaceholder left/right→start/end.
  - **register.tsx** : arrow-left isRTL conditionnel (back), arrow-right isRTL (submit),
    heroPlaceholder/heroActions left/right→start/end, heroBrand left→start,
    typeCheck right→end. Import I18nManager ajoute.
  - **forgot-password.tsx** : INCHANGE (bouton retour textuel, formulaire centre).
  - **reset-password.tsx** : INCHANGE (formulaire centre, paddingHorizontal symetrique).
  - **two-factor.tsx** : arrow-left isRTL conditionnel (back). Import I18nManager ajoute.
  - **email-verification.tsx** : arrow-left isRTL conditionnel (retour login).
    Import I18nManager ajoute.
  - **Icones isRTL introduites** : 4 icones dans 3 fichiers (register back + submit,
    two-factor back, email-verification back).
  - **Positions logiques** : 5 corrections left/right→start/end (login + register).
  - **tsc** : 12 baseline inchangee. **check-keys** : parite intacte. **JSON** : intouche.
  - **Reste S9-4b** : vagues V3-V7 (39 ecrans app/).

- **S9-4b-vague-v2 (2026-06-14) : RTL — ecrans Tabs (5 + _layout)**
  - **_layout.tsx** : INCHANGE (Tabs navigation, layout symetrique).
  - **index.tsx (home)** : chevron-right isRTL conditionnel (salon list items).
    Import I18nManager ajoute.
  - **social.tsx** : searchSpinner right→end, tabIndicator left/right→start/end.
  - **explore.tsx** : marginLeft→marginStart (verified badge + salonInfo).
  - **bookings.tsx** : marginLeft→marginStart x2 (date meta + duration).
  - **profile.tsx** : chevron-right isRTL conditionnel (menu items), marginLeft→
    marginStart x3 (verified, menuItem text, logout), editBadge right→end.
    Import I18nManager ajoute.
  - **Icones isRTL** : 2 (index.tsx chevron salon list, profile.tsx chevron menu).
  - **Proprietes logiques** : 7 marginLeft→marginStart + 3 position right/left→end/start.
  - **tsc** : 12 baseline inchangee. **check-keys** : parite intacte. **JSON** : intouche.
  - **Chaines FR en dur reperees (pas corrigees, passe finale)** : aucune nouvelle dans
    ces 5 ecrans (email-verification.tsx deja note en vague v1).
  - **Reste S9-4b** : vagues V4-V7 (34 ecrans app/).

- **S9-4b-vague-v3 (2026-06-14) : RTL — ecrans Salon+Booking (5)**
  - **salon/[id].tsx** : arrow-left + arrow-right isRTL (back + see-all),
    coverActions/coverEditBadge/tabIndicator/floatingBar left/right→start/end,
    coverBtn marginLeft→marginStart.
  - **salon/[id]/posts.tsx** : arrow-left isRTL (back), coverName left/right→start/end,
    fab right→end.
  - **booking/[id].tsx** : arrow-back→arrow-forward isRTL, chevron-right isRTL,
    6x marginLeft→marginStart (title, salon, service, date, staff, price).
  - **booking/new.tsx** : arrow-left isRTL (back), 2x chevron-right isRTL (any-staff +
    staff-list), chevron-left/right isRTL (week nav prev/next).
  - **create-salon.tsx** : arrow-left isRTL (back).
  - **STEPPER (BookingStepper)** : VERIFIE — flexDirection:'row' s'inverse
    automatiquement (step 1 a droite en RTL), connecteurs et check-icon non-
    directionnels. **RTL-safe sans modification.** Pas de fleche suivant/precedent
    dans le stepper lui-meme (les boutons continuer/retour sont dans booking/new).
  - **Icones isRTL** : 10 dans 5 fichiers (salon back+see-all, salon/posts back,
    booking back+chevron, booking/new back+2xchevron+2xweek-nav, create-salon back).
  - **Positions logiques** : 10 corrections left/right→start/end + 7 marginLeft→marginStart.
  - **Chaines FR en dur (SIGNALEES, pas corrigees)** :
    - booking/new.tsx:15 MONTHS=['Janvier',...] + DAYS_SHORT=['DIM',...]
    - create-salon.tsx:62,74,75,88,89,101,102,110,114,118,122,128,131,135,139,146,150,173
      (~18 chaines FR en dur).
  - **tsc** : 12 baseline inchangee. **check-keys** : parite intacte. **JSON** : intouche.
  - **Reste S9-4b** : vagues V4-V7 (34 ecrans app/).

- **S9-4b-vague-v4 (2026-06-14) : RTL — ecrans Social/Post (5)**
  - **create-post.tsx** : 5x marginLeft→marginStart, 2x marginRight→marginEnd,
    paddingRight→paddingEnd, 2x position right/left→end/start. Pas d'icone directionnelle
    (utilise "close" symetrique).
  - **post/[id].tsx** : arrow-back isRTL (MaterialIcons), 4x marginLeft→marginStart,
    paddingRight→paddingEnd.
  - **comments/[id].tsx** : arrow-back isRTL (MaterialIcons), 2x marginLeft→marginStart.
  - **trending.tsx** : arrow-back isRTL (MaterialIcons), 6x marginLeft→marginStart.
  - **report.tsx** : arrow-back isRTL (MaterialIcons), marginLeft→marginStart,
    textAlign:'right' → isRTL conditionnel (compteur caracteres, bord de fuite).
  - **Icones isRTL** : 4 (post back, comments back, trending back, report back) —
    toutes MaterialIcons arrow-back↔arrow-forward.
  - **Proprietes logiques** : 17 marginLeft→marginStart + 2 marginRight→marginEnd +
    2 paddingRight→paddingEnd + 2 positions left/right→start/end + 1 textAlign isRTL.
  - **Chaines FR en dur** : aucune nouvelle reperee dans ces 5 ecrans.
  - **tsc** : 12 baseline inchangee. **check-keys** : parite intacte. **JSON** : intouche.
  - **Reste S9-4b** : vagues V5-V7 (29 ecrans app/).

- **S9-4b-vague-v5 (2026-06-14) : RTL — ecrans Settings applicatifs (4)**
  - Perimetre : index.tsx, security.tsx, change-email.tsx, change-phone.tsx
  - EXCLUS (decision validee) : terms.tsx, privacy.tsx, help.tsx, contact.tsx (ecrans legaux FR-only)
  - Corrections appliquees :
    - index.tsx : I18nManager import, 2 chevrons isRTL, 1 fleche retour isRTL, marginLeft->marginStart (logout btn, sectionTitle, separator)
    - security.tsx : I18nManager import, 1 fleche retour isRTL. Pas de marginLeft/Right (gap partout). secretText textAlign:'left' = intentionnel (code TOTP, non directionnel)
    - change-email.tsx : I18nManager import, 1 fleche retour isRTL. Pas de marge directionnelle
    - change-phone.tsx : I18nManager import, 1 fleche retour isRTL, marginLeft->marginStart (delete btn)
  - Jeu d'icones : MaterialIcons partout (arrow-back/forward, chevron-left/right)
  - Chaines FR en dur croisees (SIGNALEES, PAS corrigees) : security.tsx:58 formatDate locale 'fr-FR' + ' a '
  - tsc = 12 (baseline), check-keys = 0 ecart, ecrans legaux INTACTS
  - **Reste S9-4b** : vagues V6-V7 (25 ecrans app/).

- **S9-4b-vague-v6 (2026-06-14) : RTL — ecrans Profil/Collections (8)**
  - Perimetre : profile/client/[id], profile/coiffeur/[id], profile/owner/[id], profile/salon/[id],
    collections/[id], collections/user/[userId], favorites/[userId], archives/[userId]
  - Corrections appliquees :
    - profile/client/[id].tsx : fleche retour isRTL, marginLeft->marginStart (header title, collection row), chevron isRTL
    - profile/coiffeur/[id].tsx : fleche retour isRTL, marginLeft->marginStart (header title, verified badge, rating, post meta x3)
    - profile/owner/[id].tsx : fleche retour isRTL, marginLeft->marginStart (header title, verified badge, salon row, verified icon, collection name), 2 chevrons isRTL
    - profile/salon/[id].tsx : fleche retour isRTL, marginLeft->marginStart (header title, city, verified badge, rating)
    - collections/[id].tsx : 2 fleches retour isRTL (error + main), chevron isRTL
    - collections/user/[userId].tsx : fleche retour isRTL, FAB right->end
    - favorites/[userId].tsx : fleche retour isRTL, marginLeft->marginStart (header title, likes count, thumb)
    - archives/[userId].tsx : fleche retour isRTL (MaterialCommunityIcons arrow-left/right)
  - Jeux d'icones : MaterialIcons (arrow-back/forward, chevron-left/right) pour 7 fichiers ; MaterialCommunityIcons (arrow-left/right) pour archives
  - Chaines FR en dur SIGNALEES (PAS corrigees) : archives/[userId].tsx:80-81 (messages erreur desarchiver/supprimer)
  - tsc = 12 (baseline), check-keys = 0 ecart, aucun composant partage touche
  - **Reste S9-4b** : vague V7 misc (~8 ecrans app/).

- **S9-4b-vague-v7 (2026-06-14) : RTL — ecrans Misc (8 traites + 1 inchange) = DERNIERE VAGUE**
  - Perimetre : create-review, create-service, create-staff, create-portfolio, portfolios,
    portfolio/[id], owner-bookings, queue-management, verify-email (inchange)
  - Corrections appliquees :
    - create-review.tsx : fleche retour isRTL, marginLeft->marginStart (header title)
    - create-service.tsx : fleche retour isRTL, marginLeft->marginStart (header title), right->end (checkIcon)
    - create-staff.tsx : fleche retour isRTL, marginLeft->marginStart (header title, info box text), right->end (checkIcon)
    - create-portfolio.tsx : fleche retour isRTL, marginLeft->marginStart (header title, radio label)
    - portfolios.tsx : fleche retour isRTL, marginLeft->marginStart (header title), FAB right->end
    - portfolio/[id].tsx : fleche retour isRTL, marginLeft->marginStart (header title, post meta x3)
    - owner-bookings.tsx : fleche retour isRTL, marginLeft->marginStart (header title, meta rows x2)
    - queue-management.tsx : fleche retour isRTL, marginLeft->marginStart (header title, entry info, entry meta)
    - verify-email.tsx : INCHANGE (layout centre, pas d'icone directionnelle ni de marge laterale)
  - Jeu d'icones : MaterialIcons (arrow-back/forward) pour tous les 8 fichiers traites
  - Chaines FR en dur SIGNALEES (PAS corrigees) :
    - verify-email.tsx:16 'Lien invalide : aucun token fourni.'
    - verify-email.tsx:25 'Ce lien est invalide ou a expire.'
    - verify-email.tsx:37 'Activation de votre compte...'
    - verify-email.tsx:48 'Verification echouee'
    - verify-email.tsx:51 'Retour a la connexion'
    - verify-email.tsx:62 'Compte active !'
    - verify-email.tsx:63-64 'Votre adresse email...'
    - verify-email.tsx:67 'Se connecter'
    - portfolios.tsx:44 'posts' (mot EN en dur)
    - portfolio/[id].tsx:63 'posts' (mot EN en dur)
  - tsc = 12 (baseline), check-keys = 0 ecart, aucun composant partage touche

- **S9-4b TERMINEE — RTL natif applique a tout app/ (hors 4 legaux exclus) + 20 composants + activation forceRTL**
  - BALAYAGE DE CLOTURE : 49 fichiers .tsx dans app/. Tous couverts :
    - 35 fichiers ont I18nManager (corrections directionnelles appliquees vagues 1-7)
    - 10 fichiers sans I18nManager car AUCUNE propriete directionnelle a corriger :
      app/_layout.tsx, app/index.tsx, (auth)/_layout, (auth)/login, (auth)/forgot-password,
      (auth)/reset-password, (tabs)/_layout, (tabs)/bookings, (tabs)/explore, (tabs)/social
    - 4 ecrans EXCLUS (legaux FR-only, decision validee) : settings/{terms,privacy,help,contact}
  - Reste : passe de diagnostic/nettoyage des chaines FR oubliees (inventaire constitue ci-dessous)
  - INVENTAIRE CONSOLIDE des chaines FR en dur a traiter :
    - verify-email.tsx (~8 chaines)
    - archives/[userId].tsx:80-81 (2 messages erreur)
    - security.tsx:58 (formatDate 'fr-FR' + ' a ' = dette dates)
    - booking/new.tsx (MONTHS/DAYS_SHORT = dette dates)
    - create-salon.tsx (~18 chaines)
    - email-verification.tsx (quelques chaines)
    - portfolios.tsx:44, portfolio/[id].tsx:63 ('posts' EN en dur)

- **S9-5-diagnostic (2026-06-14) : inventaire des chaines non externalisees**
  - Methode : grep statique sur tout app/ + src/ (hors 4 legaux exclus, hors i18n/*.json)
  - Baselines inchangees : tsc = 12, check-keys = 0 ecart. AUCUN fichier modifie.

  TABLEAU DES TROUVAILLES :

  | # | Fichier:ligne | Litteral (tronque) | Contexte | Cat. |
  |---|---|---|---|---|
  | 1 | app/verify-email.tsx:16 | 'Lien invalide : aucun token fourni.' | errorMessage fallback | A |
  | 2 | app/verify-email.tsx:25 | 'Ce lien est invalide ou a expire.' | errorMessage fallback | A |
  | 3 | app/verify-email.tsx:37 | 'Activation de votre compte...' | Text enfant (loading) | A |
  | 4 | app/verify-email.tsx:48 | 'Verification echouee' | Text enfant (title) | A |
  | 5 | app/verify-email.tsx:51 | 'Retour a la connexion' | bouton Text enfant | A |
  | 6 | app/verify-email.tsx:62 | 'Compte active !' | Text enfant (title) | A |
  | 7 | app/verify-email.tsx:63-64 | 'Votre adresse email...' | Text enfant (desc) | A |
  | 8 | app/verify-email.tsx:67 | 'Se connecter' | bouton Text enfant | A |
  | 9 | app/(auth)/email-verification.tsx:19 | 'Verifiez votre email' | Text enfant (title) | A |
  | 10 | app/(auth)/email-verification.tsx:23-25 | 'Un email de verification...' + 'votre adresse' fallback | Text enfant (desc) | A |
  | 11 | app/(auth)/email-verification.tsx:31 | 'Si vous n avez pas recu...' | Text enfant (info) | A |
  | 12 | app/(auth)/email-verification.tsx:40 | 'Retour a la connexion' | bouton Text enfant | A |
  | 13 | app/create-salon.tsx:62 | 'Erreur lors de la creation du salon.' | error fallback | A |
  | 14 | app/create-salon.tsx:74 | 'Salon cree !' | Text enfant (success title) | A |
  | 15 | app/create-salon.tsx:75 | 'Votre salon a ete cree...' | Text enfant (success desc) | A |
  | 16 | app/create-salon.tsx:88 | 'Nouveau salon' | Text enfant (header title) | A |
  | 17 | app/create-salon.tsx:89 | 'Configurez votre etablissement' | Text enfant (header sub) | A |
  | 18 | app/create-salon.tsx:101 | 'Photo de couverture' | Text enfant (cover label) | A |
  | 19 | app/create-salon.tsx:102 | 'Recommande : 1200 x 400 px' | Text enfant (cover hint) | A |
  | 20 | app/create-salon.tsx:110 | label="Nom du salon *" | TextField label prop | A |
  | 21 | app/create-salon.tsx:114 | placeholder="Ex : Salon Lumiere" | TextField placeholder | A |
  | 22 | app/create-salon.tsx:118 | label="Adresse *" | TextField label prop | A |
  | 23 | app/create-salon.tsx:122 | placeholder="12 rue de la Paix" | TextField placeholder | A |
  | 24 | app/create-salon.tsx:128 | label="Ville *" | TextField label prop | A |
  | 25 | app/create-salon.tsx:131 | placeholder="Paris" | TextField placeholder | A |
  | 26 | app/create-salon.tsx:136 | label="Code postal *" | TextField label prop | A |
  | 27 | app/create-salon.tsx:139 | placeholder="75002" | TextField placeholder | A |
  | 28 | app/create-salon.tsx:146 | label="Description" | TextField label prop | A |
  | 29 | app/create-salon.tsx:150 | placeholder="Decrivez votre salon..." | TextField placeholder | A |
  | 30 | app/create-salon.tsx:173 | 'Creer mon salon' | PrimaryButton Text enfant | A |
  | 31 | app/archives/[userId].tsx:80 | 'Impossible de desarchiver...' | actionError fallback | A |
  | 32 | app/archives/[userId].tsx:81 | 'Impossible de supprimer...' | actionError fallback | A |
  | 33 | app/portfolios.tsx:44 | '{item.postsCount} posts' | Text enfant (EN en dur) | A |
  | 34 | app/portfolio/[id].tsx:63 | '{portfolio.postsCount} posts' | Text enfant (EN en dur) | A |
  | 35 | app/queue-management.tsx:190 | '{item.status}' | Text enfant (enum brut WAITING/CALLED/...) | A |
  | 36 | app/queue-management.tsx:197 | '~{...} min' | Text enfant (unite en dur) | A |
  | 37 | app/create-portfolio.tsx:58 | text: 'OK' | Alert bouton label | A |
  | 38 | app/create-review.tsx:57 | text: 'OK' | Alert bouton label | A |
  | 39 | app/create-service.tsx:52 | text: 'OK' | Alert bouton label | A |
  | 40 | app/create-staff.tsx:80 | text: 'OK' | Alert bouton label | A |
  | 41 | app/(auth)/register.tsx:115 | placeholder="Camille" | TextField placeholder (prenom FR) | A |
  | 42 | app/(auth)/register.tsx:118 | placeholder="Roussel" | TextField placeholder (nom FR) | A |
  | 43 | app/(auth)/register.tsx:127 | placeholder="camille@email.com" | placeholder (email exemple FR) | A |
  | 44 | src/stores/preferencesStore.ts:45 | 'Redemarrage requis' (+ AR) | Alert.alert titre | A |
  | 45 | src/stores/preferencesStore.ts:47-48 | 'Fermez et rouvrez...' (+ AR) | Alert.alert message | A |
  | 46 | src/types/salon.ts:47-53 | SERVICE_CATEGORY_META labels x7 | label affiche (FR en dur) | A |
  |---|---|---|---|---|
  | 47 | app/booking/new.tsx:15 | MONTHS=['Janvier',...,'Decembre'] | tableau 12 mois FR | B |
  | 48 | app/booking/new.tsx:16 | DAYS_SHORT=['DIM','LUN',...] | tableau 7 jours FR | B |
  | 49 | app/booking/new.tsx:124,174,346,357,409,454,469 | usages MONTHS/DAYS_SHORT dans texte | concatenations date affichees (x7) | B |
  | 50 | app/(tabs)/index.tsx:69 | toLocaleDateString('fr-FR',...) | date accueil forcee FR | B |
  | 51 | app/(tabs)/bookings.tsx:106 | toLocaleDateString() + toLocaleTimeString() | date sans locale explicite | B |
  | 52 | app/booking/[id].tsx:159 | toLocaleDateString() - toLocaleTimeString() | date + separateur ' - ' | B |
  | 53 | app/owner-bookings.tsx:180 | toLocaleDateString() + toLocaleTimeString() | date sans locale explicite | B |
  | 54 | app/settings/security.tsx:58-60 | formatDate('fr-FR') + ' a ' | date session forcee FR | B |

  NOTE SUPPLEMENTAIRE (hors perimetre i18n classique) :
  - src/utils/countries.ts : 245 noms de pays en francais (telephone picker). Delibere a la
    conception mais techniquement non traduit. Chantier majeur (245x5 langues) a decider separement.

  DECOMPTE :
  - Total trouvailles : 54
  - Categorie A (cles simples) : 46
  - Categorie B (dette dates/formatage) : 8 (couvrant ~15 usages dans le code)
  - Fichiers concernes : 16 (dont 1 dans src/types, 1 dans src/stores)

  PROPOSITION DE DECOUPAGE :
  - LOT A1 — verify-email.tsx + email-verification.tsx : 12 cles, 2 fichiers. Ecrans autonomes,
    zero dependance. ~8 cles FR a creer + traduire 5 langues.
  - LOT A2 — create-salon.tsx : 18 cles, 1 fichier. Le plus gros ecran. Tout est FR en dur.
  - LOT A3 — archives + portfolios + portfolio + queue-management : 6 cles, 4 fichiers. Petits
    correctifs (messages erreur, 'posts', enum status, unite 'min').
  - LOT A4 — Alerts 'OK' x4 + register placeholders x3 + preferencesStore Alert x2 : 9 cles,
    6 fichiers. Eparpilles mais simples.
  - LOT A5 — SERVICE_CATEGORY_META (src/types/salon.ts) : 7 labels de categorie. Necessite un
    refactor : les labels statiques doivent devenir des cles i18n, les composants consommateurs
    (create-service, create-staff) doivent appeler t() au rendu.
  - LOT B — Dette dates/formatage locale-aware : 8 trouvailles dans 5 fichiers. Chantier separe
    necessitant une approche coherente (ex. helper formatDate locale-aware base sur i18n.language,
    remplacement des tableaux MONTHS/DAYS par Intl.DateTimeFormat ou cles i18n). A cadrer avant
    d'agir.

  IMPACT PARITE : chaque cle categorie A ajoutee au fr.json devra etre traduite dans en/es/de/ar
  pour que check-keys reste a 0 ecart. Estimation : ~46 cles nouvelles x 5 langues = ~230 entrees
  a creer dans les JSON.

---

- **S9-5-lotA2 (2026-06-14) : externaliser create-salon.tsx — 13 cles creees**
  - N = 13 cles nouvelles sous salon.create.*. Parite : fr/en/es/de = 636, ar = 660, 0 ecart.
  - 5 cles REUTILISEES (pas de creation) : salon.salonName, salon.address, salon.city,
    salon.postalCode, salon.description — deja presentes dans fr.json (et toutes langues).
  - Cles creees (valeur FR) :
    salon.create.title = "Nouveau salon"
    salon.create.subtitle = "Configurez votre etablissement"
    salon.create.coverLabel = "Photo de couverture"
    salon.create.coverHint = "Recommande : 1200 x 400 px"
    salon.create.namePlaceholder = "Ex : Salon Lumiere"
    salon.create.addressPlaceholder = "12 rue de la Paix"
    salon.create.cityPlaceholder = "Paris"
    salon.create.postalCodePlaceholder = "75002"
    salon.create.descriptionPlaceholder = "Decrivez votre salon, vos specialites..."
    salon.create.submit = "Creer mon salon"
    salon.create.successTitle = "Salon cree !"
    salon.create.successDesc = "Votre salon a ete cree avec succes. Redirection..."
    salon.create.error = "Erreur lors de la creation du salon."
  - Choix placeholders par langue (exemples localement plausibles) :
    EN: "Bella Hair Studio", "123 Main Street", "London", "SW1A 1AA"
    ES: "Salon Belleza", "Calle Mayor, 12", "Madrid", "28001"
    DE: "Salon Schonheit", "Hauptstrasse 12", "Berlin", "10115"
    AR: "صالون الجمال", "شارع الرئيسي 12", "الدار البيضاء", "20000"
  - tsc = 12, check-keys = 0 ecart, grep controle = 0 litteral FR dans create-salon.tsx
  - **Reste S9-5** : lots A1, A3, A4, A5, B.

- **S9-5-lotA1 (2026-06-14) : externaliser verify-email + email-verification — 9 cles creees**
  - N = 9 cles nouvelles sous auth.verifyEmail.*. Parite : fr/en/es/de = 645, ar = 669, 0 ecart.
  - Cles REUTILISEES (pas de creation) :
    - auth.twoFactor.backToLogin ("Retour a la connexion") -> les 2 ecrans
    - auth.verifyEmailTitle ("Verifiez votre email") -> email-verification.tsx
    - auth.verifyEmailHint (avec {{email}}) -> email-verification.tsx
  - Gestion fallback email : t('auth.verifyEmailHint', { email: email || t('auth.verifyEmail.yourAddress') })
    Le fallback "votre adresse" est une cle dediee passee comme valeur d'interpolation.
  - Cles creees (valeur FR) :
    auth.verifyEmail.invalidLink = "Lien invalide : aucun token fourni."
    auth.verifyEmail.expiredLink = "Ce lien est invalide ou a expire."
    auth.verifyEmail.activating = "Activation de votre compte..."
    auth.verifyEmail.failedTitle = "Verification echouee"
    auth.verifyEmail.successTitle = "Compte active !"
    auth.verifyEmail.successDesc = "Votre adresse email... (avec \n)"
    auth.verifyEmail.loginButton = "Se connecter"
    auth.verifyEmail.yourAddress = "votre adresse"
    auth.verifyEmail.spamHint = "Si vous n avez pas recu l email..."
  - tsc = 12, check-keys = 0 ecart, grep controle = 0 litteral FR dans les 2 ecrans
  - **Reste S9-5** : lots A3, A4, A5, B.

- **S9-5-lotA3 (2026-06-14) : archives/portfolios/queue — 5 cles creees**
  - N = 5 cles nouvelles. Parite : fr/en/es/de = 650, ar = 674, 0 ecart.
  - Cles REUTILISEES (pas de creation) :
    - collections.postCount (avec formes CLDR arabes) -> portfolios.tsx + portfolio/[id].tsx
    - queue.waiting, queue.called -> STATUS_LABELS map dans queue-management.tsx
  - Cles creees :
    social.unarchiveError = "Impossible de desarchiver ce post. Reessayez."
    social.deletePostError = "Impossible de supprimer ce post. Reessayez."
    queue.cancelled = "Annule"
    queue.completed = "Termine"
    queue.estimatedWait = "~{{count}} min"
  - Enum status map EXPLICITE (queue-management.tsx) :
    STATUS_LABELS: Record<QueueEntryStatus, string> = {
      WAITING -> t('queue.waiting'), CALLED -> t('queue.called'),
      CANCELLED -> t('queue.cancelled'), COMPLETED -> t('queue.completed')
    } avec fallback item.status si valeur inattendue.
  - Unite 'min' : cle queue.estimatedWait encapsule le format complet "~{{count}} min" (abreviation
    invariable, pas de pluriel necessaire ; DE utilise "Min." par convention).
  - tsc = 12, check-keys = 0 ecart, grep = 0 litteral FR/EN en dur dans les 4 ecrans
  - **Reste S9-5** : lots A4, A5, B.

- **S9-5-lotA4 (2026-06-14) : Alerts OK + register placeholders + preferencesStore — 6 cles creees**
  - N = 6 cles nouvelles. Parite : fr/en/es/de = 656, ar = 680, 0 ecart.
  - Cles creees :
    common.actions.ok = "OK" (FR/EN/ES/DE: "OK", AR: "حسنًا")
    common.states.restartRequired = "Redemarrage requis"
    common.states.restartHint = "Fermez et rouvrez l application..."
    auth.register.firstNamePlaceholder = "Camille" (EN: Jane, ES: Maria, DE: Anna, AR: فاطمة)
    auth.register.lastNamePlaceholder = "Dupont" (EN: Smith, ES: Garcia, DE: Muller, AR: أحمد)
    auth.register.emailPlaceholder = "camille@email.com" (EN: jane@, ES: maria@, DE: anna@, AR: fatima@)
  - Fichiers modifies : 4 create-* (Alert OK), register.tsx (3 placeholders),
    src/stores/preferencesStore.ts (Alert FR/AR -> i18next.t())
  - preferencesStore : i18next.t() utilise directement (pas de hook, store Zustand ; i18next deja
    importe et changeLanguage() appele AVANT l Alert -> la bonne langue est active)
  - tsc = 12, check-keys = 0 ecart, grep = 0 litteral en dur
  - **Reste S9-5** : lots A5, B.

- **S9-5-lotA5 (2026-06-14) : refactor SERVICE_CATEGORY_META — 7 cles creees**
  - N = 7 cles nouvelles sous service.categories.*. Parite : fr/en/es/de = 663, ar = 687, 0 ecart.
  - Refactor : SERVICE_CATEGORY_META.label (string FR) -> .labelKey (cle i18n). Les 2 ecrans
    consommateurs (create-service, create-staff) appellent t(cat.labelKey) au rendu.
  - Cles creees :
    service.categories.coupe = "Coupe & Taille" (EN: Cut & Trim, ES: Corte y Recorte, DE: Schnitt & Trimm, AR: قص وتشذيب)
    service.categories.coloration = "Coloration" (EN: Colouring, ES: Coloracion, DE: Farbung, AR: صبغة)
    service.categories.soin = "Soins" (EN: Hair Care, ES: Cuidados, DE: Haarpflege, AR: عناية بالشعر)
    service.categories.coiffage = "Coiffage" (EN: Styling, ES: Peinado, DE: Styling, AR: تصفيف)
    service.categories.barbe = "Barbier" (EN: Barber, ES: Barberia, DE: Barbier, AR: حلاقة)
    service.categories.technique = "Techniques Speciales" (EN: Special Techniques, ES: Tecnicas Especiales, DE: Spezialtechniken, AR: تقنيات خاصة)
    service.categories.autre = "Autres Prestations" (EN: Other Services, ES: Otros Servicios, DE: Sonstige Leistungen, AR: خدمات أخرى)
  - Fichiers modifies : src/types/salon.ts, app/create-service.tsx, app/create-staff.tsx, 5 JSON
  - tsc = 12, check-keys = 0 ecart, grep = 0 label FR en dur
  - **S9-5 CATEGORIE A TERMINEE** — 46 trouvailles traitees en 5 lots (A1-A5), 40 cles creees +
    nombreuses reutilisations. Parite finale cat. A : fr/en/es/de = 663, ar = 687.
  - **Reste S9-5** : lot B (dette dates/formatage locale-aware, 8 trouvailles, chantier separe).

- **S9-5-lotB-diagnostic (2026-06-14) : test Intl + recensement formatage dates**
  - **Test Intl** : Node ICU 77.1 (equivalent Android API 36 ICU 76) confirme Intl.DateTimeFormat OK
    pour les 5 locales (fr/en/es/de/ar) y compris dateStyle/timeStyle. Arabe rend correctement les
    noms de mois arabes. Hermes RN 0.85 delegue a l'ICU plateforme (Android/iOS), aucun polyfill
    requis. Build natif non concluant (CMake manquant puis file lock) — diagnostic documente.
  - **CONCLUSION** : approche Intl.DateTimeFormat(i18n.language) VALIDEE. Pas besoin de polyfill
    (@formatjs/intl-datetimeformat) ni de cles manuelles common.months.*.
  - **Recensement** : 6 fichiers, 8 points de formatage (detail dans trace ci-dessous).
  - **Helper propose** : src/utils/formatDate.ts (4 fonctions, base sur i18n.language).
  - tsc = 12, check-keys = 0 ecart, git propre.
  - **EN ATTENTE** validation humaine avant implementation.

- **S9-5-lotB (2026-06-14) : helper formatDate Intl + migration 6 ecrans**
  - CREE src/utils/formatDate.ts : 6 fonctions (formatDateLong, formatDateTime, formatDateTimeShort,
    formatMonthYear, formatDayShort, formatMonthName) + garde-fou runtime intlArabicOK (probe arabe).
  - MIGRE 6 fichiers : (tabs)/index.tsx, booking/new.tsx (MONTHS+DAYS_SHORT supprimes), (tabs)/
    bookings.tsx, booking/[id].tsx, owner-bookings.tsx, settings/security.tsx.
  - Zero cle i18n creee (check-keys inchange 663/687/0). tsc = 12 inchange.
  - Grep controle : aucun 'fr-FR', MONTHS, DAYS_SHORT, toLocaleDateString, toLocaleTimeString, ni
    separateur date FR dans les 6 fichiers.
  - **S9-5 TERMINEE** (sous reserve test de rendu ci-dessous).

- **ETAT DES LIEUX POST-ETAPE 9 (2026-06-15)** : cartographie pure, aucun code modifie.
  - Etapes 1-9 (incl. 6B) FAITES. Etape 8 famille C : 2/4 faits (security, change-phone),
    reste S10 (verification, frontend seul) et S11 (blocage, full-stack).
  - Etapes 10-15 PAS COMMENCEES.
  - tsc = 12 erreurs baseline inchangees (register fieldLabel, index averageRating/reviewCount,
    profile statistics x9).
  - check-keys = fr/en/es/de 663, ar 687, 0 ecart.
  - Dettes documentees : templates email FR only, countries.ts 245 noms FR, 3 .includes()
    sur messages backend FR, salon_staff.role ENUM, repartage-profil, resendVerification,
    overlays dark mode, MediaController 10.0.2.2, pin HTTP 401.
  - Candidats prochain chantier proposes : (1) Etape 10 navigation avancee, (2) S10+S11
    ecrans manquants restants, (3) correction 12 erreurs tsc baseline.
  - Decision en attente validation humaine.

- **Reprise post-crash + commit securisation (2026-06-15)** :
  - PC eteint brutalement. Aucune correction en cours au moment du crash.
  - Reconnaissance : backend BUILD SUCCESSFUL, tsc=0, check-keys 668/692 0 ecart.
    Aucun fichier tronque. Revert Avatar social.tsx confirme. Home inchange.
  - 5 cles salon.roles.* + salon.noTeam ajoutees (663->668, 687->692).
  - Notes salon/coiffeur (ReviewRepository, SocialDto/Service, types/profile.ts),
    equipe complete (SalonStaffService owner injection, StaffResponse.role, SalonStaff.kt),
    avatars alignes — tout valide par l'humain avant le crash.
  - .claude/ ajoute au .gitignore racine.
  - COMMIT LOCAL 2f90607 : 122 fichiers, tout le travail etapes 1-9 securise.
    Rien n'a ete pushe (decision humaine).

- **Migration port 8090 + commit securisation (2026-06-15)** :
  - Windows Hyper-V reserve dynamiquement la plage 9047-9146, rendant le port 9090
    inutilisable. **PORT BACKEND DESORMAIS = 8090** (reference pour tous les futurs
    relances, tests et configurations).
  - Migration appliquee sur 19 fichiers : application.yml (server.port, CORS, base-url),
    13 controllers (@CrossOrigin), SecurityConfig, 2 services (devUrl),
    frontend client.ts + media.ts. Sed verifie propre (22 remplacements legitimes).
  - Verification post-migration : BUILD SUCCESSFUL (recompile), tsc=0,
    check-keys 668/692 0 ecart, 0 residu :9090 dans tout le codebase.
  - COMMIT LOCAL e66044f. Rien pushe.

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

---

## PHASE 1 — DIAGNOSTIC ECRANS MANQUANTS (2026-06-15)

Diagnostic exhaustif pur lecture : inventaire backend (18 controllers, ~150 endpoints) croise avec
les 50 ecrans RN existants. Boussole = backend + liens morts RN (KMP n'est plus la reference).

### Resultat

8 lots identifies (A-H), ordonnes par dependances et ratio valeur/risque :

| Lot | Sujet | Taille | Backend | Bloque par |
|-----|-------|--------|---------|------------|
| A | Navigation profils (cablage onProfilePress, helper dispatch userType) | S | 0% | - |
| D | Menu lateral / Drawer (hamburgers morts Home+Social) | M | 0% | - |
| B | Listes followers/following (ecrans + compteurs cliquables) | M | 0% | A |
| F | Interactions salon (coeur + share cover, like=follow?) | S-M | 0-30% | - |
| C | Publication au nom du salon (selecteur identite, salonId payload) | M | 20% | A |
| E | Complements avis (noms cliquables, photos optionnel) | S | 10% | A |
| G | Blocage utilisateurs (full-stack: table + endpoints + ecran + filtrage) | L | 80% | - |
| H | Verification + Notifications (ecrans, Firebase dormant) | M-L | 50%+ | - |

12 liens morts identifies (hamburgers, cloche, onProfilePress x6, search users, cover salon x2,
staff/reviews noms, settings blocked/verification).

Diagnostic complet dans la conversation Claude du 2026-06-15. Aucun code modifie.

### LOT A — NAVIGATION VERS LES PROFILS (2026-06-15) — FAIT

Helper `src/utils/navigateToProfile.ts` : dispatch `userType` -> route profil.
Mapping : client -> /profile/client/[id], hairstylist -> /profile/coiffeur/[id],
salon_owner -> /profile/owner/[id], salon -> /profile/salon/[id], admin -> fallback client.
Type inconnu = warn DEV + noop.

Points cables :
- PostCard onProfilePress : social.tsx, archives/[userId].tsx, salon/[id]/posts.tsx, salon/[id].tsx (4/4)
- Recherche users : social.tsx L241 -> navigateToProfile(u.userType, u.id)
- Equipe salon : salon/[id].tsx staff -> TouchableOpacity, role owner -> salon_owner, sinon hairstylist
- Avis salon : salon/[id].tsx reviews -> TouchableOpacity sur reviewer (client, rev.clientId)
- Post detail : post/[id].tsx author row -> TouchableOpacity
- Comments : post author header -> TouchableOpacity (post.authorUserType dispo)

Dependances backend signalees (NON forcees) :
- CommentResponse n'a PAS de authorUserType -> le nom du commentateur ne peut pas naviguer
  vers le bon profil (seulement l'auteur du POST dans le header). Backend manquant.
- SearchResponse.users est type `any[]` -> depend du JSON reel du backend (userType + id).

Baselines : tsc=0, i18n=668/692 0 ecart.
8 fichiers (1 cree + 7 modifies), 0 backend.

### COMMIT LOT A — c95a463 (2026-06-16)

Lot A commite isolement (feat(profil): navigation vers les profils). Working tree propre
sauf parasite UserResponse.kt (emailVerified) absorbe au lot suivant.

### R0 BACKEND (city + cover user) + R1 COMPOSANTS PROFIL COMMUNS (2026-06-16)

**R0-1 — champ city** :
- Migration V046 (ALTER TABLE users ADD COLUMN city VARCHAR(100) NULL)
- User.kt : champ city nullable
- SocialDto.kt : city ajoute a CoiffeurProfileResponse, ClientProfileResponse,
  SalonOwnerProfileResponse + mapping fromUser
- UpdateProfileRequest : city ajoute (Size max 100)
- UserService.updateUserProfile : city mappe (trim, blank -> null)

**R0-2 — endpoint cover user** :
- VERDICT : endpoint EXISTANT reutilise (SocialController PUT /api/social/users/{userId}/cover-image).
  Frontend peut : POST /api/media/upload (multipart) -> URL, puis appeler l'endpoint existant.
  Aucun doublon cree.

**R0-3** : ./gradlew classes = BUILD SUCCESSFUL.

**R1 — 5 composants profil communs** (src/components/profile/) :
- CoverImage : cover 200px + gradient bas + bouton camera optionnel (onEditCover). Reutilise
  resolveMediaUrl, expo-image, LinearGradient. Gere absence de cover (gradient placeholder).
- StatCounter : valeur + label. Formatage K/M pour grands nombres.
- ProfileTabBar : extrait du pattern salon/[id].tsx (salon NON modifie). Generique :
  tabs:{key,label,icon?}[], activeKey, onChange. RTL via start/end.
- ProfileInfoSection : section a icones. Reutilise SectionHeader + Card. Crayon par ligne
  seulement si onEdit fourni.
- ProfileHeader : assemble CoverImage + Avatar chevauche + nom + badge verifie + sous-titre +
  row StatCounter + bio + zone actions (ReactNode par props). AUCUN appel API/navigation.
  Reutilise Avatar existant.
- Barrel export index.ts. Aucune cle i18n en dur (tout par props).
- AUCUN ecran branche (fondations seulement, R2 pour validation visuelle).

Baselines R0+R1 : backend BUILD OK, tsc=0, i18n=668/692 0 ecart.
Fichiers : 5 backend modifies + 1 migration + 6 frontend crees. salon/[id].tsx intact.

Commits R0/R1 + parasite emailVerified isole :
- 4fdb5ef : feat(profil) R0+R1 (12 fichiers, UserResponse.kt EXCLU)
- 1b6e428 : chore(dto) emailVerified dans UserResponse (isole, origine inconnue)

### R2 PROFIL COIFFEUR REFONDU — PILOTE (2026-06-16)

Refonte complete de profile/coiffeur/[id].tsx avec ossature R1 :
- ProfileHeader (cover+avatar chevauche+nom+badge+sous-titre+stats+bio+actions)
- ProfileTabBar (Publications/Portfolio/Avis)
- ProfileInfoSection (specialites/experience/certifications/instagram/ville)
- FollowButton CREE (src/components/profile/FollowButton.tsx) : etat OPTIMISTE avec
  rollback, reutilise socialApi.followCoiffeur/unfollowCoiffeur, generique coiffeur+salon
- isOwnProfile : actions = Modifier (noop lot R6) + Partager (natif) ; tiers = FollowButton
- Type TS CoiffeurProfileResponse MIS A JOUR (aligne backend : +coverImageUrl, city,
  certifications, instagramHandle, portfolios, recentPosts, badges ; -userId -salonId -salonName)
- Posts : pinned en tete + recentPosts dedupliques, via PostCard avec onProfilePress (lot A)
- 13 cles i18n ajoutees (5 langues) : tabs, type, info, stats, actions. Total 681/705 0 ecart.
- salon/[id].tsx, profils client/owner NON touches.
- SIGNALE : pas d'ecran d'edition coiffeur dedie (bouton Modifier present mais noop, lot R6).
  Upload cover/avatar non branche (lot R6, crayons non affiches R2 = lecture seule prioritaire).
- Onglet Avis = placeholder texte (pas d'endpoint reviews par user dans le DTO coiffeur).

### R3 PROFIL CLIENT + R4 PROFIL OWNER REFONDUS (2026-06-16)

Meme moule que R2 (ProfileHeader + ProfileTabBar + ProfileInfoSection).
- Types TS ClientProfileResponse et SalonOwnerProfileResponse MIS A JOUR (alignes backend :
  +coverImageUrl, city, bio, isVerified, recentPosts, collections, badges, isFollowedByCurrentUser).
- SalonSummaryResponse CREE dans types/profile.ts.
- VERDICT FOLLOW : PAS de followClient ni followOwner dans l'API (seuls followCoiffeur et
  followSalon existent). Pas de FollowButton pour client/owner — tiers voit le profil en
  lecture seule. SIGNALE pour lot futur si necessaire.
- R3 CLIENT : onglets Publications + Collections (donnees du DTO). Pas de favoris (endpoint
  separe owner-only, pas dans le DTO client). Stats : abonnes/abonnements/posts. Subtitle = Client.
- R4 OWNER : onglets Salons + Publications + Collections. Chaque salon navigue vers salon/[id].
  Stats : abonnes/abonnements/salons. Subtitle = Proprietaire de salon.
- 4 cles i18n ajoutees (5 langues) : type.client, type.owner, tabs.collections, tabs.salons.
  Total 685/709 0 ecart.
- salon/[id].tsx et profile/coiffeur NON modifies.
- Baselines : tsc=0, i18n=685/709 0 ecart.

### DIAGNOSTIC SYSTEME FOLLOW — EXTENSION CLIENT/OWNER (2026-06-16)

VERDICT : follow SEMI-GENERIQUE, extension FACILE.

(A) Modele : table unique `follows` (follower_id + following_type VARCHAR(20) + following_id).
Enum FollowingType = { USER, SALON, COIFFEUR }. Le type USER EXISTE DEJA dans l'enum et est
DEJA UTILISE par les compteurs profils client/owner (followersCount lit FollowingType.USER).
Un salon est une entite DISTINCTE d'un user (table salons) — le follow salon cible le salon.id,
pas l'owner.id. « Suivre un owner » = follow USER sur l'owner.id, distinct de suivre ses salons.

(B) Endpoints existants : followSalon, followCoiffeur, unfollowSalon, unfollowCoiffeur (POST/DELETE),
getFollowing, getSalonFollowers, getCoiffeurFollowers (GET). Le service FollowService a unfollow/
isFollowing/getFollowers/getFollowersCount GENERIQUES (prennent un FollowingType) ; seuls
followSalon et followCoiffeur sont SPECIFIQUES (validation du type cible). Il manque followUser.

(C) Impacts :
- FEED SUIVIS : ne remonte que les posts des COIFFEUR et SALON suivis (SocialService:472-480).
  Si on ajoute followUser pour client/owner, le feed devra aussi remonter les posts des USER suivis.
- COMPTEURS : DEJA COHERENTS — les profils client/owner comptent les followers avec FollowingType.USER
  (SocialService:2278/2373) et followingCount = countByFollowerId (tous types). Pas d'adaptation.
- isFollowedByCurrentUser : DEJA CABLE pour client (SocialService) et owner (SocialService:2398-2406)
  avec FollowingType.USER. Le backend RENVOIE deja le bon booleen.
- NOTIFICATIONS : aucun systeme de notif follow actif (Firebase dormant).
- CONTRAINTES : unicite (follower_id, following_type, following_id) = pas de doublon. Pas de garde
  soi-meme explicite dans followCoiffeur (l'UI ne montre pas le bouton si isOwnProfile).

(D) Plan d'implementation recommande (APPROCHE UNIFIEE) :
1. BACKEND : ajouter followUser(followerId, userId) dans FollowService (verifie que userId existe,
   empeche de se suivre soi-meme, FollowingType.USER). 2 endpoints dans FollowController :
   POST/DELETE /api/social/users/{userId}/follow + GET /api/social/users/{userId}/followers.
   Pas de migration (la table/enum supportent deja USER).
2. FEED : ajouter les followedUserIds (FollowingType.USER) dans getFollowingFeed pour remonter
   leurs posts (meme pattern que followedCoiffeurIds).
3. FRONTEND : etendre FollowTargetType dans FollowButton ('coiffeur'|'salon'|'user'), ajouter
   followUser/unfollowUser dans social.ts, brancher le FollowButton sur les profils client/owner.
Ampleur estimee : ~50 lignes backend, ~20 lignes frontend. Pas de migration. Risque faible.

### FOLLOW UNIFIE followUser + CABLAGE PROFILS CLIENT/OWNER + FEED (2026-06-16)

Implementation complete des 5 etapes du plan :
1. FollowService.followUser : FollowingType.USER, garde anti-soi-meme, verif existence target.
2. FollowController : POST/DELETE /api/social/users/{userId}/follow + GET .../followers.
3. Feed Suivis (SocialService.getFollowingFeed) : ajoute followedUserIds (FollowingType.USER),
   posts remontes via authorId. Deduplication GARANTIE par .distinctBy { it.id } deja en place
   (ligne 519) — un coiffeur suivi en COIFFEUR + USER ne produit pas de doublon.
4. Frontend : followUser/unfollowUser dans social.ts.
5. FollowButton : FollowTargetType etendu avec 'user', dispatch interne ajoute.
6. Profils client/owner : FollowButton cable en zone tiers (targetType='user',
   isFollowed=profile.isFollowedByCurrentUser). Propre profil = Modifier+Partager inchange.
Pas de migration. Backend BUILD OK, tsc=0, i18n=685/709 0 ecart. salon intact.

---

## R6 — DIAGNOSTIC « MON PROFIL + EDITION » (2026-06-16, LECTURE SEULE)

### (A) INVENTAIRE COMPLET DE (tabs)/profile.tsx — A PRESERVER

Ecran central (225 lignes), source de donnees = authStore.user (GET /api/users/me via refreshUser)
+ appel profil role-specifique pour stats (profilesApi.getXxxProfile). PAS de pull-to-refresh.

| Element                  | Impl. actuelle                                     | Destination / action                              |
|--------------------------|----------------------------------------------------|----------------------------------------------------|
| Avatar (88px rond)       | Image cliquable -> pickAvatar (expo-image-picker)  | Ouvre galerie, preview + Annuler/Enregistrer        |
| Upload avatar            | mediaApi.uploadImage -> usersApi.updateUserAvatar   | PATCH /api/users/{id}/avatar, puis refreshUser()    |
| Badge camera             | Icone camera-alt en bas-droite de l'avatar          | Visuel seulement, meme onPress que avatar           |
| Nom complet              | user.firstName + user.lastName                      | Affichage seul                                      |
| Email                    | user.email                                          | Affichage seul                                      |
| Badge verifie            | user.isVerified OU user.emailVerified               | Affichage conditionnel                              |
| Stats (3 compteurs)      | Differents selon userType (voir ci-dessous)         | Affichage seul, pas cliquables                      |
| Menu Favoris             | icon favorite-border                                | router.push(/favorites/{userId})                    |
| Menu Archives            | icon archive                                        | router.push(/archives/{userId})                     |
| Menu Collections         | icon collections-bookmark                           | router.push(/collections/user/{userId})             |
| Menu Portfolios          | icon photo-library                                  | router.push(/portfolios?ownerId=...&ownerType=...)  |
| Menu Reglages            | icon settings                                       | router.push(/settings)                              |
| Bouton Deconnexion       | Bordure rouge, icone logout                         | Ouvre LogoutConfirmModal                            |

Stats par type :
- client : bookings / collections / posts
- salon_owner : salons / followers / posts
- hairstylist : followers / posts / likes

Donnees : l'objet `user` du store est reconstitue SOIT par authResponseToUser (login, 7 champs) SOIT
par usersApi.getCurrentUser (GET /me, User complet). refreshUser() fait GET /me -> user complet avec
avatarUrl, coverImageUrl, bio, city, etc. Donc TOUTES ces donnees sont disponibles dans le store.

Ce qui MANQUE par rapport aux profils publics refondus (R2-R4) :
- Pas de cover image
- Pas de bio affichee
- Pas de ville affichee
- Pas de ProfileHeader / ProfileTabBar / onglets (posts, portfolios, collections)
- Pas de bouton « Modifier le profil » (les profils publics l'ont en noop)
- Pas de bouton Partager
- Design ancien (pas de ring avatar, pas de Cormorant Garamond)

### (B) EDITION — ENDPOINTS BACKEND REELS

#### B1. PUT /api/users/me (UserController:580-590, UserService:574-591)
UpdateProfileRequest (UpdateProfileRequest.kt:12-41) :
  - firstName (Size 2-50)
  - lastName (Size 2-50)
  - bio (Size max 500)
  - city (Size max 100)
  - avatarUrl (libre)
  - preferredLanguage (Pattern fr|en|es|de|ar)
  - instagramHandle (Size max 100)
  - yearsExperience (libre)
NOTE : phoneNumber RETIRE (endpoint dedie PUT /me/phone)

#### B2. PATCH /api/users/{userId}/avatar (UserController:740-760)
UpdateAvatarRequest : { avatarUrl: String }
Ownership check (403 si pas soi-meme). Flux actuel dans (tabs)/profile.tsx :
  1. expo-image-picker -> URI locale
  2. mediaApi.uploadImage(uri, fileName) -> POST /api/media/upload -> retourne path
  3. usersApi.updateUserAvatar(userId, path) -> PATCH /users/{id}/avatar -> retourne User
  4. refreshUser() -> GET /me -> met a jour authStore

#### B3. PUT /api/social/users/{userId}/cover-image (SocialController:1805-1833)
Body : { coverImageUrl: String }. Ownership check. Service : UserService:602-618 (max 500 chars).
Frontend : usersApi.updateUserCoverImage(userId, coverImageUrl) EXISTE (users.ts:23-24).
Pipeline : identique a l'avatar (mediaApi.uploadImage -> path -> PUT cover-image).
UI : AUCUNE — a creer.

#### B4. PUT /api/social/coiffeurs/{coiffeurId}/profile (SocialController:1049-1060)
UpdateCoiffeurProfileRequest (SocialDto.kt:571-577) :
  - bio (max 1000)
  - specialties (List<String>, max 5, chaque max 100)
  - yearsExperience (0-100)
  - certifications (max 2000)
  - instagramHandle (max 30, alphanum+._)
  - portfolioHighlightedId
Frontend : profilesApi.updateCoiffeurProfile EXISTE (profiles.ts:18-19).
ATTENTION : le type TS UpdateCoiffeurProfileRequest (profile.ts:56-60) n'a que 3 champs sur 6 !
  Manquent : certifications, instagramHandle, portfolioHighlightedId.

#### B5. PUT /api/social/salons/{salonId}/profile (SocialController:1127-1138)
UpdateSalonSocialProfileRequest (SocialDto.kt:871-874) :
  - socialDescription (max 2000)
  - socialCoverImage
  - highlightedPostIds (List<String>)
Frontend : profilesApi.updateSalonSocialProfile EXISTE (profiles.ts:24-25).
ATTENTION : le type TS (profile.ts:82-84) n'a que description — manquent socialCoverImage, highlightedPostIds.

#### B6. POST /api/media/upload (MediaController:80-127)
Multipart, retourne { url, path, uploadedBy, filename }.
Frontend : mediaApi.uploadImage (media.ts:5-34) — cross-platform (web blob / native FormData).

#### B7. Pas d'endpoint UPDATE pour client-only ou owner-only
  - Client/owner editent bio/ville/avatar/cover via PUT /me et les endpoints B2/B3 generiques.
  - Seul le coiffeur a un endpoint role-specifique (B4).

### (C) TABLEAU CHAMP EDITABLE -> ENDPOINT -> TYPE

| Champ             | Endpoint                              | client | coiffeur | owner |
|-------------------|---------------------------------------|--------|----------|-------|
| firstName         | PUT /api/users/me                     | oui    | oui      | oui   |
| lastName          | PUT /api/users/me                     | oui    | oui      | oui   |
| bio               | PUT /api/users/me (500c)              | oui    | oui      | oui   |
| bio (etendue)     | PUT coiffeurs/{id}/profile (1000c)    | —      | oui      | —     |
| city              | PUT /api/users/me                     | oui    | oui      | oui   |
| avatarUrl         | PATCH /users/{id}/avatar              | oui    | oui      | oui   |
| coverImageUrl     | PUT /users/{id}/cover-image           | oui    | oui      | oui   |
| instagramHandle   | PUT /api/users/me OU coiffeur profile | oui*   | oui      | oui*  |
| yearsExperience   | PUT /api/users/me OU coiffeur profile | —      | oui      | —     |
| specialties       | PUT coiffeurs/{id}/profile            | —      | oui      | —     |
| certifications    | PUT coiffeurs/{id}/profile            | —      | oui      | —     |
| portfolioHighl.   | PUT coiffeurs/{id}/profile            | —      | oui      | —     |
| preferredLanguage | PUT /api/users/me                     | oui    | oui      | oui   |
*instagramHandle dans /me : accepte mais pas affiche sur profils client/owner actuellement

### (D) ARCHITECTURE EDITION — PROPOSITION

#### Option (i) : Ecran d'edition DEDIE (app/edit-profile.tsx)
- Le bouton « Modifier le profil » (profils publics isOwnProfile) et (tabs)/profile.tsx naviguent
  vers un ecran formulaire dedie.
- Formulaire avec TextInputs (nom, bio, ville, instagram...), sections conditionnelles par userType.
- Upload avatar/cover integres dans cet ecran.
- PRO : separation claire lecture/ecriture, plus simple a tester, pas de mode inline complexe.
- PRO : coherent avec le pattern Instagram/TikTok (bouton Modifier -> ecran dedie).
- CON : un ecran de plus a creer.

#### Option (ii) : Edition inline sur (tabs)/profile.tsx avec crayons
- Chaque section a un crayon qui passe en mode edition in-place.
- PRO : pas de navigation supplementaire.
- CON : gestion d'etat complexe (mode edition/lecture par section), risque de bugs, incoherent
  avec les profils publics qui ont « Modifier le profil » comme bouton unique.

#### RECOMMANDATION : Option (i) — ecran dedie
Raisons :
1. Les profils publics (profile/{type}/[id].tsx) ont DEJA un bouton « Modifier le profil » qui
   ne fait rien (noop). Il suffit de le brancher vers router.push('/edit-profile').
2. (tabs)/profile.tsx peut etre refonde visuellement (ProfileHeader, onglets) sans toucher a
   l'edition, qui vit dans son propre ecran.
3. Le pipeline upload avatar EXISTANT dans (tabs)/profile.tsx peut etre DEPLACE ou DUPLIQUE dans
   l'ecran d'edition (meme flux : pick -> preview -> mediaApi.upload -> PATCH).
4. Pattern standard des apps sociales (Instagram, TikTok, X).

#### Articulation (tabs)/profile.tsx <-> profils publics refondus
PROPOSITION : (tabs)/profile.tsx REUTILISE ProfileHeader (R1) pour l'ossature visuelle
(cover, avatar avec ring, nom, badge, stats, bio, boutons Modifier+Partager) mais GARDE son
menu propre (favoris/archives/collections/portfolios/settings) et son bouton deconnexion en
dessous. L'ecran reste DISTINCT des profils publics (il ne charge PAS via profilesApi.getXxxProfile
pour l'affichage principal — il utilise authStore.user enrichi par GET /me). Les stats viennent
toujours de l'appel profil role-specifique (comme maintenant).

### (E) DECOUPAGE EN SOUS-ETAPES VALIDABLES

#### R6a — Ossature visuelle de « Mon Profil » (tabs)/profile.tsx
- Reutiliser ProfileHeader (cover, avatar ring, nom, badge verifie, sous-titre type, stats, bio)
- Zone actions = « Modifier le profil » (navigue vers /edit-profile, a creer R6b) + « Partager »
- Conserver TOUT le menu existant (favoris, archives, collections, portfolios, settings)
- Conserver le bouton deconnexion + LogoutConfirmModal
- Ajouter pull-to-refresh
- PAS de cover upload encore (affichage seul avec placeholder)
- Dependances : R1 composants (deja faits)
- Ampleur : ~80 lignes modifiees dans (tabs)/profile.tsx

#### R6b — Ecran d'edition champs texte (app/edit-profile.tsx)
- Nouveau fichier app/edit-profile.tsx
- Formulaire : firstName, lastName, bio, city, instagramHandle
- Sections conditionnelles coiffeur : specialties, yearsExperience, certifications
- Appels : PUT /api/users/me (champs communs) + PUT coiffeurs/{id}/profile (champs coiffeur)
- Ajouter usersApi.updateProfile dans users.ts (appel PUT /me — ABSENT aujourd'hui)
- Aligner les types TS UpdateCoiffeurProfileRequest (ajouter certifications, instagramHandle,
  portfolioHighlightedId)
- Brancher le bouton « Modifier le profil » de R6a + des profils publics (isOwnProfile)
- Dependances : R6a
- Ampleur : ~200 lignes (ecran) + ~15 lignes (api/types)

#### R6c — Upload cover + avatar dans l'ecran d'edition
- Deplacer le pipeline avatar existant de (tabs)/profile.tsx vers edit-profile.tsx
  (ou le garder dans les deux — pick avatar dans les deux endroits est un choix UX)
- Ajouter upload cover : meme pipeline (pick -> mediaApi.uploadImage -> PUT cover-image)
- La cover s'affiche via ProfileHeader (deja cable via coverUrl prop)
- Dependances : R6b
- Ampleur : ~60 lignes (reutilise le pattern avatar existant)

### Baselines inchangees
gradlew classes = BUILD SUCCESSFUL, tsc --noEmit = 0, i18n 685/709 0 ecart.
Aucun fichier modifie sauf cette trace.

### R6a — REFONTE VISUELLE « MON PROFIL » + RETRAIT BOUTON MODIFIER PROFILS PUBLICS (2026-06-16)

Implementation :
1. (tabs)/profile.tsx REFONDE avec ProfileHeader R1 :
   - Cover (affichage seul, placeholder si vide, PAS d'upload R6c)
   - Avatar avec ring (coherent R2-R4), affichage seul (pipeline upload CONSERVE en code
     mort commente TODO R6c, pret a etre deplace vers ecran d'edition)
   - Nom (CormorantGaramond), badge verifie, sous-titre type (profile.type.hairstylist/client/owner)
   - Stats role-specifiques INCHANGES (client: bookings/collections/posts, owner: salons/followers/
     posts, hairstylist: followers/posts/likes)
   - Bio (user.bio via authStore, affichage seul)
   - Actions = « Modifier le profil » (NOOP TODO R6b) + « Partager » (Share natif)
   - Pull-to-refresh ajoute (RefreshControl -> refreshUser + loadStats)
2. PRESERVE : menu 5 items (favoris/archives/collections/portfolios/settings, routes inchangees),
   bouton deconnexion + LogoutConfirmModal. Email RETIRE de l'affichage (deja visible dans Settings,
   pas montre sur les profils publics non plus — coherence).
3. Profils publics (profile/coiffeur/[id], client/[id], owner/[id]) : bouton « Modifier le profil »
   RETIRE du cas isOwnProfile. Reste UNIQUEMENT « Partager ». Cas tiers INCHANGE (FollowButton).
4. Aucune cle i18n ajoutee (profile.editProfile existait deja dans les 5 langues).
5. salon/[id].tsx NON modifie.
Baselines : tsc=0, i18n 685/709 0 ecart, gradlew classes inchange (aucun backend modifie dans ce lot).

### R6c — UPLOAD COVER (pipeline calque avatar) (2026-06-16)

Implementation :
1. Pipeline cover SYMETRIQUE au pipeline avatar dans (tabs)/profile.tsx :
   - Etats SEPARES : coverPreview (string|null) + isUploadingCover (bool) — pas de conflit avec avatar.
   - pickCover : ImagePicker aspect 16:9 (cover), quality 0.8.
   - saveCover : mediaApi.uploadImage -> usersApi.updateUserCoverImage (users.ts:23, PUT /api/social/
     users/{id}/cover-image) -> refreshUser -> reset coverPreview.
   - cancelCoverPreview : reset coverPreview.
2. Cable sur ProfileHeader :
   - coverUrl={coverPreview || user?.coverImageUrl} (preview avant upload).
   - onEditCover={pickCover} (remplace le NOOP).
3. Barre preview cover : Annuler/Enregistrer DUPLIQUEE (pas factorisee — 7 lignes, pas assez pour un
   composant dedie). Affichee au-dessus de la barre preview avatar si les deux sont actifs (cas rare).
4. Non-regression avatar : pipeline avatar INTACT (renomme isUploading -> isUploadingAvatar,
   cancelPreview -> cancelAvatarPreview pour clarte).
5. Aucune cle i18n ajoutee (reutilise common.actions.cancel/save). Backend NON modifie. salon NON modifie.
Baselines : tsc=0, i18n 685/709 0 ecart.

### BOTTOM SHEET EDITION (pilote) + CHAMP BIO (2026-06-16)

Diagnostic :
- D1 : bio = user.bio UNIQUE (500c via PUT /me). Les 3 profils publics + Mon Profil lisent le meme
  champ. CoiffeurProfileRequest a un bio mais mappe le meme user.bio. Pas de 2e bio.
- D2 : PUT /api/users/me n'avait AUCUN appel frontend. Ajoute usersApi.updateProfile (users.ts).
- D3 : pas de lib bottom sheet. Modal natif RN + Animated (calque LogoutConfirmModal). Zero dependance.
- D-cover-bugfix : UserResponse.kt n'incluait ni coverImageUrl ni bio -> ajoutes (lot precedent).
- D-cover-url : users.ts coverImage URL corrigee /api/social/users/{id}/cover-image (etait /api/users).

Implementation :
1. EditBottomSheet.tsx (src/components/profile/) : composant GENERIQUE reutilisable. Modal natif,
   backdrop tap-to-close, grabber, titre CormorantGaramond, X, children, Annuler/Enregistrer,
   Animated slide+fade, KeyboardAvoidingView. Exporte via index.ts.
2. ProfileHeader : ajout prop onEditBio. Bio tappable avec crayon-outline quand isOwnProfile+onEditBio.
   Affiche meme sans bio (pour pouvoir en ajouter une).
3. (tabs)/profile.tsx : etat showBioSheet/bioDraft/isSavingBio. openBioSheet pre-remplit. saveBio appelle
   usersApi.updateProfile({bio}) -> refreshUser -> ferme. Erreur affichee par Alert (pas de catch vide).
   TextInput multiline + compteur N/500.
4. usersApi.updateProfile ajoute (PUT /api/users/me). Couvre tous les champs du DTO backend pour les
   futurs lots (firstName/lastName/bio/city/instagramHandle/yearsExperience/preferredLanguage).
5. i18n : 2 cles ajoutees (profile.editBioTitle + profile.editBioPlaceholder) dans 5 langues.
   Total : 687/711, 0 ecart.
6. CoverImage.tsx : bouton camera deplace top:12 (etait bottom:12, bloque par le z-index avatar).
Backend : UserResponse.coverImageUrl + bio ajoutes (lot precedent, BUILD OK).
Baselines : tsc=0, i18n 687/711 0 ecart. salon/profils publics NON modifies.

### BOTTOM SHEETS VILLE + INSTAGRAM (2026-06-16)

Diagnostic :
- D1 : Mon Profil n'affichait NI city NI instagram. Profils publics les affichaient via ProfileInfoSection.
  -> ProfileInfoSection AJOUTEE sur Mon Profil avec onEdit par item (crayon discret en bout de ligne).
- D2 : instagramHandle backend = @Size(max=100) via PUT /me. Pas de @Pattern dans UpdateProfileRequest
  (la validation stricte 30c alphanum+._ est dans UpdateCoiffeurProfileRequest.validate() seulement).
- D3 : city = @Size(max=100).
- UserResponse.kt : city + instagramHandle MANQUAIENT -> ajoutes (champs + fromEntity mapping).
- User.ts frontend : city MANQUAIT -> ajoute.

Implementation :
1. (tabs)/profile.tsx : 2 sheets sur le moule bio (EditBottomSheet reutilise) :
   - City : cityDraft/isSavingCity, openCitySheet, saveCity -> updateProfile({city}) -> refreshUser.
     TextInput 1 ligne, compteur N/100. Alert erreur backend.
   - Instagram : instagramDraft/isSavingInstagram, openInstagramSheet, saveInstagram ->
     updateProfile({instagramHandle}) -> refreshUser. TextInput 1 ligne autoCapitalize=none,
     compteur N/100. Alert erreur backend.
2. ProfileInfoSection ajoutee sur Mon Profil (entre ProfileHeader et Menu) : ville + instagram,
   chaque item avec onEdit -> ouvre le sheet correspondant. Affiche "—" si vide (editable quand meme).
3. Profils publics NON modifies (lecture seule, pas de onEdit).
4. i18n : 4 cles ajoutees (editCityTitle/Placeholder + editInstagramTitle/Placeholder) x 5 langues.
5. Backend : UserResponse.kt += city + instagramHandle (champs + fromEntity).
Baselines : tsc=0, i18n 691/715 0 ecart, gradlew classes BUILD OK.

### PROFILEINFOCARD INLINE UNIFIE + ORDRE BLOCS PROFILS PUBLICS (2026-06-16)

1. COMPOSANT ProfileInfoCard.tsx (src/components/profile/) : carte inline unifiee SANS titre.
   Props : items[] avec icon + value + onEdit? optionnel. Fond surface, coins arrondis, filets de
   separation. onEdit present -> crayon en bout de ligne ; absent -> lecture seule.
2. MON PROFIL : remplace la carte infos inline ad-hoc par ProfileInfoCard AVEC onEdit (ville ->
   openCitySheet, instagram -> openInstagramSheet). Meme rendu, code factorise.
3. 3 PROFILS PUBLICS : ProfileInfoSection (gros titre) remplacee par ProfileInfoCard (pas de titre,
   lecture seule). DEPLACEE entre ProfileHeader/actions et onglets (ordre cible respecte).
   Infos par type (uniquement si presentes) :
   - Coiffeur : ville, instagram, experience, certifications, specialites
   - Client : ville
   - Owner : ville
4. Ancienne ProfileInfoSection : LAISSEE en place (pas utilisee mais ne casse rien, pourra servir
   sur d'autres ecrans).
5. Aucune cle i18n ajoutee. salon/[id].tsx NON modifie. Backend NON modifie.
Baselines : tsc=0, i18n 691/715 0 ecart.

### EDITION COIFFEUR (experience + certifications + specialites) + CARTE INFOS CONDITIONNELLE (2026-06-16)

Diagnostic :
- D1 : update MIXTE (SocialService.kt:2082-2136) — bio/yearsExp/certifs/instagram = PARTIEL (?.let),
  specialties = HYBRIDE (if isNotEmpty clear+add, default emptyList ne declenche pas le clear).
  Approche DEFENSIVE : objet COMPLET envoye a chaque save (buildCoiffeurPayload).
- D2 : getCoiffeurProfile appele dans loadStats mais profil NON stocke -> ajoute etat coiffeurProfile
  (CoiffeurProfileResponse) stocke au chargement + recharge apres chaque save coiffeur.
- D3 : UpdateCoiffeurProfileRequest TS aligne sur le DTO Kotlin (6 champs : bio, specialties,
  yearsExperience, certifications, instagramHandle, portfolioHighlightedId).
- D4 : bio 150, specialties max 5 x 100c, yearsExperience 0-100, certifications 2000,
  instagramHandle max 30 alphanum+._

Implementation :
1. Carte infos Mon Profil (ProfileInfoCard) : conditionnelle au type. Coiffeur = ville + instagram +
   experience + certifications + specialites (5 items editables). Client/owner = ville + instagram.
2. Sheet EXPERIENCE : TextInput number-pad, validation 0-100 (regle backend), objet complet.
3. Sheet CERTIFICATIONS : TextInput multiline, compteur N/2000, objet complet.
4. Sheet SPECIALITES : chips activables (toggle), suggestions FR en dur (13 termes coiffure courants),
   chips custom existantes preservees, chip "Autre" (TextInput inline + submit), compteur N/5.
   Objet complet.
5. Suggestions specialites en dur FR (raffinement i18n futur signale).
6. i18n : 7 cles ajoutees (editExp.title/hint/rangeError + editCerts.title/placeholder +
   editSpec.title/other) x 5 langues. Total 698/722, 0 ecart.
7. ANTI-ECRASEMENT : buildCoiffeurPayload lit TOUTES les valeurs du CoiffeurProfileResponse actuel
   puis override le seul champ edite. Teste : editer experience ne doit pas effacer specialites.
salon NON modifie. Profils publics NON modifies. Backend NON modifie.
Baselines : tsc=0, i18n 698/722 0 ecart.

### CLOTURE REFONTE PROFILS — NETTOYAGE + COMMIT UNIQUE + PUSH (2026-06-16)

1. Nettoyage : ProfileInfoSection orpheline SUPPRIMEE (fichier + export).
2. Revue : 27 fichiers (6 backend + 21 frontend), aucun parasite, salon non touche.
3. Baselines finales : gradlew classes BUILD OK, tsc 0, i18n 698/722 0 ecart.
4. Commit unique : 6313e97 « feat(profils): refonte complete des profils (publics + mon profil) »
5. Push : frollot/main f30c8c3..6313e97 — SUCCES. Working tree propre.
REFONTE PROFILS TERMINEE.

---

## DIAGNOSTIC ROLE PROPRIETAIRE DE SALON (2026-06-16, LECTURE SEULE)

Constat : connecte en salon_owner, l'experience est tres limitee. Aucun menu « Mes salons » dans
Mon Profil. Les ecrans create-staff et create-service existent mais sont INATTEIGNABLES (zero
navigation). create-salon accessible uniquement quand 0 salon (EmptyState home). owner-bookings
n'a que l'annulation (pas de confirmer/refuser/completer). Aucun dashboard, aucune gestion
services/equipe/avis depuis l'app.

Backend : staff CRUD complet, services CRUD complet, bookings workflow complet (pending->confirmed
->in_progress->completed/cancelled/no_show), queue ok, paiements ok. MANQUENT : edit salon general
(PUT /salons/{id}), delete salon, reply review, horaires d'ouverture (aucun modele).

Decoupage propose : L1 dashboard owner (hub central, structurant) -> L2 services CRUD (relier
ecrans morts) -> L3 equipe (idem) -> L4 bookings owner complets (confirmer/refuser) -> L5 edit
salon -> L6 profil social salon -> L7 avis+reponses -> L8 stats -> L9 horaires (full-stack).
Recommandation : commencer par L1 (hub) car sans point d'entree centralise, les autres lots
n'ont pas de chemin d'acces coherent.

### TABLEAU DE BORD OWNER LOT 1 (T1+T2+T4) (2026-06-16)

T1 — Point d'entree + ossature :
- Menu Mon Profil : item « Mon salon » (icone store) VISIBLE UNIQUEMENT si salon_owner, navigue
  vers /owner-dashboard. Les autres types ne le voient pas.
- Ecran owner-dashboard.tsx : topBar + pills categories (Activite actif, les 3 autres visuelles
  non interactives — design preserve, navigation interne viendra dans lots suivants).
- Bandeau salon actif : charge getSalonsByOwner. Si 1 salon = affichage simple. Si multi-salon =
  selecteur via Alert (chaque salon = bouton). Si 0 salon = etat vide avec lien create-salon.
  Photo + nom + ville du salon actif.

T2 — 4 cartes metriques (grille 2x2, donnees GLOBALES sans periode) :
- Reservations : bookingsApi.getBookingStatistics -> totalBookings.
- Revenus : bookingStats.revenue -> arrondi FCFA.
- Note moyenne : reviewsApi.getSalonReviewStats -> averageRating (totalReviews entre parentheses).
- Abonnes : salonsApi.getSalonById -> followersCount (getSalonsByOwner renvoie null pour ce champ).
- Chargement en parallele (Promise.allSettled), chaque echec gere isolement (affiche « — »).
- PAS de variation/tendance % (pas de periode, c'est T7).

T4 — Grille « Gerer mon salon » (7 tuiles) :
- ACTIVES (route existante) : Reservations -> /owner-bookings, File d'attente -> /queue-management.
- « BIENTOT » (opacite 0.5, toast « Bientot disponible ») : Prestations, Equipe, Horaires, Avis,
  Paiements — ecrans cibles a construire dans L2-L9.

i18n : 21 cles (ownerDashboard.*) x 5 langues. Total 719/743, 0 ecart.
Backend NON modifie. salon/[id].tsx NON modifie.
Baselines : tsc=0, i18n 719/743 0 ecart.

### TABLEAU DE BORD OWNER T3 (a confirmer + actions) (2026-06-16)

Section « A confirmer » inseree entre metriques et grille « Gerer mon salon ».
- CHARGEMENT : getSalonBookings -> filtre client-side PENDING, en parallele avec metriques.
  Spinner pendant chargement ; erreur -> console.error + liste vide.
- AFFICHAGE : titre « A confirmer » + badge rouge « N en attente ». Par reservation :
  initiales client (cercle primaryContainer), nom, service, date/heure lisible + prix.
  MAX 3 affichees, « Voir tout » -> /owner-bookings si > 3.
  Zero pending -> message « Aucune reservation en attente » (section VISIBLE, pas masquee).
- CONFIRMER : updateBookingStatus(id, { status: CONFIRMED }). Succes -> retire de la liste
  + refresh metriques + Alert succes. Erreur -> Alert erreur, reste en liste.
- REFUSER : ouvre EditBottomSheet (prop saveLabel ajoutee) avec TextInput raison optionnel.
  Appel updateBookingStatus(id, { status: CANCELLED, notesSalon? }). Meme pattern succes/erreur.
- COHERENCE : apres chaque action, loadMetrics() relance -> carte « Reservations » et badge
  a jour. Pull-to-refresh recharge tout (metriques + pending).
- EditBottomSheet : ajout prop saveLabel?: string (fallback t('common.actions.save')).

i18n : +11 cles (pending.*) x 5 langues. Total 730/754, 0 ecart.
Backend NON modifie. salon/[id].tsx NON modifie.
Baselines : tsc=0, i18n 730/754 0 ecart.

---

### DIAGNOSTIC : Inventaire permissions roles (2026-06-17)

Chantier transversal OWNER -> systeme de permissions par role.
Statut : INVENTAIRE TERMINE, matrice vide livree, en attente decision humaine.

#### Mecanisme actuel
- Autorisation owner = `salon.owner?.id != userId` DUPLIQUE dans ~15 endroits / 8 services
- staff.role JAMAIS lu pour autoriser (purement cosmetique)
- Frontend : `user.userType === 'salon_owner'` = seule source, pas de garde de route
- 3 classes d'exception differentes pour le meme concept (UnauthorizedAccessException x2, UnauthorizedException)

#### Failles identifiees (AUCUN owner check)
- CRITIQUE : GET /api/salons/{salonId}/bookings/statistics + /daily (revenus exposes)
- CRITIQUE : GET /api/salons/{salonId}/bookings + /upcoming (donnees clients)
- ELEVE : GET /api/payments/salon/{salonId} (historique financier)
- ELEVE : GET /api/staff/{staffId}/bookings (RDV d'un autre coiffeur)
- MOYEN : GET /api/bookings/{bookingId} (check incomplet, commentaire "on laisse passer")

#### Matrice vide livree
33 actions x 4 roles (owner/manager/hairstylist/apprentice).
7 domaines : SALON, PRESTATIONS, EQUIPE, INVITATIONS, RESERVATIONS, FILE_ATTENTE, AVIS,
PORTFOLIO, PAIEMENTS, PROFIL_SOCIAL, VERIFICATION.
Decoupage propose : 8 lots (Lot 0 decision -> Lot 7 tests).

Baselines inchangees : tsc=0, i18n 796 cles 0 ecart, backend BUILD SUCCESSFUL.

### LOT SECURITE : Fermeture endpoints sans owner check (2026-06-17)

Failles fermees (5/5), pattern `salon.owner?.id != userId` reutilise, exception
`BookingService.UnauthorizedAccessException` (coherent avec l'existant).

| Faille | Endpoint | Check ajoute | Curl OWNER | Curl OTHER |
|--------|----------|-------------|------------|------------|
| F1 CRITIQUE | GET /salons/{id}/bookings/statistics | owner-only (service) | 200 | 403 |
| F1 CRITIQUE | GET /salons/{id}/bookings/daily | owner-only (service) | 200 | 403 |
| F2 CRITIQUE | GET /salons/{id}/bookings | owner-only (service) | 200 | 403 |
| F2 CRITIQUE | GET /salons/{id}/bookings/upcoming | owner-only (service) | 200 | 403 |
| F3 ELEVE | GET /payments/salon/{id} | owner-only (controller) | 200 | 403 |
| F4 ELEVE | GET /staff/{id}/bookings | self OR owner (service) | 200 | 403 |
| F5 MOYEN | GET /bookings/{id} | client OR staff OR owner (service) | 200 | 403 |

Fichiers modifies (backend uniquement) :
- BookingService.kt : +userId param sur 5 methodes, checks owner/staff/client
- BookingController.kt : passe authenticatedUserId a chaque appel service
- PaymentController.kt : +SalonRepository, +getAuthenticatedUserId, check owner sur getPaymentsBySalon

Non-regression frontend : les 4 appels (owner-dashboard.tsx, owner-bookings.tsx) passent le
token OWNER -> toujours 200. Aucun ecran non-owner n'appelle ces endpoints.
Backend BUILD SUCCESSFUL. PAS de commit.

### PERMISSIONS LOT 1 : Socle backend (tables + seed matrice + service central) (2026-06-17)

Architecture choisie : OPTION B (table en base). Owner = cas special (TRUE direct, pas dans la table).

#### Migration V048
- Table `permissions` : 32 permissions (cle unique VARCHAR(60))
- Table `role_permissions` : seed matrice 26 lignes (manager=21, hairstylist=4, apprentice=1)
- Index sur (role)

#### Fichiers crees
- V048__create_role_permissions.sql (migration + seed)
- RolePermissionRepository.kt (JdbcTemplate, charge matrice role->permissions)
- SalonAuthorizationService.kt (service central : hasPermission, requirePermission, getUserRole,
  getUserPermissions)
- SalonController.kt modifie (+endpoint GET /api/salons/{salonId}/my-permissions)

#### Logique SalonAuthorizationService
1. userId == salon.owner.id -> TRUE (toutes permissions, court-circuit)
2. Sinon, cherche SalonStaff(salonId, userId) -> pas de staff ou inactif -> FALSE
3. Verifie role_permissions cache (charge @PostConstruct, reloadPermissions() si besoin)

#### Cache
- Matrice role->permissions chargee UNE FOIS au demarrage (@PostConstruct)
- Donnees de reference stables -> pas de TTL, invalidation par redemarrage ou reloadPermissions()
- Le lien user->salon->role est resolu a chaque appel (Hibernate L1 cache en transaction)

#### Tests curl (my-permissions)
| Role | Perms count | service.create | staff.add | payment.refund | booking.view_own |
|------|------------|----------------|-----------|----------------|------------------|
| owner | 32 (toutes) | TRUE | TRUE | TRUE | TRUE |
| manager | 21 | TRUE | FALSE | FALSE | TRUE |
| hairstylist | 4 | FALSE | FALSE | FALSE | TRUE |
| no-staff | 0 | FALSE | FALSE | FALSE | FALSE |

#### Recommandation scope OWN (booking.manage_status / booking.cancel coiffeur)
Le coiffeur a la PERMISSION mais ne peut agir que sur SES reservations (staff assigne).
Recommandation : au lot branchement bookings, le service BookingService verifiera :
  1. requirePermission(userId, salonId, "booking.manage_status")
  2. SI role != owner && role != manager -> verifier booking.staff.user.id == userId
Pas de permission distincte "booking.manage_own_status" (surcharge inutile) — le scope
est un CHECK SUPPLEMENTAIRE dans la logique metier, pas dans la matrice.

AUCUN endpoint existant modifie par ce lot. Backend BUILD SUCCESSFUL. PAS de commit.

### PERMISSIONS LOT 3 : Branchement prestations/equipe/invitations (2026-06-18)

Remplacement des checks owner en dur par requirePermission dans 3 services.

#### Fichiers modifies (backend uniquement)
- SalonServiceService.kt : +injection SalonAuthorizationService, 3 checks remplaces (create/update/delete) + 1 check AJOUTE (importServices n'avait AUCUN check)
- SalonStaffService.kt : +injection SalonAuthorizationService, 3 checks remplaces (add/update/remove)
- InvitationService.kt : +injection SalonAuthorizationService, 4 checks remplaces (search/create/list/cancel) avec permissions DIFFERENTES par endpoint
- SalonServiceController.kt : fix importServices — userId n'etait pas passe au service (1 ligne)

#### Mapping endpoint -> permission applique
| Endpoint | Permission | Roles autorises |
|----------|-----------|-----------------|
| POST /salons/{id}/services | service.create | owner + manager |
| PUT /salons/{id}/services/{sid} | service.update | owner + manager |
| DELETE /salons/{id}/services/{sid} | service.delete | owner + manager |
| POST /salons/{id}/services/batch | service.import | owner + manager |
| POST /salons/{id}/staff | staff.add | owner SEUL |
| PUT /salons/{id}/staff/{sid} | staff.update | owner SEUL |
| DELETE /salons/{id}/staff/{sid} | staff.remove | owner SEUL |
| GET /salons/{id}/staff/search | invitation.search | owner SEUL |
| POST /salons/{id}/invitations | invitation.create | owner SEUL |
| GET /salons/{id}/invitations | invitation.list | owner + manager |
| DELETE /salons/{id}/invitations/{iid} | invitation.cancel | owner SEUL |

#### Tests curl par role (tous PASS)
PRESTATIONS (service.create/update/delete/batch) :
| Role | create | update | delete | batch |
|------|--------|--------|--------|-------|
| owner | 201 | 200 | 204 | 201 |
| manager | 201 | 200 | 204 | 201 |
| hairstylist | 403 | 403 | 403 | 403 |
| no-staff | 403 | 403 | 403 | 403 |

EQUIPE (staff.add/update/remove) :
| Role | add | update | remove |
|------|-----|--------|--------|
| owner | (auth pass) | 200 | (skip preserve data) |
| manager | 403 | 403 | 403 |
| hairstylist | 403 | 403 | 403 |
| no-staff | 403 | 403 | 403 |

INVITATIONS (search/create/list/cancel) :
| Role | search | create | list | cancel |
|------|--------|--------|------|--------|
| owner | 200 | 404(auth pass) | 200 | 404(auth pass) |
| manager | 403 | 403 | 200 | 403 |
| hairstylist | 403 | 403 | 403 | 403 |
| no-staff | 403 | 403 | 403 | 403 |

#### Non-regression
- GET publics (lister prestations/equipe sans auth) : 200 OK
- Endpoints coiffeur (me/invitations, accept, decline) : inchanges, fonctionnels
- invitation.list ouvert au manager MAIS search/create/cancel restent owner-seul : CONFIRME

#### Bug preexistant note (hors scope)
- Table `services` (Flyway) a `price_min`/`price_max`, entite SalonService mappe `price` -> mismatch.
  L'entite utilise en fait `salon_services` (table distincte). La table `services` est orpheline.

Frontend NON touche. Backend BUILD SUCCESSFUL. PAS de commit.

### Fix UI pilules dashboard owner : placement + lisibilite (2026-06-18)

Probleme : barre de pilules de categories chevauchait l'en-tete, 1re pilule tronquee.

#### Cause
- Aucun espacement vertical entre topBar et pillsScroll (pas de marginTop)
- topBar paddingBottom=12 insuffisant, pillsContainer paddingVertical=8 trop serree
- pill sans alignItems/justifyContent ni lineHeight explicite -> texte potentiellement rogne

#### Corrections (owner-dashboard.tsx uniquement)
- topBar: paddingBottom 12->14
- pillsScroll: +marginTop:4 (separation nette sous le header)
- pillsContainer: paddingVertical 8->10, gap 8->10
- pill: +alignItems:'center', +justifyContent:'center' (centrage garanti)
- pillText: +lineHeight:20 (texte jamais rogne)
- Couleurs (deja correctes, confirmees) : actif=primary+onPrimary, inactif=surface+outlineVariant+onSurface

Metro relance avec --clear. PAS de commit.

### PERMISSIONS LOT 4 : Branchement domaines secondaires — DERNIER LOT BACKEND (2026-06-18)

Remplacement des checks owner en dur par requirePermission dans 6 services.

#### Fichiers modifies (backend uniquement)
- SalonService.kt : +injection, cover-photo check remplace
- PortfolioService.kt : +injection, +requirePortfolioMutationAccess(portfolio, userId, permission)
  qui branche salon->requirePermission ou coiffeur->ownerId==userId selon ownerType.
  create/update/manage_posts = owner+manager, delete = owner SEUL.
- QueueService.kt : +injection, callNextClient check remplace
- QueueController.kt : @PreAuthorize("hasRole('OWNER')") -> isAuthenticated() sur call-next
  (double barriere eliminee, permission fine dans le service)
- SocialService.kt : +injection, updateSalonSocialProfile check remplace
- VerificationService.kt : +injection, requestVerification cas "salon" remplace (cas "user" inchange)

#### Points delicats documentes
1. FILE D'ATTENTE (double barriere) : annotation @PreAuthorize("hasRole('OWNER')") sur controller
   BLOQUAIT tout non-owner AVANT le service. Remplacee par isAuthenticated(). Permission fine
   queue.call_next dans le service (owner+manager+coiffeur).
2. PORTFOLIO (salon vs perso) : requirePortfolioMutationAccess distingue ownerType :
   - salon -> salonAuthorizationService.requirePermission(userId, ownerId, permission)
   - coiffeur -> ownerId == userId (logique perso preservee, AUCUNE permission salon appliquee)
   hasAccessToPortfolio (lecture privee) : salon -> hasPermission("portfolio.update"), coiffeur -> ownerId==userId
3. VERIFICATION : requestVerification a 2 cas (when "user" / "salon"). Seul le cas "salon" est
   branche sur requirePermission("verification.request"). Le cas "user" reste inchange (user.id == currentUserId).

#### Tests curl par role (tous PASS)
| # | Endpoint | owner | mgr | hair | ns |
|---|----------|-------|-----|------|----|
| 1 | PUT cover-photo | 200 | 200 | 403 | 403 |
| 2 | PUT social profile | 200 | 200 | 403 | 403 |
| 3 | POST queue call-next | 500* | 500* | 500* | 403 |
| 4 | POST portfolio create (salon) | 201 | 201 | 403 | 403 |
| 5 | PUT portfolio update | 200 | 200 | 403 | 403 |
| 6 | DELETE portfolio (owner SEUL) | 200 | 403 | 403 | 403 |
| 7 | POST verification salon | 200 | 403 | 403 | 403 |
*500 = permission passee, erreur metier (pas de client en attente) — NS=403 confirme le blocage.

#### Non-regression
- Verification user (requestVerification cas "user") : 200 OK (pas casse)
- GET publics portfolios/social profile : 401 sans auth — BUG PREEXISTANT (pas dans permitAll
  SecurityConfig), PAS cause par ce lot
- Portfolio perso coiffeur : logique preservee (ownerId==userId)

TOUT le backend est maintenant couvert par le systeme de permissions.
Frontend NON touche. Backend BUILD SUCCESSFUL. PAS de commit.

### diagnostic lot 5 : contexte salon + permissions frontend (2026-06-18)

Points diagnostiques (A-F) : detection owner = user.userType === 'salon_owner' (profile.tsx:299),
salonId = etat local owner-dashboard (getSalonsByOwner[0], picker Alert si multi), passe en route
param aux sous-ecrans. Stores : authStore + preferencesStore (Zustand), ToastContext (React Context),
AUCUN store/contexte salon. API client.ts intercepteur Bearer+refresh. Endpoint my-permissions
confirme : { role: string, permissions: string[] } (SalonController:369). Types TS : StaffMember.role
= string, aucun type Permission/MyPermissions existant.

Decision architecture Lot 5 : OPTION A (hook usePermissions(salonId)) retenue.
- src/types/salon.ts : +MyPermissionsResponse
- src/api/salons.ts : +getMyPermissions(salonId)
- src/hooks/usePermissions.ts : CREER (hook ~30L, expose { role, permissions, can(key), isLoading })
- 0 i18n, 0 refactor de routes
Lot 6 consommera can() dans les ecrans owner existants.

Baselines inchangees : tsc=0, i18n 796 cles 0 ecart.

### PERMISSIONS LOT 5 : hook usePermissions + API getMyPermissions + type (2026-06-18)

Plomberie frontend seule (aucun ecran modifie, aucun masquage UI — Lot 6).

#### Fichiers crees/modifies
- src/types/salon.ts : +MyPermissionsResponse { role: string; permissions: string[] }
  (exporte via barrel src/types/index.ts, deja couvert par `export * from './salon'`)
- src/api/salons.ts : +getMyPermissions(salonId) — meme pattern que getSalonStaff
  (+import MyPermissionsResponse)
- src/hooks/usePermissions.ts : CREE (nouveau dossier src/hooks/)

#### Hook usePermissions(salonId: string | null | undefined)
- useState { role, permissions, isLoading, error }
- useEffect avec cleanup cancelled (anti setState apres unmount), refetch si salonId change
- salonId falsy -> role='none', permissions=[], isLoading=false (pas de fetch)
- Retourne { role, permissions, isLoading, error, can(key), isOwner }
- REGLE moindre privilege : can() retourne FALSE pendant chargement ET en cas d'erreur
- isLoading expose separement (l'ecran choisit loader vs masquage)
- Source de verite = backend (pas d'heuristique userType cote front)
- Pas de toast/erreur UI (log __DEV__ discret)

#### Verifications
- tsc --noEmit = 0 erreur
- check-keys.js = 796 cles, 0 ecart (aucune cle i18n ajoutee)
- curl my-permissions non re-teste (rate limit login) — contrat confirme par lecture code
  SalonController:369-385 + tests curl lots 1-4 documentes ci-dessus

Backend NON touche. Aucun ecran owner modifie. PAS de commit.

### PERMISSIONS LOT 6a : Gardes de route + composant AccessDenied partage (2026-06-18)

#### Composant AccessDenied
- src/components/common/AccessDenied.tsx : CREE. Props { message?, onBack? }. Icone shield-alert-outline
  dans cercle errorContainer, titre + message i18n, bouton Retour (router.back || replace /(tabs)).
  Exporte via barrel src/components/common/index.ts.
- Design : centre vertical, couleurs theme (useTheme), typo projet (Cormorant titre, Manrope body+btn).

#### i18n : +3 cles permissions.accessDenied.{title,message,back} dans 5 langues
- fr/en/es/de/ar. Total 799 cles (796+3), 0 ecart, parite 5 langues.

#### Gardes de route : 6 ecrans proteges
| Ecran | salonId source | Garde |
|-------|---------------|-------|
| owner-dashboard | activeSalonId (local, async getSalonsByOwner) | isLoadingSalons OR (activeSalonId AND permLoading) -> loader ; activeSalonId AND role=none -> AccessDenied ; salons.length=0 -> empty state existant (pas AccessDenied) |
| owner-services | useLocalSearchParams | permLoading -> loader ; role=none -> AccessDenied |
| owner-staff | useLocalSearchParams | permLoading -> loader ; role=none -> AccessDenied |
| owner-bookings | useLocalSearchParams | permLoading -> loader ; role=none -> AccessDenied |
| create-service | useLocalSearchParams | permLoading -> loader ; role=none -> AccessDenied |
| create-staff | useLocalSearchParams | permLoading -> loader ; role=none -> AccessDenied |

#### Regle des hooks verifiee : usePermissions appele AVANT tout return dans les 6 ecrans.
#### PIEGE evite : AccessDenied JAMAIS affiche pendant isLoading (loader d'abord, AccessDenied ensuite).
#### Masquage fin des actions (boutons/sections par permission) = Lot 6b (PAS fait ici).

#### Verifications
- tsc --noEmit = 0 erreur
- check-keys.js = 799 cles, 0 ecart
- Backend NON touche. PAS de commit.

### PERMISSIONS LOT 6b ecran 1 : masquage fin owner-services (2026-06-18)

Masquage COMPLET (elements caches, pas grises) base sur can().

#### Elements masques
| Element | Condition | Roles qui voient |
|---------|-----------|------------------|
| FAB « + » (ajouter prestation) | can('service.create') | owner, manager |
| CTA empty state « Ajouter ma premiere prestation » | can('service.create') | owner, manager |
| Bouton « ... » (menu par prestation) | canEdit OR canDelete | owner, manager |
| Action « Modifier » dans le menu | can('service.update') | owner, manager |
| Action « Supprimer » dans le menu | can('service.delete') | owner, manager |

#### Rendu par role
- OWNER / MANAGER : FAB visible, menu ... avec Modifier+Supprimer -> inchange vs avant.
- COIFFEUR / APPRENTI : liste lecture seule (pas de FAB, pas de menu ..., pas de CTA empty state).
  Les prestations s'affichent normalement, visuellement coherent.
- Menu vide = bouton « ... » cache (hasMenuActions = canEdit || canDelete).
- FAB = position absolute, masque sans impact layout.

#### Fichier modifie : owner-services.tsx uniquement.
- Destructure `can` depuis usePermissions (deja branche au 6a).
- canEdit/canDelete/hasMenuActions = variables derivees en haut du composant.
- openMenu construit les actions dynamiquement.

tsc=0, i18n=799 inchange. Backend NON touche. PAS de commit.

### PERMISSIONS LOT 6b ecran 2 : masquage fin owner-dashboard (2026-06-18)

| Element | Condition | Qui voit |
|---------|-----------|----------|
| Carte metrique « Revenus » | can('payment.view_salon') | owner SEUL |

- Spread conditionnel dans le tableau metrics (... can() ? [carte] : []).
- Grille metriques = flexWrap+flexGrow : a 3 cartes (sans Revenus), 2 en haut + 1 pleine largeur
  en bas. Layout propre, pas de trou.
- Autres cartes (Reservations, Note, Abonnes) + graphique + sections : inchanges, visibles pour
  tout role ayant acces.

tsc=0. Fichier : owner-dashboard.tsx uniquement. PAS de commit.

### PERMISSIONS LOT 6b ecran 3 : masquage fin owner-staff (2026-06-18)

| Element | Condition | Qui voit |
|---------|-----------|----------|
| FAB « Inviter un coiffeur » | can('invitation.create') | owner SEUL |
| CTA empty state « Inviter » | can('invitation.create') | owner SEUL |
| Section « Invitations en attente » (liste) | can('invitation.list') | owner + manager |
| Bouton « Annuler » par invitation | can('invitation.cancel') | owner SEUL |
| Bouton « ... » par membre (menu) | canEditStaff OR canRemoveStaff | owner SEUL |
| Action « Modifier » dans menu | can('staff.update') | owner SEUL |
| Action « Retirer » dans menu | can('staff.remove') + !isOwner + !isSelf | owner SEUL |

Fetch getSalonInvitations conditionne a canListInvitations (evite 403 pour coiffeur/apprenti).

#### Rendu par role
- OWNER : tout visible (FAB, invitations, menu ..., retrait) — inchange.
- MANAGER : voit la liste des invitations (sans boutons Annuler), voit les membres (sans menu ...).
  Pas de FAB. Liste lecture seule + section invitations en lecture seule.
- COIFFEUR / APPRENTI : liste des membres en lecture seule uniquement. Pas de section invitations,
  pas de FAB, pas de menu ...

tsc=0. Fichier : owner-staff.tsx uniquement. PAS de commit.

### PERMISSIONS LOT 6b ecran 4 : masquage fin owner-bookings (2026-06-18)

#### Diagnostic cas coiffeur
- getSalonBookings appelle GET /api/salons/{id}/bookings -> requirePermission("booking.view_all")
  (BookingService:202). booking.view_all = owner + manager UNIQUEMENT (V048:67).
- Un coiffeur qui entre sur owner-bookings voit un message d'erreur (403 catch -> booking.loadError).
- Decision : OPTION (a) retenue — restreindre l'ENTREE a can('booking.view_all'). Les RDV perso du
  coiffeur sont sur l'ecran client bookings.tsx, pas ici.

#### Elements masques / gardes renforcees
| Element | Condition | Qui voit |
|---------|-----------|----------|
| Garde d'entree renforcee | can('booking.view_all') | owner + manager (coiffeur/apprenti -> AccessDenied) |
| Bouton « Annuler » par reservation | can('booking.cancel') | owner + manager |

#### Rendu par role
- OWNER / MANAGER : tout visible (stats, filtres, liste, bouton Annuler) — inchange.
- COIFFEUR / APPRENTI : AccessDenied (pas d'acces a l'ecran, coherent avec le backend).

tsc=0, i18n=799 inchange. Fichier : owner-bookings.tsx uniquement. PAS de commit.

### L3 edition membre : dialog specialites+actif+role, backend role dans UpdateStaffRequest (2026-06-18)

Full-stack. Le menu « Modifier » d'un membre (owner-staff) ouvre desormais un dialog d'edition
au lieu de "comingSoon".

#### Backend
- SalonStaffDto.kt : UpdateStaffRequest +val role: String? = null, +ASSIGNABLE_ROLES = {manager,
  hairstylist, apprentice}, +validateRole() (IllegalArgumentException -> 400 si role invalide ou
  "owner"), applyTo() applique entity.role = it.lowercase().
- SalonStaffService.kt : +request.validateRole() avant applyTo() (etape 3 du updateStaff).
- Garde existante : requirePermission("staff.update") = owner seul (inchange, lot 3).
- Curl non teste (rate limit login) — resultats attendus par lecture code :
  PUT role=manager -> 200 ; role=owner -> 400 ; role=bidon -> 400.

#### Frontend
- UpdateStaffRequest TS : +role?: string.
- owner-staff.tsx : dialog Modal centre (pattern LogoutConfirmModal) contenant :
  * Picker de role (3 chips : manager/hairstylist/apprentice, libelles salon.roles.*)
  * Multi-select specialites (SERVICE_CATEGORY_META, icones + labels)
  * Toggle Switch actif/inactif
  * Boutons Annuler / Enregistrer
  Succes = toast + recharge liste (loadStaff). Erreur = toast avec message backend.
- « Modifier » NON propose sur le membre role==='owner' (en plus du masquage permission).
- i18n : +7 cles profile.ownerStaff.editDialog.{title,roleLabel,specialtiesLabel,activeLabel,
  save,saveSuccess,saveError} dans 5 langues (parite). Total 806 cles, 0 ecart.

#### Verifications
- backend classes BUILD SUCCESSFUL
- tsc --noEmit = 0 erreur
- check-keys = 806 cles (+7), 0 ecart, parite 5 langues
- Backend NON commite. Frontend NON commite. PAS de commit.

### Nettoyage owner-services : catch suppression -> toast, residus debug retires
- **catch muet** dans `handleDeleteConfirm` remplace par `showToast(message, 'error')` (pattern owner-staff)
- **0 autre catch muet** dans le fichier (loadServices a setError -> OK)
- **0 console.log de debug** a retirer (seul console.error dans loadServices = legitime)
- **0 cle i18n ajoutee** (message d'erreur backend ou JS natif)
- tsc --noEmit = 0, diff = owner-services.tsx seul. NON commite.

### Diagnostic L5 : edition infos salon (PURE LECTURE)
- Endpoint PUT/PATCH salon info : ABSENT -> FULL-STACK
- Permission salon.update_info : seedee V048, owner+manager
- Entite Salon.kt : 10 colonnes DB non mappees (country, phone_number, email, website_url,
  instagram_handle, facebook_page, opening_hours, is_accepting_walk_ins, is_premium, subscription_plan)
- Geocoding : AUCUN mecanisme dans le code ; lat/lng nullable, saisie manuelle a la creation (ou null)
- Ecran patron : create-salon.tsx (name, address, city, postalCode, description, coverPhoto)
- salonsApi.updateSalon : N'EXISTE PAS cote front
- UpdateSalonRequest DTO : N'EXISTE PAS cote backend
- Champs L5 candidats SIMPLES : name, address, city, postalCode, description, phone_number, email, website_url
- Champs L5 EXCLUS (geres ailleurs) : coverPhotoUrl (update_cover), socialDescription (L6), opening_hours (L9)
- Champ SENSIBLE : address/city/postalCode -> pas de geocoding auto, lat/lng resterait inchange (doc user)
- Voir detail complet dans la reponse diagnostic ci-dessous.

### L5 : edition infos salon (8 champs, full-stack, mapping phone/email/website)
- **Backend** : Salon.kt +3 colonnes mappees (phoneNumber, email, websiteUrl), SalonResponse +3 champs,
  UpdateSalonRequest DTO (8 champs, @NotBlank name/address/city/postalCode, @Email, @URL),
  SalonService.updateSalonInfo (requirePermission salon.update_info), SalonController PUT /{salonId}
- **Frontend** : Salon TS +3 champs, UpdateSalonRequest type, salonsApi.updateSalon, edit-salon.tsx
  (8 champs, 3 sections identite/localisation/contact, toast succes/erreur, garde usePermissions +
  can('salon.update_info') + AccessDenied), tuile "Infos salon" dans owner-dashboard masquee par
  can('salon.update_info')
- **i18n** : +10 cles salon.edit.* + profile.ownerDashboard.tiles.editSalon (5 langues, parite 816/840)
- **Curl** : owner PUT 200, non-staff 403, unauth 401, validation name blank 400, email invalid 400,
  GET non-regression OK (nouveaux champs presents)
- tsc --noEmit = 0, check-keys 0 ecart. NON commite.

### edit-salon : photo de couverture centralisee (ajout edit-salon, retrait du detail)
- **edit-salon.tsx** : section photo en haut (ImagePicker + mediaApi.uploadImage + updateSalonCoverPhoto),
  enregistrement IMMEDIAT a la selection (endpoint cover separe), toast succes/erreur, spinner pendant upload.
  La photo existante est affichee via resolveMediaUrl, placeholder si absente.
- **salon/[id].tsx** (page publique) : retrait du bouton camera owner (handlePickCover, isUploadingCover,
  coverEditBadge, imports ImagePicker + mediaApi). Le cover est desormais un View non interactif.
  isOwner conserve pour le floating action bar (reservations/queue owner).
- **i18n** : +2 cles salon.edit.coverLabel + coverSuccess (5 langues, parite 818/842)
- tsc --noEmit = 0, check-keys 0 ecart. NON commite.

### Diagnostic social approfondi : contexte de publication salon/coiffeur
- Colonne cle : posts.author_type ENUM('salon','staff','user') existe en base (V001) mais NON MAPPEE
  dans Post.kt. L'entite n'a que author (User ManyToOne) -> tout post est « de l'utilisateur ».
- getPostsBySalon = posts TAGUES avec le salon (PostTag), PAS posts PUBLIES PAR le salon.
- CreatePostRequest n'a PAS de champ authorType/salonId/postAs -> pas de selecteur de contexte.
- PostCard affiche authorUserType (salon_owner/hairstylist) = type du USER, pas du contexte de publi.
- Tags salon = seul l'auteur du post peut taguer un salon (addTag verifie post.author == userId).
- Verdict : la vision 3 modes NECESSITE d'evoluer le modele (mapper author_type, ajouter salonId
  optionnel sur Post, modifier CreatePostRequest, adapter PostResponse/PostCard).
- Voir rapport complet axes 1-6 + verdict + questions a trancher dans la reponse diagnostic.

### Lot A social : edition profil social du salon (socialDescription + socialCoverImage)
- **Backend** : NON touche (endpoint PUT /api/social/salons/{salonId}/profile existe, DTO
  UpdateSalonSocialProfileRequest = socialDescription + socialCoverImage + highlightedPostIds)
- **Types TS corriges** : SalonSocialProfileResponse alignee sur le JSON reel (socialDescription,
  socialCoverImage, address, postalCode, slug, isOwner — supprime salonId fictif, description
  renomme en socialDescription). UpdateSalonSocialProfileRequest = socialDescription + socialCoverImage.
- **Ecran profile/salon/[id].tsx** : corrige description -> socialDescription, salonId -> id ;
  ajoute bouton "Modifier le profil" masque par can('social.update_profile') (usePermissions).
- **Ecran edit-salon-social.tsx** (nouveau) : image couverture sociale (ImagePicker + mediaApi) +
  description sociale (multiligne, max 2000) + Enregistrer -> PUT profile -> toast. Garde
  can('social.update_profile') + AccessDenied. Image uploade et URL stockee, tout part au save.
- **i18n** : +8 cles salon.social.* (5 langues, parite 826/850)
- tsc --noEmit = 0, check-keys 0 ecart. NON commite.

### LOT A : AVIS-SALON LIBRE (2026-06-20)

Diagnostic : le MODELE reviews.booking_id est DEJA NULLABLE (base + entite). 8 avis seed ont
booking_id=NULL. Le flux createReview bloquait l'avis-salon (6 checks + bookingId obligatoire).
Decision humain : NOTES SEPAREES (verifiee vs generale) + anti-abus 1/user/salon.

Construction :
- **Backend** : CreateSalonReviewRequest DTO + ReviewRepository (+existsBySalonIdAndClientIdAndBookingIsNull
  anti-abus + 4 queries stats verified/general) + ReviewService.createSalonReview (anti-abus +
  owner interdit + isVerified=false, booking=null) + SalonReviewStats DTO enrichi
  (verifiedAverage/Count, generalAverage/Count backward-compatible) + endpoint POST
  /api/salons/{salonId}/reviews @PreAuthorize("isAuthenticated()"). Flux avis-reservation INCHANGE.
- **curl** : 201 isVerified=false | doublon 400 | stats verified=4.0(1) general=4.33(9) | non-regression OK
- **Frontend** : CreateSalonReviewRequest TS + reviewsApi.createSalonReview + create-review.tsx
  bookingId optionnel (mode salon = toast, pas Alert) + salon/[id].tsx onglet Avis (stats 2 sous-lignes
  + bouton "Donner mon avis" connecte non-owner sans avis existant + badge "Visite" isVerified)
- **i18n** : +5 cles review.* x 5 langues. Parite 852 FR, 0 ecart.
- tsc=0, check-keys=0 ecart. 15 fichiers. NON commite.

### LOT B : ECRAN OWNER-REVIEWS (2026-06-20) — DERNIER LOT CHANTIER AVIS

Construction (frontend seul — endpoints backend existants) :
- **owner-reviews.tsx** (nouveau) : ecran dedie consulter/repondre aux avis depuis le dashboard.
  Garde permission usePermissions(salonId) -> AccessDenied si role=none. useFocusEffect pour
  charger getAllSalonReviews + getSalonReviewStats. Bandeau stats (moyenne + total + non-repondus en
  rouge). Filtre chips (Sans reponse | Repondus | Tous), defaut = Sans reponse (priorite owner).
  Liste : ReviewCard avec avatar, nom, note etoiles, date, badge "Visite" (isVerified), contenu,
  bloc reponse existante OU bouton Repondre (si canReply). Modal reply (meme pattern que
  salon/[id].tsx) -> reviewsApi.replyToReview -> toast + refresh liste. Etats loading/erreur/vide.
- **owner-dashboard.tsx** : tuile "Avis" activee (route=/owner-reviews, routeParams salonId).
  Plus de placeholder/opacite 50%/comingSoon.
- **i18n** : +6 cles ownerReviews.* x 5 langues (totalLabel, unrepliedLabel, filterAll,
  filterUnreplied, filterReplied, noUnreplied). Parite 858 FR, 0 ecart.
- tsc=0, check-keys=0 ecart. 18 fichiers (dont 1 nouveau). NON commite.

### DIAGNOSTIC HORAIRES + SYSTEME RESERVATION (2026-06-20)

AXE 1 — HORAIRES (opening_hours) :
- **Colonne** : `opening_hours JSON NULL` EXISTE dans la table salons (migration DDL existante).
  Valeur actuelle = NULL (aucune donnee).
- **Entite Salon.kt** : la colonne N'EST PAS MAPPEE (aucun champ openingHours). A ajouter.
- **Affichage** : aucun affichage horaires dans salon/[id].tsx (onglet info = adresse + description
  seulement). La tuile "Horaires" du dashboard owner = placeholder comingSoon (L311, pas de route).

AXE 2 — SYSTEME DE RESERVATION :
- **Entite Booking** : porte bookingDatetime (timestamp precis) + durationMinutes (depuis le service).
  Methode getEndDatetime() = start + duration. PAS de colonne end_time en base (calcule).
- **SalonService** : porte durationMinutes (default 30 min). La duree est copiee dans le booking
  a la creation (L113 BookingService).
- **CREATION createBooking** (BookingService.kt:61-154) — validations EXISTANTES :
  1. Salon existe (L65)
  2. Client existe + type client (L69-74)
  3. Service existe + appartient au salon (L77-83)
  4. Staff optionnel : existe, actif, meme salon, peut faire le service (L86-105)
  5. Date dans le futur (L108-110)
  6. Duree depuis le service (L113)
  7a. **CHEVAUCHEMENT CLIENT** : hasClientConflict query (L119-126) — empeche le meme client d'avoir
     2 reservations au meme moment
  7b. **CHEVAUCHEMENT STAFF** : hasStaffConflict query (L129-138) — si un coiffeur est specifie,
     verifie qu'il n'a pas de reservation qui chevauche
  >>> PAS de validation horaires : aucune verification que le creneau tombe dans les horaires
     d'ouverture du salon. Un client peut reserver a 3h du matin si le creneau est libre. <<<
- **SYSTEME DE CRENEAUX** (BookingService.kt:417-494) : getAvailableSlots EXISTE et genere des slots.
  MAIS les plages horaires sont HARDCODEES :
    openingTime = 9h, closingTime = 19h, slotInterval = 30min (L448-450).
  La generation : boucle de 9h a 19h par pas de 30min, pour chaque coiffeur eligible, verifie
  hasStaffConflict pour chaque slot. Filtre les slots passes + indisponibles.
  >>> C'est LE point de branchement : remplacer le hardcode 9h-19h par les opening_hours du salon
     = contrainte LEGERE si les horaires sont stockes dans un format exploitable. <<<
- **Cote app** (booking/new.tsx) : le client voit une grille de creneaux proposes par le backend
  (L102-109 : bookingsApi.getAvailableSlots). Il choisit un slot -> le datetime est envoye au
  backend. Le client ne saisit PAS d'heure libre — il choisit parmi les creneaux proposes.
- **Horaires SALON vs COIFFEUR** : aujourd'hui, les creneaux sont generes PAR COIFFEUR (boucle
  sur staffList L455), mais la plage horaire (9h-19h) est au niveau SALON (globale, hardcodee).
  Un coiffeur n'a pas d'horaires propres. La pause dejeuner n'est PAS geree (continu 9h-19h).
- **Bookings en base** : 2 reservations (1 pending, 1 completed). Impact migration negligeable.

VERDICTS :
1. **La contrainte horaires est un AJOUT LEGER** : le systeme de creneaux existe deja, la
   generation de slots est hardcodee 9h-19h. Il suffit de remplacer par les opening_hours du salon
   (qui supportent multi-plages par jour = pause dejeuner). La validation a la creation peut
   verifier que le creneau tombe dans une plage ouverte. Le client choisit parmi les creneaux
   proposes (pas de saisie libre) -> la contrainte est NATURELLE.
2. **Pas besoin de 2 lots** : horaires informatifs + contrainte = UN SEUL lot. L'effort est :
   - Backend : mapper opening_hours dans Salon.kt, CRUD endpoints, remplacer le hardcode 9h-19h
     dans getAvailableSlots par lecture des plages, validation dans createBooking.
   - Frontend : ecran edition horaires owner (multi-plages/jour, jour ferme), affichage page salon,
     activer la tuile dashboard.
3. **Horaires au niveau SALON** (pas par coiffeur) — coherent avec l'existant. Les horaires par
   coiffeur seraient un chantier a part entiere (planning individuel, tables supplementaires) et
   ne sont pas necessaires pour le MVP.
4. **Format opening_hours propose** : JSON type { "monday": [{"open":"09:00","close":"12:00"},
   {"open":"14:00","close":"19:00"}], "tuesday": [...], ... "sunday": null }. Multi-plages par
   jour (pause dejeuner). Jour null ou absent = ferme.

### HORAIRES LOT 1 : FONDATION + RECURRENTS (2026-06-20)

Construction :
- **Migration V051** : ADD COLUMN timezone VARCHAR(64) NOT NULL DEFAULT 'Africa/Libreville' sur salons.
  (opening_hours JSON existait deja.)
- **Salon.kt** : +openingHours (@JdbcTypeCode JSON, Map<String, List<Map<String,String>>>?) +timezone.
- **OpeningHoursDto.kt** (nouveau) : TimeRange{open,close}, UpdateOpeningHoursRequest (openingHours,
  timezone), OpeningHoursResponse. Validation stricte : HH:mm, close>open, pas de chevauchement,
  jours valides, timezone IANA.
- **SalonService** : +updateOpeningHours (requirePermission salon.update_info, validation, conversion
  DTO->entite, retourne OpeningHoursResponse).
- **SalonController** : PUT /api/salons/{salonId}/opening-hours + GET salon expose openingHours+timezone.
  Fix SalonController.getSalonById qui construisait SalonResponse manuellement sans les nouveaux champs.
- **SalonResponse** : +openingHours, +timezone.
- **curl** : PUT valide 200 (multi-plages, dimanche ferme) | GET expose openingHours+timezone |
  close<open 400 | overlap 400 | non-membre 403.
- **Frontend** :
  - Types TS : TimeRange, OpeningHours, Salon +openingHours/timezone, UpdateOpeningHoursRequest.
  - salonsApi.updateOpeningHours(salonId, data).
  - edit-opening-hours.tsx (nouveau) : 7 jours, toggle ouvert/ferme, multi-plages par jour (inputs
    HH:mm), bouton +ajouter plage, copie rapide "appliquer a tous" via Modal, validation UI, toast.
    Garde can('salon.update_info') + AccessDenied.
  - salon/[id].tsx onglet info : affichage horaires semaine (ou "Horaires non renseignes" si null).
  - owner-dashboard.tsx : tuile "Horaires" activee (route=/edit-opening-hours, routeParams salonId).
  - i18n : +19 cles openingHours.* (title, open, closed, notSet, addRange, applyToAll, copyFrom,
    appliedToAll, saveSuccess, 7 jours, 3 validations) x 5 langues. Parite 877 FR, 0 ecart.
- NE TOUCHE PAS getAvailableSlots ni createBooking (Lot 2).
- tsc=0, check-keys=0 ecart. 14 fichiers modifies + 3 nouveaux. NON commite.
- **Fix UI** : champs heure de droite debordaient hors carte. Cause : pas de minWidth:0 sur les
  inputs flex:1 (RN ne retrecit pas en dessous du contenu intrinseque) + rangeSep sans flexShrink:0.
  Fix : timeInput +minWidth:0 ; rangeSep +flexShrink:0 +marginHorizontal:2. Tient pour 1 et 2 plages.

### HORAIRES LOT 2 : CONTRAINTE RESERVATION (2026-06-20)

Construction (backend surtout — BookingService.kt) :
- **resolveOpeningRangesForDate(salon, date)** : fonction de resolution centrale. Lot 2 = recurrent
  seulement (jour de la semaine -> plages openingHours). Point d'extension documente pour Lot 3
  (exceptions datees : "if salon has exception for date -> return exception ranges"). Retourne
  List<Pair<LocalTime, LocalTime>> triees. Salon sans horaires = liste vide = ferme.
- **isWithinOpeningHours(ranges, start, durationMinutes)** : verifie qu'un creneau (start + duration)
  tient entierement dans une plage ouverte (end <= close).
- **getAvailableSlots** : remplace le hardcode 9h-19h par resolveOpeningRangesForDate. Generation des
  creneaux DANS chaque plage (saute les pauses). Un creneau n'est propose que si start + duration <=
  close de la plage. Conserve slotInterval=30, hasStaffConflict par coiffeur, filtre passes.
  Fermé/non renseigné = retour vide immediat.
- **createBooking** : +validation step 6b apres calcul duree, avant checks conflit. Appelle
  isWithinOpeningHours -> si hors plage, rejet 400 "Le salon est ferme a cet horaire. La prestation
  (N min) doit tenir entierement dans une plage d'ouverture."
- **curl prouve** :
  - getAvailableSlots LUNDI (09-12 + 14-19) : 16 creneaux 30min (09:00-11:30 + 14:00-18:30),
    RIEN entre 12h-14h (pause respectee). Service 60min : dernier matin = 11:00 (11:30 absent).
  - getAvailableSlots DIMANCHE (ferme) : 0 creneau.
  - createBooking lundi 13:00 (pause) : 400 "salon ferme".
  - createBooking dimanche : 400 "salon ferme".
  - createBooking 11:30 + 60min (deborde 12h) : 400 "prestation doit tenir dans plage".
  - NOTE : createBooking DANS plage retourne 500 (bug pre-existant dans BookingResponse.fromEntity,
    hors perimetre de ce lot — la validation horaires a PASSE, le rejet serait 400 sinon).
- **Frontend** : PAS de changement necessaire. booking/new.tsx affiche deja "Aucun creneau disponible
  pour cette date" quand la liste est vide (L360). Message adequat pour jours fermes.
- **i18n** : 0 nouvelles cles (message existant booking.noSlots suffit).
- tsc=0, check-keys=0 ecart (877 FR). BUILD SUCCESSFUL. NON commite.

### FIX STAFF GENERALISTES (2026-06-20)

Bug : `findBySalonIdAndSpecialty` (SalonStaffRepository.kt:53-58) faisait un `JOIN s.specialties sp`
qui excluait les staff SANS specialites (salon_staff_specialties = 0 lignes). Or
`canPerformService()` traite correctement le cas : `specialties.isEmpty() -> true` (generaliste).
Incoherence query/code = 0 creneaux pour tout le monde.
Fix : `LEFT JOIN s.specialties sp ... AND (sp = :specialty OR s.specialties IS EMPTY)` — inclut
les staff qui matchent la specialite OU qui n'ont aucune specialite (generalistes, peuvent tout
faire). Coherent avec `canPerformService`. Impact : getAvailableSlots + getStaffBySpecialty.
Prouve curl : 96 creneaux (6 staff generalistes x 16 slots/staff) sur un lundi, 0 dimanche (ferme).

### Fix available-slots : date LocalDate (2026-06-20)
Cause prouvee du 500 systematique depuis l'app : mismatch format date. L'app (booking/new.tsx:101)
envoie `date:"2026-06-29"` (LocalDate via toISOString().split('T')[0]), le DTO backend
(AvailableSlotsRequest.date) attendait LocalDateTime. Jackson echouait a deserialiser AVANT le
controller -> HttpMessageNotReadableException -> 500 global (le try-catch debug dans le controller ne
capturait rien). Le bug affectait TOUS les appels (avec ou sans staffId) — l'hypothese initiale
lazy-loading etait invalidee.
Fix applique :
1. BookingDto.kt : AvailableSlotsRequest.date LocalDateTime -> LocalDate ; AvailableSlotsResponse.date
   idem (import java.time.LocalDate ajoute)
2. BookingService.kt : request.date.toLocalDate() -> request.date (deja LocalDate)
3. BookingController.kt : try-catch debug retire (ResponseEntity<Any> -> ResponseEntity<AvailableSlotsResponse>)
4. SalonStaffRepository.kt : findBySalonIdAndSpecialty supprimee (requete native, 0 appelant confirme,
   import ServiceCategory retire) — les appelants utilisent findBySalonId + filtre Kotlin
   (canPerformService, generalistes inclus, owner exclu = fix legitime conserve)
Verification curl (4 appels) :
- date "2026-06-29" AVEC staffId Darla -> 200 (16 creneaux) [ETAIT 500]
- date "2026-06-29" SANS staffId -> 200 (96 creneaux, 6 staffs)
- dimanche "2026-06-28" -> 200 + 0 creneau (ferme, pas 500)
- GET /staff/specialties/COUPE -> 200 (6 staffs, owner exclu, generalistes inclus)
Build backend OK, tsc --noEmit OK. Non commite.

### Diagnostic ecran stats owner (2026-06-20) — preparation L8
AXE 1 — ECRAN ACTUEL (owner-dashboard.tsx, 770L) : TOUT est branche sur de VRAIES donnees.
  - totalBookings : API GET /bookings/statistics -> bookingStats.totalBookings (REEL, 4 en base)
  - revenue : bookingStats.revenue -> MISMATCH NOM : backend renvoie `totalRevenue`, TS lit `revenue`
    -> toujours undefined -> affiche "—". De plus totalRevenue=0 car filtrage = COMPLETED+PAID (1 seule
    resa completed mais payment_status=unpaid).
  - rating : API GET /reviews/stats -> REEL (mais 0 reviews)
  - followers : API GET /salons/{id} -> followersCount -> REEL
  - graphe evolution : API GET /bookings/daily -> REEL mais tous les jours = count:0 car
    findBySalonAndDatetimeRange exclut cancelled/no_show ET les 4 bookings sont tous FUTURS (>= 24 juin)
    -> hors de la fenetre 28j passee (24 mai - 20 juin) -> 0 partout.
  - pending bookings : API GET /salons/{id}/bookings -> filtre local status=PENDING -> REEL (3 pending)
  - top services : meme liste bookings -> filtre CONFIRMED+IN_PROGRESS+COMPLETED -> 1 seule completed
    -> montre 1 service. REEL.
AXE 2 — ENDPOINTS STATS BACKEND : 3 existent.
  (a) GET /bookings/statistics : total, pending, confirmed, completed, cancelled, totalRevenue (COMPLETED
      +PAID), averagePrice (COMPLETED). Fonctionne, prouve curl.
  (b) GET /bookings/daily?from=&to= : serie temporelle count+revenue par jour. Exclut cancelled/no_show.
      Revenue = COMPLETED+PAID seulement. Fonctionne, prouve curl.
  (c) GET /salons/{id}/bookings : liste complete (toutes les resas). Fonctionne.
AXE 3 — POURQUOI LA RESA N'APPARAIT PAS DANS LES STATS :
  C'est NORMAL (pas un bug) pour 3 raisons cumulees :
  (i) Revenue totalRevenue = somme des COMPLETED + PAID. La resa recente est status=pending,
      payment_status=unpaid -> exclue du CA. Correct metier.
  (ii) Graphe daily : fenetre = 28 jours PASSES. Les 4 resas ont booking_datetime >= 24 juin (futur)
       -> aucune dans la fenetre -> count=0 chaque jour. Correct si on montre l'historique.
  (iii) MISMATCH NOM DE CHAMP : backend envoie `totalRevenue`, le type TS BookingStatistics attend
        `revenue` -> la valeur n'arrive jamais cote app. C'est un BUG (mineur, car la valeur serait
        0 de toute facon avec les donnees actuelles, mais bloquant quand il y aura du vrai CA).
  De plus le type TS manque pendingBookings et confirmedBookings (presents dans le JSON backend).
AXE 4 — MATIERE DISPONIBLE POUR TABLEAU DE BORD L8 :
  - REVENUS : calculable (priceFinal sur bookings COMPLETED+PAID). Matiere en base, endpoint existe.
  - RESERVATIONS par statut : calculable (countBySalonIdAndStatus). Endpoint existe.
  - TOP SERVICES : calculable cote app (deja fait, filtre local sur allBookings). Pas d'endpoint dedie
    mais faisable en agrégeant la liste. Un endpoint backend GROUP BY service_id serait plus propre pour
    de gros volumes.
  - TENDANCES : serie daily existe deja (bookings/daily). Manque : ventilation par statut dans la serie,
    comparaison N-1 (semaine precedente), tendance en pourcentage.
VERDICT D'AMPLEUR L8 :
  - Backend : TRES PEU a creer. Les 3 endpoints existent. A faire : (a) fix mismatch nom de champ
    totalRevenue vs revenue (cote TS ou cote backend), (b) optionnel : endpoint top-services agrege,
    (c) optionnel : ventilation daily par statut.
  - Frontend : C'EST LE GROS DU TRAVAIL. L'ecran existe et est fonctionnel mais pas premium. A faire :
    refonte visuelle complete (design editorial, graphes plus riches, KPIs visuels, animations),
    correction du mismatch de champs TS, ajout de la fenetre « a venir » en plus de l'historique.
  -> L8 = 80% FRONTEND premium + 20% ajustements backend (mismatch champs + endpoints optionnels).

### L8 : Tableau de bord owner premium (2026-06-20)
Fix donnees : BookingStatistics TS aligne sur JSON backend (totalRevenue au lieu de revenue,
pendingBookings + confirmedBookings + salonId + averagePrice ajoutes). Revenue affiche correctement
(meme si 0 car aucun booking COMPLETED+PAID).
Refonte owner-dashboard.tsx (770L -> ~460L utiles, architecture plus propre) :
  - KPIs (4 cartes) : reservations, CA (totalRevenue, FCFA, garde payment.view_salon), note moyenne,
    a venir (nb bookings futurs). Chaque carte avec icone dans cercle tinte accent.
  - Selecteur periode : 7j / 30j / Annee (chips). Pilote le graphe. Fenetre = passe + futur.
  - Graphe SVG (react-native-svg, pas de lib externe) : courbe lissee passe (trait plein prune +
    gradient) vs futur (pointille champagne + gradient dore) separes par ligne verticale "Auj.",
    legende "Realise / A venir". Repond a la frustration "ma resa n'apparait pas" -> les resas futures
    sont visibles dans la partie doree du graphe.
  - Repartition par statut : barres horizontales (pending/confirmed/completed/cancelled) avec compteurs.
  - Reservations en attente : cartes avec actions confirmer/refuser (inchange fonctionnellement).
  - Prochains rendez-vous : 5 prochains bookings futurs, dot couleur statut, service + client + date.
  - Top services : classement numerote + barre + pourcentage.
  - Grille gestion (tuiles) : inchangee.
Design : Cormorant Garamond titres, Manrope UI, sections en cartes surface arrondies r16, tokens
couleur du design system, KPI avec cercle d'icone tinte.
i18n : 13 nouvelles cles ajoutees aux 5 langues (fr/en/es/de/ar) — parite. Cles : metrics.upcoming,
chart.todayLabel/past/upcoming/year, statusTitle, status.{pending,confirmed,completed,cancelled},
upcomingTitle, upcomingEmpty.
tsc --noEmit = 0. Non commite.

### Diagnostic parcours COIFFEUR (2026-06-20)
AXE 1 — CE QUE VOIT LE COIFFEUR AUJOURD'HUI :
  Aucun ecran dedie « dashboard coiffeur » ou « agenda ». Aucun fichier staff-dashboard, my-schedule,
  my-bookings dans app/. Le hairstylist voit le meme onglet Profil que les autres users, avec des
  ajouts conditionnels : invitations, specialites, experience, certifications (profile.tsx:304-351).
  Le menu profil propose : Invitations, Favoris, Archives, Collections, Portfolios, Parametres.
  PAS de lien vers « Mes RDV » ou « Mon agenda ».
AXE 2 — BACKEND :
  (2a) L'endpoint existe : GET /api/staff/{staffId}/bookings (BookingController:251). Le service
  getBookingsByStaff (BookingService:264) verifie isSelf || isOwner, retourne les bookings tries par
  datetime ASC. Fonctionne (prouve curl : Darla a 2 RDV pending). MAIS il manque cote app : aucun appel
  dans src/api/bookings.ts vers cet endpoint.
  (2b) Transitions de statut : updateBookingStatus (PATCH /api/bookings/{id}/status) avec scope OWN pour
  hairstylist — ne peut modifier QUE les bookings ou staff.user.id == userId. Permissions :
  booking.manage_status + booking.cancel. Le hairstylist PEUT confirmer/completer/annuler SES RDV.
AXE 3 — PERMISSIONS HAIRSTYLIST (5 en base) :
  booking.view_own, booking.manage_status, booking.cancel, queue.call_next, social.post_as_salon.
  NE PEUT PAS : voir TOUS les RDV du salon (booking.view_all = owner/manager only), voir les stats
  (booking.view_statistics = owner/manager), voir le CA (payment.view_salon = owner only), modifier
  les services ou le salon.
AXE 4 — PROFIL PRO :
  CoiffeurProfileResponse : bio, specialties (user_specialties table, vocabulaire libre), yearsExperience,
  certifications, instagramHandle, portfolios, badges, recentPosts. Gere depuis le profil (profile.tsx).
  Les specialites salon_staff_specialties (ServiceCategory enum) = categories de services reservables
  (canPerformService), gerees par l'owner via owner-staff. Sont DISTINCTES des specialites profil
  (vocabulaire libre).
AXE 5 — DONNEES DE TEST :
  Darla (dhshopentreprise@gmail.com, staffId a2d99c45): 2 bookings pending (24/06 + 29/06), avatar OK.
  Read Documents (readdocuments64@gmail.com, staffId bd70fa3d): 0 booking assigne, avatar OK.
  Test Coiffeur (staff-hair-001): 1 booking completed.
  Les 3 vrais comptes ont des refresh tokens valides, pas de 2FA.
PROPOSITION DE DECOUPAGE :
  LOT C1 — Ecran « Mon agenda » (frontend pur, ~2h) :
    - Nouvel ecran app/my-schedule.tsx (ou staff-bookings.tsx)
    - API layer : ajouter getStaffBookings(staffId) dans bookings.ts
    - Le coiffeur doit d'abord trouver son staffId (via findBySalonIdAndUserId depuis ses salons)
    - Affiche SES RDV : liste filtrable par statut, vue jour/semaine calendrier
    - Actions : confirmer, completer, annuler (SES RDV uniquement, scope OWN)
    - Lien depuis le menu profil hairstylist
    - i18n 5 langues
    Backend : 0 — l'endpoint GET /staff/{staffId}/bookings EXISTE et marche.
  LOT C2 — Dashboard coiffeur (frontend, ~3h) :
    - Ecran app/staff-dashboard.tsx : KPIs (RDV aujourd'hui, cette semaine, a venir, completes)
    - Vue journee (timeline/agenda visuel)
    - Notifications de nouveaux RDV assignes
    - Lien depuis le menu profil hairstylist (remplace ou complete le lien agenda)
    Backend : un endpoint « mes KPIs staff » serait utile mais calculable cote app.
  LOT C3 SUPPRIME — mono-salon confirme (garanti par InvitationService, un coiffeur = un seul salon). Code multi-salon nettoye.
  LOT C4 — Gestion des specialites staff par le coiffeur lui-meme (~1h) :
    - Aujourd'hui seul l'owner gere les specialites salon_staff_specialties
    - Permettre au coiffeur de declarer ses categories de services
    Backend : endpoint PUT /staff/{staffId}/specialties (permission booking.view_own ?).
  VERDICT : LOT C1 est le MINIMUM VITAL (l'endpoint backend existe, c'est du pur frontend).
  C2 est le premium. C3 et C4 sont des polissages.

  ### LOT C1 — FAIT (2026-06-21)
  Coiffeur C1 : ecran Mon agenda (RDV assignes, resolution staffId multi-salon, actions scope OWN).
  - Resolution staffId CENTRALISEE et MULTI-SALON : hook useMyStaffMemberships (getMySalons + getSalonStaff, filtre userId+isActive, retourne [{salonId,salonName,staffId}]).
  - API : getStaffBookings(staffId) dans bookings.ts (GET /staff/{staffId}/bookings).
  - Ecran app/my-schedule.tsx : premium (Cormorant titres, Manrope UI, tokens design system).
    Sections : header + banner salon (sélecteur cycle si multi) + filtres pills (tous/a venir/en attente/confirmes/realises) + RDV groupes par jour (Aujourd'hui/Demain/dates, passes estompes) + cartes (avatar client, nom, prestation, heure debut-fin, duree, prix, badge statut couleur semantique, notes client).
    Actions scope OWN : confirmer (pending->confirmed), terminer (confirmed->completed), annuler (->cancelled avec motif via Modal). Toast succes/erreur.
    Etats : loading, vide, erreur retry, pas de rattachement.
  - Navigation : entree "Mon agenda" (icone calendar-today) dans le menu profil hairstylist (profile.tsx).
  - i18n 5 langues (25 cles x 5 = 889->914 cles FR, parite 0 ecart).
  - Backend : 0 touche. Endpoint GET /staff/{staffId}/bookings existant, confirme par SQL (Darla 2 RDV pending).
  - tsc --noEmit = 0, check-keys = 0 ecart.

  ### LOT C2+ — FAIT (2026-06-21)
  Coiffeur C2+ FINI : donut prestations + barres charge/jour ; code mort multi-salon nettoye ; C3 caduc (mono-salon).
  - Donut SVG (react-native-svg) « Repartition de mes prestations » : remplace la liste top services.
    groupBy serviceName -> count -> %. Top 4 segments + Autres. Trou central (total). Legende sous le donut.
    Degradation : 0 RDV = section masquee ; 1 seul type = Circle plein lisible.
  - Barres SVG « Ma charge par jour » : 7 barres (lun->dim), jour(s) max mis en evidence (tertiary).
    Degradation : 0 RDV = section masquee.
  - Code mort multi-salon nettoye : selecteur cycle + chevron retire de staff-dashboard.tsx ET my-schedule.tsx.
    Affichage du salon unique conserve (icone + nom). Hook useMyStaffMemberships INTACT (liste, consomme [0]).
  - C3 acte caduc dans le plan (mono-salon garanti par InvitationService).
  - i18n : 10 cles charts.* ajoutees (5 langues, parite).
  - tsc --noEmit = 0, check-keys = 0 ecart.

  ### DIAGNOSTIC C4 SPECIALITES COIFFEUR (2026-06-21, pure lecture)
  Diagnostic exhaustif : structure, logique metier, impact reservations, droits, UI, plan.
  AXE 1 — DEUX NOTIONS : salon_staff_specialties (ServiceCategory enum, FK staff_id, impacte reservation)
    vs user_specialties (texte libre profil/marketing). C4 = salon_staff_specialties UNIQUEMENT.
  AXE 2 — canPerformService utilise en 3 points : createBooking (validation), getAvailableSlots (filtre staff),
    getStaffBySpecialty (choix coiffeur). AUCUNE re-validation sur bookings existants.
  AXE 3 — IMPACT RDV : SANS DANGER. Declarer des specialites n'affecte que le FUTUR (nouvelles reservations).
    Les 7 RDV existants de Darla restent intacts meme si elle ne declare que {COUPE}.
  AXE 4 — DROITS : endpoint PUT /salons/{salonId}/staff/{staffId} existe, requiert permission staff.update
    (owner-only). Pour C4 : creer PUT /staff/me/specialties (garde isSelf) OU ajouter permission
    staff.update_own_specialties au role hairstylist.
  AXE 5 — UI : owner-staff.tsx a un composant chips categories reutilisable (ServiceCategory + SERVICE_CATEGORY_META
    + cles i18n service.categories.*). Coiffeur accederait via dashboard C2 ou menu profil.
  AMPLEUR : ~2h. Backend 1 endpoint OWN + frontend 1 ecran/modal + i18n.

  ### LOT C4 — Coiffeur C4 REPRIS apres coupure : inventaire existant + finition + preuve RDV intacts + Darla remise generaliste (2026-06-21)
  REPRISE apres coupure secteur. Une impl C4 existait deja (tentative anterieure hors boucle) ->
  INVENTAIRE d'abord, PUIS preuves. Resultat : DEJA FAIT et CORRECT, 0 ligne a corriger (Partie B vide).
  INVENTAIRE :
  - Backend StaffMeController.kt : PUT + GET /api/staff/me/specialties. Scope OWN parfait : resout le
    user AUTHENTIFIE (SecurityContext) -> son salon_staff actif (findByUserId.filter{isActive}.first),
    AUCUN staffId en parametre => impossible de toucher un collegue. Validation : categorie invalide ->
    400 (message + valeurs acceptees), liste vide acceptee (= generaliste), dedup via Set. GET expose
    specialties + allCategories (pre-cochage). COMPLET.
  - Frontend my-specialties.tsx : pas besoin de useMyStaffMemberships (le backend resout le staffId
    depuis l'auth, plus propre). Pre-coche via getMySpecialties. Chips des 7 ServiceCategory reutilisant
    SERVICE_CATEGORY_META + i18n service.categories.*. UX SEMANTIQUE VALORISANTE : badge generaliste
    (star-circle + "Vous realisez toutes les prestations"), etat specialise = "propose uniquement pour
    ces categories", note rassurante "Vos rendez-vous deja pris ne sont pas affectes". Save -> PUT ->
    toast succes ; catch -> toast (pas muet). Tous les hooks avant les early return. COMPLET.
  - Navigation : entree "Mes specialites" (icone content-cut) dans menu profil, gated isHairstylist
    (profile.tsx:309-313). OK.
  - i18n : bloc mySpecialties (10 cles) dans LES 5 langues (fr/en/es/de/ar), parite stricte.
  PREUVES API (token via refresh_tokens DB -> POST /api/users/refresh ; mysql CLI OK via 127.0.0.1, pas
  localhost) :
  - C2 PUT [COUPE,COLORATION] -> 200, GET reconfirme persistance.
  - C3 PUT [] -> 200 "Generaliste".
  - C4 PUT [XXX] -> 400 "Categorie invalide".
  - C5 (CRITIQUE) PUT [COUPE] -> 200, puis GET /staff/{id}/bookings -> 7 RDV TOUJOURS LA et intacts
    (1 completed BARBE, 2 confirmed COIFFAGE+SOIN, 4 pending COUPE x2/COLORATION/COIFFAGE) malgre la
    restriction a COUPE => declarer des specialites n'affecte QUE le futur, jamais les RDV existants.
  - C6 PUT [] -> 200, GET + DB confirment Darla revenue a 0 specialite (generaliste) ; 7 bookings intacts.
  BASELINES : tsc --noEmit = 0 ; check-keys = 0 ecart (958 cles fr ref, 5 langues) ; gradlew classes
  BUILD SUCCESSFUL. NON COMMITE (validation humaine).

  ### DIAGNOSTIC SERVICES + GALERIE PHOTOS (2026-06-21, pure lecture)
  AXE 1 — STRUCTURE SERVICE :
    Table salon_services : id CHAR(36) PK, salon_id FK, name VARCHAR(255) NOT NULL, description TEXT,
    category ENUM(COUPE/COLORATION/SOIN/BARBE/COIFFAGE/TECHNIQUE/AUTRE), duration_minutes INT NOT NULL,
    price DECIMAL(10,2) NOT NULL, is_available TINYINT(1) default 1, image_urls TEXT (CSV de chemins),
    created_at/updated_at TIMESTAMP.
    Entite Kotlin SalonService.kt : imageUrls = String? (CSV comma-separated).
    DTO CreateServiceRequest : salonId, name (3-150), description, durationMinutes (1-480),
    price (0-10000), category, imageUrls List<String>? (max 5 — @Size(max=5)).
    Endpoint : POST /api/salons/{salonId}/services (+ PUT /{serviceId}, DELETE, batch, search, stats).
  AXE 2 — GALERIE 5 PHOTOS :
    (2a) Stockage : PAS de table dediee. Champ image_urls TEXT dans salon_services. Format = CSV de
         chemins relatifs (ex. /uploads/uuid.jpg,/uploads/uuid2.jpg,...). Parsing : split(",").filter(isNotBlank).
         Limite 5 = validation DTO (@Size(max=5)) MAIS pas de contrainte DB.
    (2b) Upload : VRAI FICHIER depose sur le serveur.
         - App : mediaApi.uploadImage(uri, fileName) -> POST /api/media/upload (multipart/form-data).
         - Backend : MediaService.saveFile() -> dossier backend/uploads/ (UUID.ext), retourne /uploads/uuid.jpg.
         - create-service.tsx : pour chaque image locale, appelle mediaApi.uploadImage AVANT le POST service,
           puis envoie la liste de paths au backend dans imageUrls[].
         - UpdateServiceRequest.applyTo() : imageUrls?.let { service.imageUrls = it.joinToString(",") }.
    (2c) Affichage :
         - Composant ServiceImageStack (src/components/common/) : stack 3D si multi, single, ou icone categorie fallback.
         - Utilise resolveMediaUrl(path) qui prefixe /uploads/... avec API_BASE_URL (localhost:8090 en dev).
         - Ecrans : salon/[id].tsx (onglet prestations), owner-services.tsx (liste owner), create-service.tsx (edit prefill).
    (2d) Pas de photo principale/ordre explicite. L'ordre = l'ordre du CSV. La premiere est la plus visible dans le stack.
  AXE 3 — ETAT ECLAT PRESTIGE (salon abbd7b70-...) :
    5 services total :
      POUBELLE (4) :
        - Batch-MGR-1781770103   | COUPE | 1000 FCFA | 30min | pas de photos | 0 bookings -> SUPPRIMABLE
        - Batch-OWNER-1781770103 | COUPE | 1000 FCFA | 30min | pas de photos | 1 booking  -> PROTEGE (FK)
        - Create-MGR-1781770103  | COUPE | 1000 FCFA | 30min | pas de photos | 0 bookings -> SUPPRIMABLE
        - Create-OWNER-1781770103| COUPE | 1000 FCFA | 30min | pas de photos | 0 bookings -> SUPPRIMABLE
      SEMI-POUBELLE (1) :
        - Updated Service Name   | COUPE | 15 FCFA   | 60min | 5 photos OK   | 3 bookings -> PROTEGE (FK)
    VERDICT : 2 services proteges par bookings (FK fk_booking_service), 3 libres.
  AXE 4 — MEDIA :
    Fichiers dans backend/uploads/ (disque local, ~38 Mo, fichiers UUID.jpg).
    Servis par WebConfig : /uploads/** -> file:uploads/.
    resolveMediaUrl() prefixe /uploads/... avec http://localhost:8090.
  APPROCHE SEED (a valider, NON executee) :
    (i) PHOTOS : on NE PEUT PAS simplement stocker des URLs Unsplash/Pexels dans image_urls car le
        mecanisme resolveMediaUrl attend des chemins /uploads/... et le backend sert les fichiers
        localement. DEUX OPTIONS :
        A) TELECHARGER les images (curl/wget) dans backend/uploads/ avec des noms UUID.jpg, puis
           stocker les chemins /uploads/uuid.jpg dans image_urls. APPROCHE PROPRE — les images sont
           locales, le mecanisme d'affichage existant fonctionne tel quel.
        B) Stocker des URLs absolues externes (https://images.unsplash.com/...) dans image_urls.
           resolveMediaUrl les retournerait telles quelles (case 4 : absolute URL). CA MARCHERAIT
           car resolveMediaUrl ne touche pas aux URLs absolues. PLUS SIMPLE, pas de fichier a deposer.
           MAIS : depend de la connectivite internet, les URLs Unsplash peuvent expirer/changer.
        >>> RECOMMANDATION : option B (URLs externes Unsplash) pour le seed dev/demo, c'est immediat
            et resolveMediaUrl les passe telles quelles. Pour la prod, option A.
    (ii) ASSAINISSEMENT :
        - 3 services libres (0 booking) : SUPPRIMER via DELETE /api/salons/{salonId}/services/{id}.
        - 2 services proteges (bookings existants) : RENOMMER + corriger prix/duree/categorie via
          PUT /api/salons/{salonId}/services/{id}. Ex: "Updated Service Name" -> "Coupe Homme",
          "Batch-OWNER-1781770103" -> "Tresses Africaines". Ajouter photos.
        - Puis CREER les services manquants (coloration, soin, brushing, barbe...) avec photos.

  ### ASSAINISSEMENT + CATALOGUE ECLAT PRESTIGE — FAIT (2026-06-21)
  Operations executees via API (refresh token owner, pas de login direct) :
    SUPPRIME (3, 0 booking chacun) :
      - Batch-MGR-1781770103 (efa79ec0) — HTTP 204
      - Create-MGR-1781770103 (38015151) — HTTP 204
      - Create-OWNER-1781770103 (d6e12f06) — HTTP 204
    RENOMME (2, IDs conserves, bookings intacts) :
      - "Updated Service Name" (43528c04, 3 bookings) -> "Coupe Femme" | COUPE | 7000 FCFA | 45min | 5 photos
      - "Batch-OWNER-1781770103" (f1324c67, 1 booking) -> "Tresses Africaines" | COIFFAGE | 10000 FCFA | 3h | 5 photos
    CREE (8 nouveaux) :
      - Coupe Homme | COUPE | 4000 FCFA | 30min | 5 photos
      - Coloration Complete | COLORATION | 10000 FCFA | 2h | 5 photos
      - Soin Capillaire Profond | SOIN | 8000 FCFA | 1h | 5 photos
      - Brushing & Mise en Plis | COIFFAGE | 6000 FCFA | 45min | 5 photos
      - Taille de Barbe | BARBE | 2500 FCFA | 20min | 5 photos
      - Defrisage | TECHNIQUE | 9000 FCFA | 1h30 | 5 photos
      - Locks & Entretien | COIFFAGE | 8000 FCFA | 1h30 | 5 photos
      - Tissage & Extensions | TECHNIQUE | 10000 FCFA | 2h30 | 5 photos
  CATALOGUE FINAL : 10 prestations, 6 categories, 50 photos (URLs Unsplash absolues verifiees HTTP 200).
  Prix max DTO = 10000 (validation @DecimalMax) — respecte. Aucun nom-poubelle restant.
  Photos = URLs absolues Unsplash, resolveMediaUrl les passe telles quelles (case 4).

  ### SEED RESERVATIONS DURABLES — FAIT (2026-06-21)
  Reservations creees via API (refresh token clients, pas login direct) + 1 SQL (passe).
  Calendrier : Jun 21 = dimanche. Lun 09-12+14-19, Mar-Ven 09-19, Sam 10-17, Dim ferme.
  canPerformService : specialties vides = generaliste -> tous les coiffeurs acceptent tout.
  API refuse dates passees -> 1 booking completed insere en SQL.

  DARLA (staffId a2d99c45, 7 bookings) :
    | Date       | Heure | Service                  | Client       | Statut    | Methode |
    | 2026-06-20 | 10:00 | Taille de Barbe (20min)  | Olsen        | completed | SQL     |
    | 2026-06-24 | 13:00 | Coupe Femme (60min)      | Olsen        | pending   | existant|
    | 2026-06-25 | 09:30 | Coloration Complete (2h)  | Olsen        | pending   | API     |
    | 2026-06-26 | 14:00 | Brushing (45min)         | Test NoStaff | confirmed | API     |
    | 2026-06-27 | 10:00 | Soin Profond (1h)        | Olsen        | confirmed | API     |
    | 2026-06-29 | 08:00 | Tresses (30min)          | Arch         | pending   | existant|
    | 2026-06-30 | 11:00 | Coupe Homme (30min)      | Test NoStaff | pending   | API     |
  AUTRES COIFFEURS :
    Read Documents (bd70fa3d) : 2 bookings (1 pending Coupe Homme Jeu 25, 1 confirmed Barbe Ven 26)
    Test Coiffeur (staff-hair-001) : 1 pending Defrisage Ven 26 15:00
    (+ bookings preexistants test-staff-hair-001 et test-staff-mgr-001)
  VERIFICATIONS : aucun chevauchement Darla (verifie SQL), creneaux dans horaires, statuts minuscules,
  services du catalogue assaini. Transitions confirmed via owner (PATCH /bookings/{id}/status).

  ### LOT C2 — FAIT (2026-06-21)
  Coiffeur C2 : dashboard premium (prochain RDV, KPIs perso scope OWN, timeline du jour, apercu a venir).
  - ARTICULATION : dashboard C2 = point d'entree principal (menu profil "Tableau de bord" icone dashboard).
    Agenda C1 (my-schedule) = vue detail, accessible via bouton "Voir tout mon agenda" dans C2.
  - Ecran app/staff-dashboard.tsx : premium (Cormorant titres, Manrope UI, tokens design system).
    Sections : header (salut heure du jour + prenom + date longue) + banner salon (sélecteur si multi) +
    carte prochain RDV (primaryContainer, avatar client, nom, prestation, heure, "dans X min/h/j") +
    4 KPIs perso scope OWN (aujourd'hui/cette semaine/a venir/realises ce mois, cartes accent + icones) +
    timeline du jour (dot + ligne, heure debut-fin, avatar client, prestation, badge statut couleur, passes estompes) +
    apercu prochains jours (3 RDV futurs non-aujourd'hui) +
    prestations frequentes (top 3 avec rang) +
    bouton CTA "Voir tout mon agenda" -> my-schedule.
    Etats : loading, vide (pas de rattachement, pas de RDV today), erreur toast.
  - Navigation : entree "Tableau de bord" (icone dashboard) remplace "Mon agenda" dans profile.tsx.
  - i18n 5 langues (23 cles x 5 = 914->937 cles FR, parite 0 ecart).
  - Backend : 0 touche. Donnees de getStaffBookings, calculs cote app.
  - tsc --noEmit = 0, check-keys = 0 ecart.
