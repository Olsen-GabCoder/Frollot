-- V031: Ajout des images d'illustration aux services
-- Permet aux clients de visualiser à quoi correspond chaque prestation

ALTER TABLE salon_services
ADD COLUMN image_urls TEXT NULL COMMENT 'URLs des images d''illustration du service, séparées par des virgules (max 5 images)';

-- Index pour optimiser les recherches de services avec images
CREATE INDEX idx_salon_services_has_images ON salon_services ((image_urls IS NOT NULL));

