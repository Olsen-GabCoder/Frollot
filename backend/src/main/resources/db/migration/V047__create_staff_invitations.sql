-- V047: Table des invitations d'équipe (système de consentement)
CREATE TABLE staff_invitations (
    id          CHAR(36)     NOT NULL PRIMARY KEY,
    salon_id    CHAR(36)     NOT NULL,
    invited_user_id CHAR(36) NULL,
    invited_email   VARCHAR(255) NULL,
    role        VARCHAR(30)  NOT NULL DEFAULT 'hairstylist',
    status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    token       CHAR(36)     NOT NULL,
    expires_at  DATETIME     NOT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_staff_inv_salon FOREIGN KEY (salon_id) REFERENCES salons(id) ON DELETE CASCADE,
    CONSTRAINT fk_staff_inv_user  FOREIGN KEY (invited_user_id) REFERENCES users(id) ON DELETE SET NULL,

    INDEX idx_staff_inv_salon   (salon_id),
    INDEX idx_staff_inv_user    (invited_user_id),
    INDEX idx_staff_inv_status  (status),
    INDEX idx_staff_inv_token   (token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Specialties stored in a join table (same pattern as salon_staff_specialties)
CREATE TABLE staff_invitation_specialties (
    invitation_id CHAR(36)    NOT NULL,
    specialty     VARCHAR(30) NOT NULL,

    CONSTRAINT fk_inv_spec_inv FOREIGN KEY (invitation_id) REFERENCES staff_invitations(id) ON DELETE CASCADE,
    PRIMARY KEY (invitation_id, specialty)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
