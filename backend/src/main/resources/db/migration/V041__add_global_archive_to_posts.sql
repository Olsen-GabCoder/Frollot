-- Migration V041 : Archivage global des posts (façon Instagram)
-- Un post archivé est masqué pour TOUS les utilisateurs (pas seulement l'archiveur),
-- réversible par le propriétaire. Remplace l'archivage par utilisateur (post_archives).
-- La table post_archives est conservée dormante (future fonction "Masquer ce post").

ALTER TABLE posts
    ADD COLUMN is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN archived_at DATETIME NULL;

-- Migration des archives existantes : seules les archives owner-only
-- (l'utilisateur a archivé son propre post) deviennent des archives globales.
UPDATE posts p
JOIN post_archives pa ON pa.post_id = p.id AND pa.user_id = p.author_id
SET p.is_archived = TRUE,
    p.archived_at = pa.archived_at;
