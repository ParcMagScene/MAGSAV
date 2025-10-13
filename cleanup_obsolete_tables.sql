-- Analyse des tables utilisées dans l'application MAGSAV
-- Tables identifiées comme UTILISÉES dans le code :
-- 1. produits - Massivement utilisé (ProductRepository)
-- 2. societes - Utilisé (SocieteRepository) 
-- 3. requests - Utilisé (RequestRepository)
-- 4. request_items - Utilisé (RequestRepository)
-- 5. categories - Utilisé (CategoryRepository)
-- 6. interventions - Utilisé (InterventionRepository)
-- 7. email_templates - Utilisé (DB.java)
-- 8. configuration_google - Utilisé (GoogleServicesConfigRepository)
-- 9. sav_history - Utilisé (SavHistoryRepository)
-- 10. techniciens - Utilisé (TechnicienRepository)
-- 11. vehicules - Utilisé (VehiculeRepository)
-- 12. planifications - Utilisé (PlanificationRepository)
-- 13. commandes - Utilisé (CommandeRepository)
-- 14. lignes_commandes - Utilisé (CommandeRepository)
-- 15. clients - Utilisé (ClientRepository)
-- 16. users - Utilisé (UserRepository)
-- 17. entities - Utilisé (SimpleEntityRepository)
-- 18. demandes_intervention - Utilisé (DemandeInterventionRepository)
-- 19. demandes_creation_proprietaire - Utilisé (DemandeCreationProprietaireRepository)
-- 20. demandes_elevation_privilege - Utilisé (DemandeElevationPrivilegeRepository)
-- 21. schema_version - Utilisé (MigrationRunner)

-- Tables identifiées comme OBSOLÈTES/INUTILISÉES :
-- 1. companies - Remplacée par 'societes' (migration effectuée)
-- 2. alertes_stock - Référencée dans TestDataGenerator mais pas d'usage actif
-- 3. mouvements_stock - Référencée dans TestDataGenerator mais pas d'usage actif
-- 4. disponibilites_techniciens - Référencée dans TestDataGenerator mais pas d'usage actif
-- 5. communications - Référencée dans TestDataGenerator mais pas d'usage actif
-- 6. sync_history - Aucune référence trouvée dans le code actuel

-- Verification finale avant suppression
SELECT 'Tables présentes dans la base:' as info;
.tables

-- Vérification du contenu des tables potentiellement obsolètes
SELECT 'Table companies (à supprimer):' as info, COUNT(*) as count FROM companies;
SELECT 'Table alertes_stock:' as info, COUNT(*) as count FROM alertes_stock;
SELECT 'Table mouvements_stock:' as info, COUNT(*) as count FROM mouvements_stock;
SELECT 'Table disponibilites_techniciens:' as info, COUNT(*) as count FROM disponibilites_techniciens;
SELECT 'Table communications:' as info, COUNT(*) as count FROM communications;
SELECT 'Table sync_history (si existe):' as info, COUNT(*) as count FROM sync_history WHERE 1=0 OR EXISTS(SELECT name FROM sqlite_master WHERE type='table' AND name='sync_history');

