-- V048 : Système de permissions par rôle pour la gestion salon
-- Rôles concernés : manager, hairstylist, apprentice
-- L'owner a TOUTES les permissions (cas spécial géré en code, pas dans cette table)

CREATE TABLE IF NOT EXISTS permissions (
    permission_key VARCHAR(60) NOT NULL,
    description    VARCHAR(200) DEFAULT NULL,
    PRIMARY KEY (permission_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS role_permissions (
    role           VARCHAR(30) NOT NULL,
    permission_key VARCHAR(60) NOT NULL,
    PRIMARY KEY (role, permission_key),
    CONSTRAINT fk_role_perm_key FOREIGN KEY (permission_key) REFERENCES permissions(permission_key) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_role_permissions_role ON role_permissions(role);

-- ========== SEED : 33 permissions ==========

INSERT INTO permissions (permission_key, description) VALUES
('salon.update_cover',      'Modifier la photo de couverture du salon'),
('salon.update_info',       'Modifier les informations du salon'),
('salon.update_hours',      'Modifier les horaires du salon'),
('service.create',          'Creer une prestation'),
('service.update',          'Modifier une prestation'),
('service.delete',          'Supprimer une prestation'),
('service.import',          'Importer des prestations en lot'),
('staff.add',               'Ajouter un membre a l equipe'),
('staff.update',            'Modifier un membre de l equipe'),
('staff.remove',            'Retirer un membre de l equipe'),
('invitation.search',       'Chercher des coiffeurs invitables'),
('invitation.create',       'Creer une invitation'),
('invitation.list',         'Lister les invitations du salon'),
('invitation.cancel',       'Annuler une invitation'),
('booking.view_all',        'Voir toutes les reservations du salon'),
('booking.view_upcoming',   'Voir les reservations a venir du salon'),
('booking.view_own',        'Voir ses propres reservations (staff assigne)'),
('booking.manage_status',   'Modifier le statut d une reservation'),
('booking.manage_payment',  'Modifier le paiement d une reservation'),
('booking.cancel',          'Annuler une reservation'),
('booking.view_statistics', 'Voir les statistiques de reservations'),
('booking.view_daily',      'Voir la serie temporelle journaliere'),
('queue.call_next',         'Appeler le prochain client en file d attente'),
('review.reply',            'Repondre a un avis'),
('portfolio.create',        'Creer un portfolio salon'),
('portfolio.update',        'Modifier un portfolio salon'),
('portfolio.delete',        'Supprimer un portfolio salon'),
('portfolio.manage_posts',  'Gerer les posts d un portfolio salon'),
('payment.refund',          'Rembourser un paiement'),
('payment.view_salon',      'Voir les paiements du salon'),
('social.update_profile',   'Modifier le profil social du salon'),
('verification.request',    'Demander la verification du salon');

-- ========== SEED : matrice role -> permissions ==========
-- manager : 17 permissions
INSERT INTO role_permissions (role, permission_key) VALUES
('manager', 'salon.update_cover'),
('manager', 'salon.update_info'),
('manager', 'salon.update_hours'),
('manager', 'service.create'),
('manager', 'service.update'),
('manager', 'service.delete'),
('manager', 'service.import'),
('manager', 'invitation.list'),
('manager', 'booking.view_all'),
('manager', 'booking.view_upcoming'),
('manager', 'booking.view_own'),
('manager', 'booking.manage_status'),
('manager', 'booking.cancel'),
('manager', 'booking.view_statistics'),
('manager', 'booking.view_daily'),
('manager', 'queue.call_next'),
('manager', 'review.reply'),
('manager', 'portfolio.create'),
('manager', 'portfolio.update'),
('manager', 'portfolio.manage_posts'),
('manager', 'social.update_profile');

-- hairstylist : 3 permissions
INSERT INTO role_permissions (role, permission_key) VALUES
('hairstylist', 'booking.view_own'),
('hairstylist', 'booking.manage_status'),
('hairstylist', 'booking.cancel'),
('hairstylist', 'queue.call_next');

-- apprentice : 1 permission
INSERT INTO role_permissions (role, permission_key) VALUES
('apprentice', 'booking.view_own');
