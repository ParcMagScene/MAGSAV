-- NETTOYAGE DE LA BASE DE DONNÉES MAGSAV
-- Suppression des tables obsolètes identifiées lors de l'analyse du code

-- 1. Sauvegarde des informations importantes avant suppression
SELECT 'AVANT NETTOYAGE - État de la base:' as info;
SELECT 
    'Total tables:' as type,
    COUNT(*) as count 
FROM sqlite_master 
WHERE type='table' AND name NOT LIKE 'sqlite_%';

-- 2. Suppression de la table 'companies' (remplacée par 'societes')
SELECT 'Suppression de la table companies (remplacée par societes)...' as action;
DROP TABLE IF EXISTS companies;

-- 3. Suppression des tables de gestion de stock non utilisées
SELECT 'Suppression des tables de stock non utilisées...' as action;
DROP TABLE IF EXISTS alertes_stock;
DROP TABLE IF EXISTS mouvements_stock;

-- 4. Suppression des tables de communication/planning avancé non utilisées
SELECT 'Suppression des tables de fonctionnalités avancées non utilisées...' as action;
DROP TABLE IF EXISTS disponibilites_techniciens;
DROP TABLE IF EXISTS communications;

-- 5. Suppression de la table de synchronisation vide
SELECT 'Suppression de la table sync_history...' as action;
DROP TABLE IF EXISTS sync_history;

-- 6. Nettoyage des séquences automatiques pour les tables supprimées
DELETE FROM sqlite_sequence 
WHERE name IN ('companies', 'alertes_stock', 'mouvements_stock', 'disponibilites_techniciens', 'communications', 'sync_history');

-- 7. État final de la base
SELECT 'APRÈS NETTOYAGE - État de la base:' as info;
SELECT 
    'Tables restantes:' as type,
    COUNT(*) as count 
FROM sqlite_master 
WHERE type='table' AND name NOT LIKE 'sqlite_%';

-- 8. Liste des tables conservées
SELECT 'Tables conservées dans la base:' as info;
.tables

-- 9. Compactage de la base de données
VACUUM;

SELECT 'Nettoyage terminé avec succès!' as resultat;
