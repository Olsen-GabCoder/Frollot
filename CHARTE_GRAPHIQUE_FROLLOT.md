# Charte Graphique Frollot — Application de Réservation Coiffure

**Version :** 1.0  
**Date :** Décembre 2024  
**Application :** Frollot — Marketplace et Réseau Social Coiffure

---

## 📋 Table des matières

1. [Vue d'ensemble](#vue-densemble)
2. [Identité visuelle](#identité-visuelle)
3. [Palette de couleurs](#palette-de-couleurs)
4. [Typographie](#typographie)
5. [Espacements et grille](#espacements-et-grille)
6. [Cartes et surfaces](#cartes-et-surfaces)
7. [Composants UI](#composants-ui)
8. [Hiérarchie visuelle](#hiérarchie-visuelle)
9. [Animations et transitions](#animations-et-transitions)
10. [Bonnes pratiques](#bonnes-pratiques)

---

## 🎯 Vue d'ensemble

### Objectifs de la charte

Cette charte graphique définit les principes de design pour **Frollot**, une application mobile multiplateforme (Android, iOS, Web) combinant marketplace de réservation de salons de coiffure et réseau social dédié à la beauté et à la coiffure.

### Principes directeurs

1. **Modernité et élégance** : Interface contemporaine inspirée des meilleures applications sociales (Instagram, Threads, Twitter X) tout en conservant une identité propre au secteur de la beauté.
2. **Sobriété et raffinement** : Design épuré qui met en valeur le contenu (photos de coiffures, portfolios) sans surcharger l'interface.
3. **Cohérence globale** : Système de design unifié applicable à tous les écrans et composants.
4. **Lisibilité et accessibilité** : Contraste élevé, typographie lisible, espacements généreux.
5. **Équilibre visuel** : Élévations subtiles, bordures fines, espacements harmonieux.

### Problématiques identifiées dans l'existant

- **Incohérence des élévations** : Élévations variées (2dp à 16dp) sans hiérarchie claire.
- **Cartes surélevées** : Certaines cartes ont des ombres trop prononcées (12dp, 16dp), créant un effet "flottant" excessif.
- **Manque de finesse** : Bordures parfois absentes, ombres trop marquées.
- **Couleurs hardcodées** : Utilisation de couleurs en dur (`Color(0xFF4F46E5)`) au lieu du système de thème Material.
- **Bordures arrondies variables** : Rayons de coins non standardisés (8dp, 12dp, 16dp, 20dp, 24dp, 32dp).
- **Espacements non systématisés** : Valeurs arbitraires sans système d'échelle cohérent.

---

## 🎨 Identité visuelle

### Ambiance générale

L'application doit transmettre :
- **Luxe accessible** : Premium mais accessible, pas élitiste.
- **Professionnalisme** : Confiance et expertise dans le secteur de la beauté.
- **Inspiration** : Stimuler la créativité et l'envie de se faire beau/belle.
- **Communauté** : Chaleur humaine et partage d'expériences.

### Style visuel

- **Minimalisme fonctionnel** : Chaque élément a un but, pas de décorations superflues.
- **Cards plates avec bordures subtiles** : Préférence pour les bordures fines plutôt que les ombres lourdes.
- **Gradients discrets** : Utilisés avec parcimonie pour les accents (avatars, badges).
- **Micro-interactions** : Animations subtiles et fluides pour améliorer le feedback utilisateur.

---

## 🎨 Palette de couleurs

### Architecture Material Design 3

L'application utilise **Material Design 3** avec une palette de couleurs personnalisée adaptée au secteur de la beauté.

### Couleurs primaires (Beauté & Coiffure)

```
Primary (Violet Premium)
├── Primary: #6B46C1 (Indigo-600) — Actions principales, CTA, liens actifs
├── On Primary: #FFFFFF — Texte sur fond primaire
├── Primary Container: #EDE9FE — Fond des éléments primaires secondaires
└── On Primary Container: #4C1D95 — Texte sur Primary Container

Secondary (Rose Soie)
├── Secondary: #EC4899 (Pink-500) — Accents, éléments secondaires
├── On Secondary: #FFFFFF
├── Secondary Container: #FCE7F3
└── On Secondary Container: #BE185D

Tertiary (Or Élégant)
├── Tertiary: #F59E0B (Amber-500) — Badges, notifications, temps d'attente
├── On Tertiary: #FFFFFF
├── Tertiary Container: #FEF3C7
└── On Tertiary Container: #92400E
```

### Couleurs de surface

```
Surface
├── Surface: #FFFFFF — Fond principal des écrans (clair)
├── On Surface: #0F172A — Texte principal (Slate-900)
├── Surface Variant: #F1F5F9 — Fonds alternatifs (Slate-100)
└── On Surface Variant: #64748B — Texte secondaire (Slate-500)

Surface Container (Hiérarchie)
├── Surface Container Highest: #E2E8F0 — Éléments élevés (Slate-200)
├── Surface Container High: #F1F5F9 — Conteneurs secondaires (Slate-100)
├── Surface Container: #F8FAFC — Conteneurs standards (Slate-50)
└── Surface Container Low: #FFFFFF — Fond de base
```

### Couleurs d'état

```
Error
├── Error: #DC2626 (Red-600) — Erreurs, actions destructives
├── On Error: #FFFFFF
├── Error Container: #FEE2E2
└── On Error Container: #991B1B

Success
├── Success: #10B981 (Green-500) — Confirmations, succès
├── On Success: #FFFFFF
├── Success Container: #D1FAE5
└── On Success Container: #047857

Warning
├── Warning: #F59E0B (Amber-500) — Avertissements
└── On Warning: #FFFFFF

Info
├── Info: #3B82F6 (Blue-500) — Informations, liens externes
└── On Info: #FFFFFF
```

### Couleurs fonctionnelles (sémantiques)

```
Neutral
├── Outline: #CBD5E1 (Slate-300) — Bordures standards
├── Outline Variant: #E2E8F0 (Slate-200) — Bordures subtiles
└── Scrim: #0F172A (Slate-900) avec alpha 0.32 — Overlays

Background
├── Background: #FFFFFF — Fond de l'application
└── Inverse Surface: #0F172A — Inversion pour éléments spéciaux

Accent Beauté (utilisées avec parcimonie)
├── Gold: #F59E0B — Badges premium, vérifications
├── Rose: #EC4899 — Likes, réactions émotionnelles
├── Indigo: #6366F1 — Tags, mentions
└── Emerald: #10B981 — Disponibilité, statuts positifs
```

### Règles d'utilisation des couleurs

1. **Toujours utiliser MaterialTheme.colorScheme** : Jamais de couleurs hardcodées (`Color(0xFF...)`).
2. **Contraste minimum** : Ratio de contraste de 4.5:1 pour le texte normal, 3:1 pour le texte large.
3. **Cohérence sémantique** : Primary pour actions principales, Error pour erreurs, Success pour confirmations.
4. **Modération des accents** : Utiliser les couleurs vives (Tertiary, Secondary) avec parcimonie pour attirer l'attention.

---

## 📝 Typographie

### Hiérarchie typographique (Material Design 3)

L'application utilise le système typographique Material Design 3 avec des ajustements pour le secteur de la beauté.

#### Styles de texte principaux

```kotlin
// Headlines — Titres d'écran
HeadlineLarge: 32sp, FontWeight.Bold, LineHeight 40sp
HeadlineMedium: 28sp, FontWeight.Bold, LineHeight 36sp
HeadlineSmall: 24sp, FontWeight.Bold, LineHeight 32sp

// Titles — Titres de sections
TitleLarge: 22sp, FontWeight.SemiBold, LineHeight 28sp
TitleMedium: 16sp, FontWeight.SemiBold, LineHeight 24sp
TitleSmall: 14sp, FontWeight.Medium, LineHeight 20sp

// Body — Contenu principal
BodyLarge: 16sp, FontWeight.Normal, LineHeight 24sp
BodyMedium: 14sp, FontWeight.Normal, LineHeight 20sp
BodySmall: 12sp, FontWeight.Normal, LineHeight 16sp

// Labels — Labels de boutons, tags
LabelLarge: 14sp, FontWeight.Medium, LineHeight 20sp
LabelMedium: 12sp, FontWeight.Medium, LineHeight 16sp
LabelSmall: 11sp, FontWeight.Medium, LineHeight 16sp
```

### Utilisation par contexte

| Contexte | Style | Poids | Taille | Exemple |
|----------|-------|-------|--------|---------|
| Titre d'écran | HeadlineMedium | Bold | 28sp | "Découvrir" |
| Titre de section | TitleLarge | SemiBold | 22sp | "Salons près de vous" |
| Nom d'utilisateur | TitleMedium | SemiBold | 16sp | "@marie_coiffure" |
| Description de post | BodyMedium | Normal | 14sp | "Nouvelle coupe réalisée aujourd'hui..." |
| Métadonnées | BodySmall | Normal | 12sp | "Il y a 2h • Paris" |
| Label de bouton | LabelLarge | Medium | 14sp | "Réserver" |
| Tag/Hashtag | LabelMedium | Medium | 12sp | "#coiffure" |

### Lettrage (Letter Spacing)

- **Labels en majuscules** : +1.2sp (ex: "MENU", "PROFIL")
- **Labels normaux** : +0.5sp
- **Body text** : 0sp (par défaut)

### Couleurs typographiques

- **Texte principal** : `MaterialTheme.colorScheme.onSurface` (#0F172A)
- **Texte secondaire** : `MaterialTheme.colorScheme.onSurfaceVariant` (#64748B)
- **Texte désactivé** : `MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)`
- **Liens actifs** : `MaterialTheme.colorScheme.primary`
- **Erreurs** : `MaterialTheme.colorScheme.error`

### Règles typographiques

1. **Ne jamais utiliser de tailles personnalisées** sauf cas exceptionnel (ex: emojis 12sp).
2. **Respecter la hiérarchie** : Un seul `HeadlineLarge` par écran, plusieurs `TitleMedium` pour les sections.
3. **Limiter les FontWeight** : Bold (titres), SemiBold (sous-titres), Medium (labels), Normal (corps).
4. **Éviter FontWeight.Black** sauf pour le logo ou éléments très spécifiques.

---

## 📏 Espacements et grille

### Système d'espacement (8dp Grid)

L'application utilise une grille de 8dp pour tous les espacements (padding, margin, gaps).

```
Base: 4dp (micro-espacements)
├── 4dp: Espacements très serrés (icônes dans boutons)
├── 8dp: Espacements standards (padding interne de cartes)
├── 12dp: Espacements moyens (gaps entre éléments)
├── 16dp: Espacements larges (padding externe de cartes, sections)
├── 24dp: Espacements très larges (sections principales)
└── 32dp: Espacements maximaux (marges d'écran)
```

### Grille de layout

- **Marges d'écran** : 16dp (horizontal), 8dp (vertical pour les listes)
- **Padding interne de cartes** : 16dp
- **Gaps entre cartes dans une liste** : 12dp
- **Espacements entre sections** : 24dp
- **Espacements dans les lignes (Row)** : 8dp, 12dp, 16dp selon la densité

### Règles d'espacement

1. **Utiliser uniquement des multiples de 4dp** (4, 8, 12, 16, 20, 24, 32).
2. **Cohérence horizontale** : Padding horizontal identique dans une même liste.
3. **Respiration verticale** : Espacements verticaux généreux pour améliorer la lisibilité.

---

## 🎴 Cartes et surfaces

### Principes généraux

Les cartes sont l'élément central de l'interface. Elles doivent être :
- **Discrètes mais présentes** : Bordures subtiles plutôt qu'ombres lourdes.
- **Cohérentes** : Même style pour tous les types de cartes (posts, salons, avis).
- **Équilibrées** : Élévation minimale, bordures fines, espacements harmonieux.

### Style de carte standard

```kotlin
Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp), // Standardisé à 16dp
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(
        defaultElevation = 1.dp, // Élévation minimale
        pressedElevation = 2.dp  // Légère augmentation au press
    ),
    border = BorderStroke(
        width = 0.5.dp, // Bordure très fine
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    )
) {
    // Contenu avec padding de 16dp
}
```

### Hiérarchie des élévations

| Élément | Élévation | Usage |
|---------|-----------|-------|
| Surface/Carte standard | 1dp | Cartes de contenu (posts, salons, avis) |
| Carte interactive (hover/press) | 2dp | État pressé |
| FAB (Floating Action Button) | 4dp | Boutons flottants |
| Dialogues, Bottom Sheets | 8dp | Surfaces modales |
| AppBar/Header | 0dp | Pas d'ombre, seulement bordure inférieure si nécessaire |

### Bordures arrondies (corner radius)

| Élément | Rayon | Usage |
|---------|-------|-------|
| Cartes standard | 16dp | Posts, salons, avis, collections |
| Cartes compactes | 12dp | Items de liste secondaires, tags |
| Boutons | 12dp | Boutons principaux et secondaires |
| Chips/Tags | 20dp (pill) | Filtres, tags, hashtags |
| Avatars | Circle | Photos de profil (diamètre variable) |
| Badges | 8dp | Petits badges d'état |
| Dialogs | 24dp | Dialogues modaux |
| Bottom Sheets | 24dp (top) | Bottom sheets (coins supérieurs arrondis) |

### Bordures (Border)

- **Épaisseur standard** : 0.5dp pour les cartes, 1dp pour les séparateurs.
- **Couleur** : `MaterialTheme.colorScheme.outlineVariant` avec alpha 0.3 pour les cartes, 0.5 pour les séparateurs.
- **Usage** : Préférer les bordures aux ombres pour définir les limites des cartes.

### Règles pour les cartes

1. **Élévation maximale 1dp** pour les cartes de contenu standard.
2. **Toujours inclure une bordure fine** (0.5dp) pour définir les limites.
3. **Padding interne de 16dp** pour tous les contenus de carte.
4. **Rayon de 16dp** standardisé pour toutes les cartes principales.
5. **Pas d'ombres multiples** : Une seule source d'ombre subtile.

---

## 🧩 Composants UI

### Boutons

#### Bouton principal (Primary Button)

```kotlin
Button(
    onClick = { },
    shape = RoundedCornerShape(12.dp),
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ),
    elevation = ButtonDefaults.buttonElevation(
        defaultElevation = 0.dp, // Pas d'ombre
        pressedElevation = 0.dp
    )
) {
    Text(
        text = "Réserver",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Medium
    )
}
```

- **Hauteur** : 48dp (standard), 56dp (grand)
- **Padding horizontal** : 24dp
- **Rayon** : 12dp
- **Pas d'ombre** : Boutons plats avec couleur pleine

#### Bouton secondaire (Outlined Button)

```kotlin
OutlinedButton(
    onClick = { },
    shape = RoundedCornerShape(12.dp),
    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
    colors = ButtonDefaults.outlinedButtonColors(
        contentColor = MaterialTheme.colorScheme.primary
    )
) {
    Text("Annuler")
}
```

- **Bordure** : 1.5dp
- **Couleur de bordure** : Primary
- **Fond transparent**

#### Bouton texte (Text Button)

```kotlin
TextButton(
    onClick = { }
) {
    Text("Voir plus")
}
```

- **Pas de bordure ni fond**
- **Couleur** : Primary ou OnSurface selon le contexte

### Chips / Tags

```kotlin
FilterChip(
    selected = isSelected,
    onClick = { },
    label = { Text("Tous") },
    shape = RoundedCornerShape(20.dp), // Pill shape
    colors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
)
```

- **Forme** : Pill (20dp de rayon)
- **Hauteur** : 32dp
- **Padding horizontal** : 12dp

### Avatars

- **Tailles standards** : 24dp, 32dp, 40dp, 48dp, 64dp, 80dp
- **Forme** : Circle (toujours)
- **Bordure optionnelle** : 2dp avec couleur primaire pour les utilisateurs vérifiés

### Badges

```kotlin
Surface(
    shape = RoundedCornerShape(8.dp),
    color = MaterialTheme.colorScheme.primaryContainer
) {
    Text(
        text = "Vérifié",
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer
    )
}
```

- **Rayon** : 8dp
- **Padding** : 8dp horizontal, 4dp vertical
- **Typographie** : LabelSmall

### Séparateurs (Dividers)

```kotlin
HorizontalDivider(
    thickness = 0.5.dp,
    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
)
```

- **Épaisseur** : 0.5dp
- **Couleur** : OutlineVariant avec alpha 0.5
- **Pas de padding** : S'étend sur toute la largeur disponible

### Champs de texte (Text Fields)

```kotlin
OutlinedTextField(
    value = text,
    onValueChange = { },
    shape = RoundedCornerShape(12.dp),
    colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
    )
)
```

- **Rayon** : 12dp
- **Bordure** : 1dp non focus, 2dp focus
- **Hauteur minimale** : 56dp

---

## 📊 Hiérarchie visuelle

### Niveaux de profondeur

1. **Surface (fond)** : 0dp — Fond de l'écran
2. **Cartes de contenu** : 1dp — Posts, salons, avis
3. **FAB** : 4dp — Boutons flottants
4. **Dialogs/Modals** : 8dp — Surfaces modales

### Focus et états interactifs

- **États hover (Web)** : Légère élévation +2dp
- **États pressés** : Élévation +1dp (si applicable)
- **États focus** : Bordure de 2dp avec couleur primary

### Règles de hiérarchie

1. **Un seul élément très élevé** (FAB, Dialog) à la fois.
2. **Cartes au même niveau** : Toutes les cartes de contenu ont la même élévation (1dp).
3. **Hiérarchie par contraste** : Utiliser les couleurs et les espacements pour créer la hiérarchie, pas seulement l'élévation.

---

## ✨ Animations et transitions

### Durées standard

- **Micro-interactions** : 150ms (hover, press)
- **Transitions d'état** : 300ms (changement de couleur, taille)
- **Navigation** : 300ms (transitions d'écran)
- **Animations complexes** : 500ms (apparitions, disparitions)

### Easing (courbes d'animation)

- **Standard** : `FastOutSlowInEasing` (Material)
- **Entrées** : `DecelerateEasing`
- **Sorties** : `AccelerateEasing`
- **Bounce** : `Spring` avec `DampingRatioMediumBouncy` (pour les likes, réactions)

### Animations recommandées

1. **Likes/Reactions** : Animation spring avec scale (1.0 → 1.4 → 1.0) sur 300ms
2. **Apparition de cartes** : Fade in + slide up (300ms)
3. **Navigation** : Slide horizontal (300ms)
4. **Chargement** : CircularProgressIndicator avec animation continue

### Règles d'animation

1. **Modération** : Animations subtiles, pas de mouvements excessifs.
2. **Performance** : Éviter les animations sur des listes longues.
3. **Accessibilité** : Respecter les préférences système (réduire les animations si demandé).

---

## ✅ Bonnes pratiques

### Do's ✅

1. **Utiliser MaterialTheme.colorScheme** pour toutes les couleurs
2. **Respecter la grille de 8dp** pour tous les espacements
3. **Standardiser les élévations** : 1dp pour les cartes, 4dp pour les FAB
4. **Inclure des bordures fines** (0.5dp) sur les cartes
5. **Utiliser les styles typographiques Material** sans tailles personnalisées
6. **Maintenir la cohérence** : Même style pour tous les composants similaires

### Don'ts ❌

1. **Ne pas hardcoder les couleurs** : Éviter `Color(0xFF4F46E5)`
2. **Ne pas utiliser d'élévation > 8dp** sauf pour les modals
3. **Ne pas mélanger les rayons** : 16dp pour les cartes, 12dp pour les boutons
4. **Ne pas surcharger avec les gradients** : Utiliser avec parcimonie
5. **Ne pas utiliser FontWeight.Black** sauf cas très spécifiques
6. **Ne pas créer d'espacements arbitraires** : Toujours utiliser des multiples de 4dp

### Checklist de conformité

Avant de créer ou modifier un composant, vérifier :

- [ ] Couleurs via `MaterialTheme.colorScheme` (pas de hardcoding)
- [ ] Élévation ≤ 1dp pour les cartes (sauf FAB: 4dp, Modals: 8dp)
- [ ] Bordure fine (0.5dp) sur les cartes
- [ ] Rayon de 16dp pour les cartes, 12dp pour les boutons
- [ ] Padding de 16dp dans les cartes
- [ ] Espacements en multiples de 4dp
- [ ] Typographie Material sans tailles personnalisées
- [ ] Contraste suffisant (texte lisible)

---

## 📐 Exemples de code

### Carte de post standard

```kotlin
@Composable
fun StandardPostCard(
    post: PostResponse,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            pressedElevation = 2.dp
        ),
        border = BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header avec avatar et nom
            PostHeader(post)
            
            // Contenu
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Actions (like, comment, share)
            PostActions(post)
        }
    }
}
```

### Bouton principal

```kotlin
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}
```

---

## 🎯 Migration depuis l'existant

### Priorités de migration

1. **Phase 1 — Cartes** : Réduire les élévations (12dp → 1dp), ajouter bordures, standardiser rayons (16dp)
2. **Phase 2 — Couleurs** : Remplacer toutes les couleurs hardcodées par `MaterialTheme.colorScheme`
3. **Phase 3 — Espacements** : Aligner tous les espacements sur la grille de 8dp
4. **Phase 4 — Typographie** : Standardiser les tailles de texte, éliminer les tailles personnalisées

### Outils de migration

- **Recherche globale** : Chercher `elevation =` et remplacer les valeurs > 1dp
- **Recherche couleurs** : Chercher `Color(0x` et remplacer par les équivalents MaterialTheme
- **Refactoring progressif** : Modifier écran par écran, composant par composant

---

## 📚 Ressources

### Références

- [Material Design 3 Guidelines](https://m3.material.io/)
- [Material Design Color System](https://m3.material.io/styles/color/the-color-system/overview)
- [Material Design Typography](https://m3.material.io/styles/typography/overview)

### Fichiers de référence dans le projet

- `frontend/composeApp/src/commonMain/kotlin/com/frollot/mobile/ui/components/` — Composants réutilisables
- `frontend/composeApp/src/commonMain/kotlin/com/frollot/mobile/ui/screens/` — Écrans principaux

---

**Document créé le :** Décembre 2024  
**Dernière mise à jour :** Décembre 2024  
**Version :** 1.0

