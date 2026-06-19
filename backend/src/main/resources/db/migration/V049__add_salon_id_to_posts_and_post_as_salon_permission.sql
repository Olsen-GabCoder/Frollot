-- V049 : Publier au nom du salon (mode 3)
-- 1. Ajouter salon_id nullable sur posts (FK vers salons)
-- 2. Corriger author_type des posts existants : tous sont 'salon' par erreur
--    (Post.kt ne mappait pas author_type, MySQL prenait la 1re valeur de l'enum)
-- 3. Mettre un DEFAULT 'user' sur author_type pour les futurs INSERT sans valeur explicite
-- 4. Ajouter la permission social.post_as_salon

-- 1. salon_id
ALTER TABLE posts ADD COLUMN salon_id CHAR(36) NULL AFTER author_id;
ALTER TABLE posts ADD CONSTRAINT fk_posts_salon FOREIGN KEY (salon_id) REFERENCES salons(id) ON DELETE SET NULL;
CREATE INDEX idx_post_salon ON posts(salon_id);

-- 2. Corriger les posts existants (tous sont des posts personnels)
UPDATE posts SET author_type = 'user' WHERE author_type != 'user';

-- 3. DEFAULT pour futurs INSERT
ALTER TABLE posts ALTER COLUMN author_type SET DEFAULT 'user';

-- 4. Permission social.post_as_salon
INSERT INTO permissions (permission_key, description) VALUES
('social.post_as_salon', 'Publier un post au nom du salon');

INSERT INTO role_permissions (role, permission_key) VALUES
('manager',     'social.post_as_salon'),
('hairstylist', 'social.post_as_salon'),
('apprentice',  'social.post_as_salon');
