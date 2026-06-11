-- Migration pour créer la table post_services
-- Permet d'associer des posts aux services de coiffure proposés par les salons

CREATE TABLE IF NOT EXISTS post_services (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    post_id CHAR(36) NOT NULL,
    service_id CHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES salon_services(id) ON DELETE CASCADE,
    UNIQUE KEY unique_post_service (post_id, service_id),
    INDEX idx_post (post_id),
    INDEX idx_service (service_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

