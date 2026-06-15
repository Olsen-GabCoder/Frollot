-- Migration V045 : Visibilite du numero de telephone (increment 1 ChangePhoneScreen)
--
-- Le numero (users.phone_number, E.164, UNIQUE - inchange) est DECLARATIF, sans
-- verification (couche future). L'utilisateur choisit sa visibilite :
-- - FALSE (defaut) : prive - seul le proprietaire (GET /me) et le canal
--   transactionnel (clientPhone des reservations vues par le salon) le voient.
-- - TRUE : expose aussi sur les vues publiques (recherche, followers, equipe salon).

ALTER TABLE users
ADD COLUMN phone_public BOOLEAN NOT NULL DEFAULT FALSE;
