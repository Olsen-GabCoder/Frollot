-- B35 - Correction du bug du fil "Suivis" (divergence de casse following_type)
--
-- Cause racine : Follow.kt utilise @Enumerated(EnumType.STRING) avec l'enum Kotlin
-- FollowingType { USER, SALON, COIFFEUR } (MAJUSCULES), mais V016 a cree la colonne
-- en ENUM('user','salon','coiffeur') (minuscules). A l'INSERT, MySQL accepte 'SALON'
-- (collation insensible a la casse) mais stocke la valeur canonique 'salon'.
-- A la LECTURE, Hibernate fait Enum.valueOf("salon") — sensible a la casse — qui crashe :
-- "No enum constant com.frollot.model.FollowingType.salon".
--
-- Endpoints casses repares par cette migration : GET /feed/following (400),
-- GET /coiffeurs|salons/{id}/followers (500), GET /users/{id}/following (500),
-- DELETE .../follow (unfollow, 500).
--
-- Meme motif et meme remede que V020 (reaction_type) et V025 (visibility) :
-- on aligne la base sur le code.

-- Etape 1 : Passer la colonne en VARCHAR(20), coherent avec l'entite (length = 20)
ALTER TABLE follows
MODIFY COLUMN following_type VARCHAR(20) NOT NULL;

-- Etape 2 : Mettre les donnees existantes en majuscules pour matcher l'enum Kotlin
UPDATE follows SET following_type = UPPER(following_type);
