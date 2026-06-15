-- B36 - Correction du 500 sur GET /api/salons/{id}/staff
--
-- Cause racine : l'entite SalonStaff.kt mappe une colonne is_active
-- (@Column(name = "is_active", nullable = false), defaut true) et declare
-- l'index idx_salon_staff_active, mais V001 a cree la table salon_staff
-- sans cette colonne. Toute lecture du staff echoue :
-- "Unknown column 'ss1_0.is_active' in 'field list'".
--
-- Meme principe que V042 : on aligne la base sur le code.

ALTER TABLE salon_staff
ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX idx_salon_staff_active ON salon_staff (is_active);
