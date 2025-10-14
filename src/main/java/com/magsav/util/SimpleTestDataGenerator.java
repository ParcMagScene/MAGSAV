package com.magsav.util;

import com.magsav.db.DB;
import com.magsav.model.User;
import com.magsav.model.Vehicule;
import com.magsav.model.Vehicule.TypeVehicule;
import com.magsav.model.Vehicule.StatutVehicule;
import com.magsav.repo.*;

/**
 * Générateur simplifié de données de test
 */
public class SimpleTestDataGenerator {
    
    public static void generateTestData() {
        generateTestData(false);
    }
    
    public static void generateTestData(boolean force) {
        try {
            // Vérifier d'abord s'il y a déjà des données (sauf si forcé)
            if (!force && hasExistingData()) {
                System.out.println("⚠️ Données existantes détectées - génération ignorée");
                return;
            }
            
            System.out.println("=== Génération de données de test ===");
            
            // 1. Générer les catégories
            generateCategories();
            
            // 2. Générer les sociétés
            generateCompanies();
            
            // 3. Générer les utilisateurs
            generateUsers();
            
            // 4. Générer les produits
            generateProducts();
            
            // 5. Générer les véhicules
            generateVehicules();
            
            // 6. Générer les interventions
            generateInterventions();
            
            System.out.println("✅ Données de test générées avec succès !");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Vérifie s'il y a déjà des données dans la base
     */
    private static boolean hasExistingData() {
        try (java.sql.Connection conn = DB.getConnection()) {
            // Vérifier s'il y a des catégories
            java.sql.Statement stmt = conn.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM categories");
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
            
            // Vérifier s'il y a des sociétés
            rs = stmt.executeQuery("SELECT COUNT(*) FROM societes");
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
            
            // Vérifier s'il y a des produits
            rs = stmt.executeQuery("SELECT COUNT(*) FROM produits");
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
            
            // Vérifier s'il y a des véhicules
            rs = stmt.executeQuery("SELECT COUNT(*) FROM vehicules");
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
            
            // Vérifier s'il y a des interventions
            rs = stmt.executeQuery("SELECT COUNT(*) FROM interventions");
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification des données existantes: " + e.getMessage());
            return true; // En cas d'erreur, on suppose qu'il y a des données pour éviter les doublons
        }
    }
    
    private static void generateCategories() {
        System.out.println("📁 Génération des catégories...");
        try {
            CategoryRepository categoryRepo = new CategoryRepository();
            
            // Catégories principales
            long audiovisuelId = categoryRepo.insert("Audiovisuel", null);
            long informatiqueId = categoryRepo.insert("Informatique", null);
            long eclairageId = categoryRepo.insert("Éclairage", null);
            
            // Sous-catégories Audiovisuel
            long camerasId = categoryRepo.insert("Caméras", audiovisuelId);
            long microsId = categoryRepo.insert("Microphones", audiovisuelId);
            categoryRepo.insert("Enceintes", audiovisuelId);
            categoryRepo.insert("Projecteurs", audiovisuelId);
            
            // Sous-catégories Informatique
            long ordinateursId = categoryRepo.insert("Ordinateurs", informatiqueId);
            categoryRepo.insert("Tablettes", informatiqueId);
            categoryRepo.insert("Réseaux", informatiqueId);
            
            // Sous-catégories Éclairage
            categoryRepo.insert("Panneaux LED", eclairageId);
            categoryRepo.insert("Projecteurs LED", eclairageId);
            categoryRepo.insert("Consoles d'éclairage", eclairageId);
            
            // Sous-sous-catégories
            categoryRepo.insert("Caméras PTZ", camerasId);
            categoryRepo.insert("Caméras fixes", camerasId);
            categoryRepo.insert("Micros-cravates", microsId);
            categoryRepo.insert("Micros de plateau", microsId);
            categoryRepo.insert("Portables", ordinateursId);
            categoryRepo.insert("Fixes", ordinateursId);
            
            System.out.println("   ✓ Catégories créées");
            
        } catch (Exception e) {
            System.err.println("   ❌ Erreur catégories: " + e.getMessage());
        }
    }
    
    private static void generateCompanies() {
        System.out.println("🏢 Génération des sociétés...");
        try {
            SocieteRepository societeRepo = new SocieteRepository();
            
            // Fabricants (si pas déjà existants)
            societeRepo.insert("MANUFACTURER", "Sony Corporation", "contact@sony.fr", "01.23.45.67.89", "Tokyo, Japon", "Fabricant équipements audiovisuels");
            societeRepo.insert("MANUFACTURER", "Apple Inc.", "contact@apple.com", "01.34.56.78.90", "Cupertino, États-Unis", "Fabricant ordinateurs et tablettes");
            societeRepo.insert("MANUFACTURER", "Panasonic Corporation", "contact@panasonic.com", "01.45.67.89.01", "Osaka, Japon", "Fabricant équipements électroniques");
            
            // Fournisseurs
            societeRepo.insert("SUPPLIER", "TechDistrib", "contact@techdistrib.fr", "01.56.78.90.12", "Paris, France", "Distributeur équipements techniques");
            societeRepo.insert("SUPPLIER", "AudioPro", "contact@audiopro.fr", "01.67.89.01.23", "Lyon, France", "Fournisseur matériel audio");
            
            // Clients
            societeRepo.insert("CLIENT", "Théâtre Municipal", "contact@theatre-municipal.fr", "01.78.90.12.34", "Marseille, France", "Théâtre municipal");
            societeRepo.insert("CLIENT", "Centre Culturel", "contact@centre-culturel.fr", "01.89.01.23.45", "Toulouse, France", "Centre culturel et événementiel");
            
            
            System.out.println("   ✓ Sociétés créées");
            
        } catch (Exception e) {
            System.err.println("   ❌ Erreur sociétés: " + e.getMessage());
        }
    }

    private static void generateUsers() {
        System.out.println("👥 Génération des utilisateurs...");
        try {
            UserRepository userRepo = new UserRepository();
            
            // Récupérer l'ID de Mag Scene (societe ID 1)
            long magSceneId = 1;
            
            // Créer des utilisateurs de test avec les bons objets User (utilisation de hash simple pour les tests)
            User admin = new User(null, "admin", "admin@magscene.fr", 
                "admin123", // Password en dur pour les tests
                User.Role.ADMIN, "Administrateur Système", "01.02.03.04.05", 
                magSceneId, "Responsable IT", null, true, null, null, null, null);
            userRepo.createUser(admin);
            
            User tech1 = new User(null, "pierre", "pierre.martin@magscene.fr", 
                "tech123", // Password en dur pour les tests
                User.Role.TECHNICIEN_MAG_SCENE, "Pierre Martin", "01.12.23.34.45", 
                magSceneId, "Technicien Audio", null, true, null, null, null, null);
            userRepo.createUser(tech1);
            
            User tech2 = new User(null, "marie", "marie.dubois@magscene.fr", 
                "tech123", // Password en dur pour les tests
                User.Role.TECHNICIEN_MAG_SCENE, "Marie Dubois", "01.23.34.45.56", 
                magSceneId, "Technicienne Éclairage", null, true, null, null, null, null);
            userRepo.createUser(tech2);
            
            System.out.println("   ✓ Utilisateurs créés");
            
        } catch (Exception e) {
            System.err.println("   ❌ Erreur utilisateurs: " + e.getMessage());
        }
    }
    
    private static void generateProducts() {
        System.out.println("📦 Génération des produits...");
        try {
            ProductRepository productRepo = new ProductRepository();
            
            String[][] productsData = {
                {"Caméra Sony FX6", "SN001", "Sony", "UID001", "En stock"},
                {"MacBook Pro 16\"", "SN002", "Apple", "UID002", "En stock"},
                {"Console Yamaha CL5", "SN004", "Yamaha", "UID004", "En stock"},
                {"iPad Pro 12.9\"", "SN005", "Apple", "UID005", "En service"},
                {"Caméra Sony A7S III", "SN006", "Sony", "UID006", "En stock"},
                {"Enceinte Yamaha DXR15", "SN007", "Yamaha", "UID007", "En service"},
                {"Micro Shure SM58", "SN010", "Shure", "UID010", "En stock"}
            };
            
            for (String[] productData : productsData) {
                try {
                    productRepo.insert(
                        productData[0], // nom
                        productData[1], // sn
                        productData[2], // fabricant
                        productData[3], // uid
                        productData[4]  // situation
                    );
                } catch (Exception e) {
                    System.err.println("   ⚠️ Erreur produit " + productData[0] + ": " + e.getMessage());
                }
            }
            
            System.out.println("   ✓ Produits créés");
            
        } catch (Exception e) {
            System.err.println("   ❌ Erreur produits: " + e.getMessage());
        }
    }
    
    private static void generateVehicules() {
        System.out.println("🚗 Génération des véhicules...");
        try {
            VehiculeRepository vehiculeRepo = new VehiculeRepository();
            
            // Véhicules légers
            Vehicule vl1 = new Vehicule();
            vl1.setImmatriculation("AB-123-CD");
            vl1.setTypeVehicule(TypeVehicule.VL);
            vl1.setMarque("Peugeot");
            vl1.setModele("Partner");
            vl1.setAnnee(2020);
            vl1.setKilometrage(45000);
            vl1.setStatut(StatutVehicule.DISPONIBLE);
            vl1.setNotes("Véhicule de service pour interventions");
            vehiculeRepo.save(vl1);
            
            Vehicule vl2 = new Vehicule();
            vl2.setImmatriculation("EF-456-GH");
            vl2.setTypeVehicule(TypeVehicule.VL);
            vl2.setMarque("Renault");
            vl2.setModele("Kangoo");
            vl2.setAnnee(2019);
            vl2.setKilometrage(62000);
            vl2.setStatut(StatutVehicule.EN_SERVICE);
            vl2.setNotes("Équipé pour transport matériel audiovisuel");
            vehiculeRepo.save(vl2);
            
            // Poids lourds
            Vehicule pl1 = new Vehicule();
            pl1.setImmatriculation("IJ-789-KL");
            pl1.setTypeVehicule(TypeVehicule.PL);
            pl1.setMarque("Mercedes");
            pl1.setModele("Sprinter");
            pl1.setAnnee(2018);
            pl1.setKilometrage(120000);
            pl1.setStatut(StatutVehicule.DISPONIBLE);
            pl1.setNotes("Véhicule principal pour gros matériel");
            vehiculeRepo.save(pl1);
            
            // Scène mobile
            Vehicule scene = new Vehicule();
            scene.setImmatriculation("MN-012-OP");
            scene.setTypeVehicule(TypeVehicule.SCENE_MOBILE);
            scene.setMarque("Iveco");
            scene.setModele("Daily");
            scene.setAnnee(2021);
            scene.setKilometrage(25000);
            scene.setStatut(StatutVehicule.DISPONIBLE);
            scene.setNotes("Scène mobile pour événements extérieurs");
            vehiculeRepo.save(scene);
            
            // Remorque
            Vehicule remorque = new Vehicule();
            remorque.setImmatriculation("QR-345-ST");
            remorque.setTypeVehicule(TypeVehicule.REMORQUE);
            remorque.setMarque("Hapert");
            remorque.setModele("Azure H-2");
            remorque.setAnnee(2020);
            remorque.setKilometrage(0);
            remorque.setStatut(StatutVehicule.MAINTENANCE);
            remorque.setNotes("Remorque fermée pour transport sécurisé");
            vehiculeRepo.save(remorque);
            
            System.out.println("   ✓ Véhicules créés");
            
        } catch (Exception e) {
            System.err.println("   ❌ Erreur véhicules: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void generateInterventions() {
        System.out.println("🔧 Génération des interventions...");
        try {
            InterventionRepository interventionRepo = new InterventionRepository();
            
            // Requête pour récupérer les IDs des produits créés
            try (java.sql.Connection conn = DB.getConnection()) {
                String sql = "SELECT id, nom_produit FROM produits LIMIT 5";
                java.sql.Statement stmt = conn.createStatement();
                java.sql.ResultSet rs = stmt.executeQuery(sql);
                
                int interventionCount = 0;
                while (rs.next() && interventionCount < 5) {
                    long productId = rs.getLong("id");
                    String productName = rs.getString("nom_produit");
                    
                    // Créer une intervention pour ce produit
                    String[] descriptions = {
                        "Écran LCD défaillant, affichage déformé",
                        "Connecteur audio endommagé, pas de signal",
                        "Problème d'alimentation, arrêt intempestif",
                        "Télécommande ne répond plus",
                        "Ventilation bruyante, surchauffe constatée"
                    };
                    
                    String[] clientNotes = {
                        "Problème survenu lors d'un événement important",
                        "Matériel utilisé intensivement ces derniers mois",
                        "Panne subite, aucun signe avant-coureur",
                        "Problème récurrent depuis quelques semaines",
                        "Matériel tombé en panne en pleine utilisation"
                    };
                    
                    long interventionId = interventionRepo.insert(
                        productId,
                        "TEST-" + String.format("%03d", interventionCount + 1),
                        clientNotes[interventionCount],
                        descriptions[interventionCount]
                    );
                    
                    System.out.println("   ✓ Intervention créée pour " + productName + " (ID: " + interventionId + ")");
                    interventionCount++;
                }
            }
            
            System.out.println("   ✓ Interventions créées");
            
        } catch (Exception e) {
            System.err.println("   ❌ Erreur interventions: " + e.getMessage());
            e.printStackTrace();
        }
    }
}