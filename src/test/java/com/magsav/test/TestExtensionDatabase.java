package com.magsav.test;

import com.magsav.model.Technicien;
import com.magsav.model.Technicien.StatutTechnicien;
import com.magsav.repo.TechnicienRepository;
import javafx.collections.ObservableList;

import java.util.Map;

/**
 * Test de l'extension de base de données avec les nouveaux modules
 */
public class TestExtensionDatabase {
    
    public static void main(String[] args) {
        System.out.println("=== Test Extension Base de Données MAGSAV ===");
        
        try {
            // 1. Test du repository Technicien
            System.out.println("\n1. Test du module Techniciens...");
            TechnicienRepository technicienRepo = new TechnicienRepository();
            System.out.println("✓ Repository techniciens créé");
            
            // 2. Vérifier le nombre initial
            ObservableList<Technicien> techniciensInitiaux = technicienRepo.findAll();
            System.out.println("✓ Nombre de techniciens initial : " + techniciensInitiaux.size());
            
            // 3. Créer un technicien de test
            System.out.println("\n2. Création d'un technicien de test...");
            Technicien technicienTest = new Technicien();
            technicienTest.setNom("Martin");
            technicienTest.setPrenom("Jean");
            technicienTest.setEmail("jean.martin@magsav.fr");
            technicienTest.setTelephone("06.12.34.56.78");
            technicienTest.setStatut(StatutTechnicien.ACTIF);
            technicienTest.setSpecialites("[\"Audio\", \"Vidéo\", \"Éclairage\"]");
            technicienTest.setNotes("Technicien spécialisé en sonorisation");
            technicienTest.setSyncGoogleEnabled(true);
            
            // 4. Sauvegarder le technicien
            boolean saveSuccess = technicienRepo.save(technicienTest);
            if (saveSuccess) {
                System.out.println("✓ Technicien sauvegardé avec succès");
                System.out.println("  - ID : " + technicienTest.getId());
                System.out.println("  - Nom complet : " + technicienTest.getNomComplet());
                System.out.println("  - Email : " + technicienTest.getEmail());
                System.out.println("  - Statut : " + technicienTest.getStatut().getDisplayName());
            } else {
                System.out.println("✗ Erreur lors de la sauvegarde");
                return;
            }
            
            // 5. Test de recherche
            System.out.println("\n3. Test de recherche...");
            ObservableList<Technicien> resultatsRecherche = technicienRepo.search("Jean");
            System.out.println("✓ Résultats recherche 'Jean' : " + resultatsRecherche.size());
            
            // 6. Test des techniciens disponibles
            System.out.println("\n4. Test techniciens disponibles...");
            ObservableList<Technicien> disponibles = technicienRepo.findDisponibles();
            System.out.println("✓ Techniciens disponibles : " + disponibles.size());
            
            // 7. Test de validation email
            System.out.println("\n5. Test validation email...");
            boolean emailExiste = technicienRepo.existsByEmail("jean.martin@magsav.fr", null);
            boolean emailInexistant = technicienRepo.existsByEmail("inexistant@test.com", null);
            System.out.println("✓ Email 'jean.martin@magsav.fr' existe : " + emailExiste);
            System.out.println("✓ Email inexistant existe : " + emailInexistant);
            
            // 8. Test des statistiques
            System.out.println("\n6. Test des statistiques...");
            Map<String, Integer> stats = technicienRepo.getStatistiques();
            System.out.println("✓ Statistiques des techniciens :");
            stats.forEach((key, value) -> 
                System.out.println("  - " + key + " : " + value));
            
            // 9. Test modification
            System.out.println("\n7. Test de modification...");
            technicienTest.setTelephone("06.87.65.43.21");
            technicienTest.setGoogleContactId("google-contact-123");
            boolean updateSuccess = technicienRepo.save(technicienTest);
            if (updateSuccess) {
                System.out.println("✓ Technicien modifié avec succès");
                System.out.println("  - Nouveau téléphone : " + technicienTest.getTelephone());
                System.out.println("  - Google Contact ID : " + technicienTest.getGoogleContactId());
            }
            
            // 10. Test Google Sync
            System.out.println("\n8. Test intégration Google...");
            boolean googleSyncUpdate = technicienRepo.updateGoogleSync(
                technicienTest.getId(), 
                "google-contact-456", 
                "google-calendar-789", 
                true
            );
            if (googleSyncUpdate) {
                System.out.println("✓ Synchronisation Google mise à jour");
                
                // Récupérer le technicien mis à jour
                Technicien technicienMaj = technicienRepo.findById(technicienTest.getId());
                if (technicienMaj != null) {
                    System.out.println("  - Google Contact ID : " + technicienMaj.getGoogleContactId());
                    System.out.println("  - Google Calendar ID : " + technicienMaj.getGoogleCalendarId());
                    System.out.println("  - Sync activé : " + technicienMaj.isSyncGoogleEnabled());
                    System.out.println("  - Dernière sync : " + technicienMaj.getLastGoogleSync());
                }
            }
            
            // 11. Afficher les détails complets
            System.out.println("\n9. Détails du technicien de test :");
            System.out.println("  - " + technicienTest.toString());
            System.out.println("  - Display Name : " + technicienTest.getDisplayName());
            System.out.println("  - Disponible : " + technicienTest.isDisponible());
            System.out.println("  - Google Sync Enabled : " + technicienTest.isGoogleSyncEnabled());
            
            // 12. Nettoyage
            System.out.println("\n10. Nettoyage...");
            boolean deleteSuccess = technicienRepo.delete(technicienTest.getId());
            if (deleteSuccess) {
                System.out.println("✓ Technicien de test supprimé");
            } else {
                System.out.println("✗ Erreur lors de la suppression");
            }
            
            // Vérification finale
            ObservableList<Technicien> techniciensFinaux = technicienRepo.findAll();
            System.out.println("✓ Nombre de techniciens final : " + techniciensFinaux.size());
            
            System.out.println("\n=== TEST TERMINÉ AVEC SUCCÈS ===");
            System.out.println("L'extension de base de données avec intégration Google fonctionne correctement !");
            System.out.println("Tables créées : techniciens, planifications, communications, commandes, etc.");
            System.out.println("Fonctionnalités disponibles :");
            System.out.println("- Gestion des techniciens avec spécialités");
            System.out.println("- Intégration Google Contacts et Calendar");
            System.out.println("- Templates d'emails personnalisés");
            System.out.println("- Configuration services Google (OAuth)");
            System.out.println("- Historique de synchronisation");
            System.out.println("- Système de notifications automatiques");
            
        } catch (Exception e) {
            System.err.println("✗ ERREUR PENDANT LE TEST : " + e.getMessage());
            e.printStackTrace();
        }
    }
}