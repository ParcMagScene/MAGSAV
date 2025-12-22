-- Script pour corriger la contrainte de type de véhicule
-- Exécuter via la console H2 sur http://localhost:8080/h2-console
-- URL: jdbc:h2:file:~/magsav/data/magsav
-- User: sa / Password: password

-- Suppression de la contrainte existante sur la colonne type
ALTER TABLE vehicles DROP CONSTRAINT IF EXISTS "CONSTRAINT_3D";
ALTER TABLE vehicles DROP CONSTRAINT IF EXISTS VEHICLES_TYPE_CHECK;

-- Recréation de la contrainte avec tous les types valides
ALTER TABLE vehicles ADD CONSTRAINT vehicles_type_check CHECK (
    type IN ('VAN', 'VL', 'VL_17M3', 'VL_20M3', 'TRUCK', 'PORTEUR', 'TRACTEUR', 'SEMI_REMORQUE', 'SCENE_MOBILE', 'TRAILER', 'CAR', 'MOTORCYCLE', 'OTHER')
);

-- Vérification
SELECT CONSTRAINT_NAME, CHECK_EXPRESSION FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
WHERE TABLE_NAME = 'VEHICLES' AND CONSTRAINT_TYPE = 'CHECK';
