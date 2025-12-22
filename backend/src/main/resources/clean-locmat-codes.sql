-- Script de nettoyage des codes LOCMAT (suppression des caractères "*")
-- MAGSAV 3.0 - Exécuter une seule fois

-- Afficher les codes avant nettoyage (pour vérification)
SELECT id, name, internal_reference AS locmat_before 
FROM equipment 
WHERE internal_reference LIKE '%*%'
ORDER BY id;

-- Nettoyer les codes LOCMAT en supprimant les "*"
UPDATE equipment 
SET internal_reference = REPLACE(internal_reference, '*', '')
WHERE internal_reference LIKE '%*%';

-- Nettoyer aussi les espaces superflus
UPDATE equipment 
SET internal_reference = TRIM(internal_reference)
WHERE internal_reference IS NOT NULL;

-- Afficher les codes après nettoyage (pour vérification)
SELECT id, name, internal_reference AS locmat_after 
FROM equipment 
WHERE id IN (
    SELECT id FROM equipment 
    WHERE internal_reference IS NOT NULL
)
ORDER BY id;

-- Statistiques
SELECT 
    COUNT(*) AS total_equipments,
    COUNT(internal_reference) AS with_locmat,
    COUNT(DISTINCT internal_reference) AS unique_locmat_codes
FROM equipment;
