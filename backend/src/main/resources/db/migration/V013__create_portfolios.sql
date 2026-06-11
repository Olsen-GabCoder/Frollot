-- Migration pour créer la table portfolios
-- Permet aux coiffeurs et salons d'organiser leurs créations en portfolios

CREATE TABLE IF NOT EXISTS portfolios (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    owner_id CHAR(36) NOT NULL,
    owner_type ENUM('coiffeur', 'salon') NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    cover_image_url VARCHAR(500),
    is_public BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_portfolio_owner (owner_id, owner_type),
    INDEX idx_portfolio_public (is_public)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

