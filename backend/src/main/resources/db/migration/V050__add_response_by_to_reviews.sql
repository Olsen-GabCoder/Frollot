-- V050 : Ajouter response_by pour tracer QUI a repondu a un avis (attribution double)
ALTER TABLE reviews ADD COLUMN response_by CHAR(36) NULL AFTER response_at;
ALTER TABLE reviews ADD CONSTRAINT fk_review_response_by FOREIGN KEY (response_by) REFERENCES users(id) ON DELETE SET NULL;
