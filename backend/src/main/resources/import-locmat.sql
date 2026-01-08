-- =============================================================
-- SCRIPT D'IMPORT LOCMAT - MAGSAV 3.0
-- Vide la table equipment et importe les données du fichier CSV
-- =============================================================

-- Désactiver les contraintes pour vider proprement
SET REFERENTIAL_INTEGRITY FALSE;

-- Vider les tables liées aux équipements
DELETE FROM equipment_photo;
DELETE FROM service_request;
DELETE FROM equipment;

-- Réactiver les contraintes
SET REFERENTIAL_INTEGRITY TRUE;

-- Reset des séquences
ALTER TABLE equipment ALTER COLUMN id RESTART WITH 1;

-- =====================================================
-- NOTE: L'import des données CSV se fait via le backend
-- Utiliser l'endpoint POST /api/equipment/import-locmat
-- =====================================================
