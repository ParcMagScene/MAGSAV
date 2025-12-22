-- ========================================
-- Script de nettoyage des codes LOCMAT
-- Supprime les caractères '*' des références internes
-- ========================================

-- Nettoyage des '*' dans les codes LOCMAT (internal_reference)
UPDATE equipment 
SET internal_reference = TRIM(REPLACE(internal_reference, '*', '')) 
WHERE internal_reference IS NOT NULL 
AND internal_reference LIKE '%*%';

-- Log du nombre d'enregistrements nettoyés (pour debug uniquement)
-- SELECT COUNT(*) AS cleaned_records FROM equipment WHERE internal_reference NOT LIKE '%*%';
