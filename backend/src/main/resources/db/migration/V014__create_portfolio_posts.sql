-- Migration pour créer la table portfolio_posts
-- Association entre les portfolios et les posts

CREATE TABLE IF NOT EXISTS portfolio_posts (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    portfolio_id CHAR(36) NOT NULL,
    post_id CHAR(36) NOT NULL,
    order_index INT DEFAULT 0,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    UNIQUE KEY unique_portfolio_post (portfolio_id, post_id),
    INDEX idx_portfolio_posts_portfolio (portfolio_id),
    INDEX idx_portfolio_posts_post (post_id),
    INDEX idx_portfolio_posts_order (portfolio_id, order_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

