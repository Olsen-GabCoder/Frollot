-- V033: Ajoute la colonne stripe_session_id pour Stripe Checkout Sessions
-- Cette colonne permet de lier un paiement à une Checkout Session Stripe

ALTER TABLE payments 
ADD COLUMN stripe_session_id VARCHAR(255) NULL;

-- Index pour recherche rapide par session ID
CREATE INDEX idx_payment_stripe_session ON payments(stripe_session_id);

