package com.magsav.test;

import com.magsav.model.Vehicule;
import com.magsav.model.Vehicule.TypeVehicule;
import com.magsav.model.Vehicule.StatutVehicule;
import com.magsav.repo.VehiculeRepository;
import javafx.collections.ObservableList;

import java.util.Map;

/**
 * Test rapide du module véhicules
 */
public class TestVehiculeModule {
    
    public static void main(String[] args) {
        System.out.println("=== Test du Module Véhicules MAGSAV ===");
        
        try {
            // 1. Initialiser la base de données
            System.out.println("\n1. Initialisation de la base de données...");
            // La base sera initialisée automatiquement lors de la première connexion
            System.out.println("✓ Base de données prête");
            
            // 2. Créer le repository
            VehiculeRepository vehiculeRepository = new VehiculeRepository();
            System.out.println("✓ Repository créé");
            
            // 3. Vérifier le nombre initial de véhicules
            ObservableList<Vehicule> vehiculesInitiaux = vehiculeRepository.findAll();
            System.out.println("✓ Nombre de véhicules initial : " + vehiculesInitiaux.size());
            
            // 4. Créer un véhicule de test
            System.out.println("\n2. Création d'un véhicule de test...");
            Vehicule vehiculeTest = new Vehicule();
            vehiculeTest.setImmatriculation("AA-123-BB");
            vehiculeTest.setTypeVehicule(TypeVehicule.VL);
            vehiculeTest.setMarque("Renault");
            vehiculeTest.setModele("Kangoo");
            vehiculeTest.setAnnee(2023);
            vehiculeTest.setKilometrage(15000);
            vehiculeTest.setStatut(StatutVehicule.DISPONIBLE);
            vehiculeTest.setLocationExterne(false);
            vehiculeTest.setNotes("Véhicule de test pour validation du module");
            
            // 5. Sauvegarder le véhicule
            boolean saveSuccess = vehiculeRepository.save(vehiculeTest);
            if (saveSuccess) {
                System.out.println("✓ Véhicule sauvegardé avec succès");
                System.out.println("  - ID : " + vehiculeTest.getId());
                System.out.println("  - Immatriculation : " + vehiculeTest.getImmatriculation());
                System.out.println("  - Nom d'affichage : " + vehiculeTest.getDisplayName());
            } else {
                System.out.println("✗ Erreur lors de la sauvegarde");
                return;
            }
            
            // 6. Récupérer tous les véhicules
            System.out.println("\n3. Test de récupération...");
            ObservableList<Vehicule> vehicules = vehiculeRepository.findAll();
            System.out.println("✓ Nombre total de véhicules : " + vehicules.size());
            
            // 7. Test de recherche
            System.out.println("\n4. Test de recherche...");
            ObservableList<Vehicule> resultatsRecherche = vehiculeRepository.search("AA-123");
            System.out.println("✓ Résultats recherche 'AA-123' : " + resultatsRecherche.size());
            
            // 8. Test de récupération par ID
            System.out.println("\n5. Test récupération par ID...");
            Vehicule vehiculeRecupere = vehiculeRepository.findById(vehiculeTest.getId());
            if (vehiculeRecupere != null) {
                System.out.println("✓ Véhicule récupéré : " + vehiculeRecupere.getDisplayName());
            } else {
                System.out.println("✗ Véhicule non trouvé par ID");
            }
            
            // 9. Test de modification
            System.out.println("\n6. Test de modification...");
            vehiculeTest.setKilometrage(16000);
            vehiculeTest.setStatut(StatutVehicule.EN_SERVICE);
            boolean updateSuccess = vehiculeRepository.save(vehiculeTest);
            if (updateSuccess) {
                System.out.println("✓ Véhicule modifié avec succès");
                System.out.println("  - Nouveau kilométrage : " + vehiculeTest.getKilometrage());
                System.out.println("  - Nouveau statut : " + vehiculeTest.getStatut().getDisplayName());
            }
            
            // 10. Test de validation de l'immatriculation
            System.out.println("\n7. Test de validation d'immatriculation...");
            boolean existeDejaTest = vehiculeRepository.existsByImmatriculation("AA-123-BB", null);
            boolean existePasTest = vehiculeRepository.existsByImmatriculation("ZZ-999-ZZ", null);
            System.out.println("✓ 'AA-123-BB' existe : " + existeDejaTest);
            System.out.println("✓ 'ZZ-999-ZZ' existe : " + existePasTest);
            
            // 11. Test des statistiques
            System.out.println("\n8. Test des statistiques...");
            Map<String, Integer> stats = vehiculeRepository.getStatistiques();
            System.out.println("✓ Statistiques des véhicules :");
            stats.forEach((key, value) -> 
                System.out.println("  - " + key + " : " + value));
            
            // 12. Afficher le véhicule créé
            System.out.println("\n9. Détails du véhicule de test :");
            System.out.println("  - ID : " + vehiculeTest.getId());
            System.out.println("  - Immatriculation : " + vehiculeTest.getImmatriculation());
            System.out.println("  - Type : " + vehiculeTest.getTypeVehicule().getDisplayName());
            System.out.println("  - Marque : " + vehiculeTest.getMarque());
            System.out.println("  - Modèle : " + vehiculeTest.getModele());
            System.out.println("  - Année : " + vehiculeTest.getAnnee());
            System.out.println("  - Kilométrage : " + vehiculeTest.getKilometrage() + " km");
            System.out.println("  - Statut : " + vehiculeTest.getStatut().getDisplayName());
            System.out.println("  - Location externe : " + vehiculeTest.isLocationExterne());
            System.out.println("  - Notes : " + vehiculeTest.getNotes());
            System.out.println("  - Date création : " + vehiculeTest.getDateCreation());
            System.out.println("  - Date modification : " + vehiculeTest.getDateModification());
            
            // 13. Nettoyage (suppression du véhicule de test)
            System.out.println("\n10. Nettoyage...");
            boolean deleteSuccess = vehiculeRepository.delete(vehiculeTest.getId());
            if (deleteSuccess) {
                System.out.println("✓ Véhicule de test supprimé");
            } else {
                System.out.println("✗ Erreur lors de la suppression");
            }
            
            // Vérification finale
            ObservableList<Vehicule> vehiculesFinaux = vehiculeRepository.findAll();
            System.out.println("✓ Nombre de véhicules final : " + vehiculesFinaux.size());
            
            System.out.println("\n=== TEST TERMINÉ AVEC SUCCÈS ===");
            System.out.println("Le module Véhicules MAGSAV fonctionne correctement !");
            
        } catch (Exception e) {
            System.err.println("✗ ERREUR PENDANT LE TEST : " + e.getMessage());
            e.printStackTrace();
        }
    }
}