package com.magsav.util;

import com.magsav.db.DB;
import com.magsav.model.Company;
import com.magsav.model.InterventionRow;
import com.magsav.repo.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Random;

/**
 * Générateur complet de données de test avec gestion des doublons
 */
public class ComprehensiveTestDataGenerator {
    
    private static final Random random = new Random();
    
    public static void main(String[] args) {
        generateCompleteTestData();
    }
    
    public static void generateCompleteTestData() {
        try {
            System.out.println("=== GÉNÉRATION COMPLÈTE DE DONNÉES DE TEST ===");
            
            // 1. Nettoyer les doublons
            System.out.println("🧹 Nettoyage des doublons...");
            cleanDuplicates();
            
            // 2. Générer les données manquantes
            System.out.println("📊 Génération des données...");
            generateCategories();
            generateCompanies();
            generateProducts();
            generateInterventions();
            
            // 3. Afficher un résumé
            printDataSummary();
            
            System.out.println("✅ Génération complète terminée !");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Nettoie les doublons dans toutes les tables
     */
    private static void cleanDuplicates() {
        try (Connection conn = DB.getConnection()) {
            
            // Supprimer les doublons de sociétés (même nom et type)
            String cleanCompanies = """
                DELETE FROM companies 
                WHERE rowid NOT IN (
                    SELECT MIN(rowid) 
                    FROM companies 
                    GROUP BY name, type
                )
            """;
            
            // Supprimer les doublons de produits (même nom et fabricant)
            String cleanProducts = """
                DELETE FROM produits 
                WHERE id NOT IN (
                    SELECT MIN(id) 
                    FROM produits 
                    GROUP BY nom, fabricant
                )
            """;
            
            // Supprimer les doublons de catégories (même nom)
            String cleanCategories = """
                DELETE FROM categories 
                WHERE id NOT IN (
                    SELECT MIN(id) 
                    FROM categories 
                    GROUP BY nom
                )
            """;
            
            Statement stmt = conn.createStatement();
            
            int companiesCleaned = stmt.executeUpdate(cleanCompanies);
            System.out.println("   🏢 Sociétés doublons supprimées: " + companiesCleaned);
            
            int productsCleaned = stmt.executeUpdate(cleanProducts);
            System.out.println("   📦 Produits doublons supprimés: " + productsCleaned);
            
            int categoriesCleaned = stmt.executeUpdate(cleanCategories);
            System.out.println("   📁 Catégories doublons supprimées: " + categoriesCleaned);
            
        } catch (Exception e) {
            System.err.println("   ❌ Erreur nettoyage: " + e.getMessage());
        }
    }
    
    /**
     * Génère les catégories si elles n'existent pas
     */
    private static void generateCategories() {
        System.out.println("📁 Vérification des catégories...");
        try {
            CategoryRepository categoryRepo = new CategoryRepository();
            
            // Vérifier si des catégories existent déjà
            try (Connection conn = DB.getConnection()) {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM categories");
                if (rs.next() && rs.getInt("count") > 10) {
                    System.out.println("   ✓ Catégories déjà présentes");
                    return;
                }
            }
            
            // Générer les catégories directement ici
            String[][] categories = {
                {"Audiovisuel", null},
                {"Informatique", null},
                {"Éclairage", null},
                {"Caméras", "1"},
                {"Microphones", "1"},
                {"Enceintes", "1"},
                {"Projecteurs", "1"},
                {"Ordinateurs", "2"},
                {"Tablettes", "2"},
                {"Réseaux", "2"},
                {"Panneaux LED", "3"},
                {"Projecteurs LED", "3"},
                {"Consoles d'éclairage", "3"}
            };
            
            for (String[] cat : categories) {
                try {
                    Long parentId = cat[1] != null ? Long.parseLong(cat[1]) : null;
                    categoryRepo.insert(cat[0], parentId);
                } catch (Exception e) {
                    // Catégorie existe déjà, continuer
                }
            }
            System.out.println("   ✓ Catégories créées");
            
        } catch (Exception e) {
            System.err.println("   ❌ Erreur catégories: " + e.getMessage());
        }
    }
    
    /**
     * Génère les sociétés avec plus de diversité
     */
    private static void generateCompanies() {
        System.out.println("🏢 Génération des sociétés...");
        try {
            CompanyRepository companyRepo = new CompanyRepository(DB.getConnection());
            
            // Vérifier si assez de sociétés existent
            try (Connection conn = DB.getConnection()) {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM companies");
                if (rs.next() && rs.getInt("count") > 20) {
                    System.out.println("   ✓ Sociétés déjà présentes en nombre suffisant");
                    return;
                }
            }
            
            // Fabricants supplémentaires
            String[][] manufacturers = {
                {"Sony", "https://www.sony.com", "Tokyo", "Électronique"},
                {"Canon", "https://www.canon.com", "Tokyo", "Optique"},
                {"Panasonic", "https://www.panasonic.com", "Osaka", "Électronique"},
                {"Blackmagic Design", "https://www.blackmagicdesign.com", "Melbourne", "Vidéo"},
                {"Audio-Technica", "https://www.audio-technica.com", "Tokyo", "Audio"},
                {"Sennheiser", "https://www.sennheiser.com", "Wedemark", "Audio"},
                {"Rode", "https://www.rode.com", "Sydney", "Audio"},
                {"Aputure", "https://www.aputure.com", "Los Angeles", "Éclairage"}
            };
            
            for (String[] mfg : manufacturers) {
                try {
                    Company manufacturer = new Company(mfg[0], Company.CompanyType.MANUFACTURER);
                    manufacturer.setWebsite(mfg[1]);
                    manufacturer.setCity(mfg[2]);
                    manufacturer.setSector(mfg[3]);
                    companyRepo.save(manufacturer);
                } catch (Exception e) {
                    System.err.println("   ⚠️ Fabricant " + mfg[0] + " existe déjà");
                }
            }
            
            // Clients supplémentaires
            String[][] clients = {
                {"École Nationale de Musique", "CLIENT", "12 rue de la République", "Lyon", "04 78 28 37 28", "contact@enm-lyon.fr"},
                {"Salle Paul Bocuse", "CLIENT", "20 place Bellecour", "Lyon", "04 78 42 10 10", "events@bocuse-lyon.com"},
                {"Université Lyon 2", "ADMINISTRATION", "86 rue de Pasteur", "Lyon", "04 78 77 23 23", "audiovisuel@univ-lyon2.fr"},
                {"Musée des Confluences", "ADMINISTRATION", "86 quai Perrache", "Lyon", "04 72 69 05 05", "technique@museedesconfluences.fr"},
                {"Opéra de Lyon", "CLIENT", "1 place de la Comédie", "Lyon", "04 69 85 54 54", "technique@opera-lyon.com"},
                {"Palais des Congrès", "CLIENT", "50 quai Charles de Gaulle", "Lyon", "04 72 82 29 29", "technique@palaisdescongreslyon.com"}
            };
            
            for (String[] client : clients) {
                try {
                    Company.CompanyType type = client[1].equals("CLIENT") ? 
                        Company.CompanyType.CLIENT : Company.CompanyType.ADMINISTRATION;
                    Company company = new Company(client[0], type);
                    company.setAddress(client[2]);
                    company.setCity(client[3]);
                    company.setPhone(client[4]);
                    company.setEmail(client[5]);
                    companyRepo.save(company);
                } catch (Exception e) {
                    System.err.println("   ⚠️ Client " + client[0] + " existe déjà");
                }
            }
            
            System.out.println("   ✓ Sociétés étendues créées");
            
        } catch (Exception e) {
            System.err.println("   ❌ Erreur sociétés: " + e.getMessage());
        }
    }
    
    /**
     * Génère plus de produits variés
     */
    private static void generateProducts() {
        System.out.println("📦 Génération des produits...");
        try {
            ProductRepository productRepo = new ProductRepository();
            
            // Vérifier si assez de produits existent
            try (Connection conn = DB.getConnection()) {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM produits");
                if (rs.next() && rs.getInt("count") > 50) {
                    System.out.println("   ✓ Produits déjà présents en nombre suffisant");
                    return;
                }
            }
            
            // Produits supplémentaires avec plus de variété
            String[][] products = {
                // Caméras
                {"Sony FX9", "SN101", "Sony", "CAM001", "En stock"},
                {"Canon C300 Mark III", "SN102", "Canon", "CAM002", "En service"},
                {"Blackmagic URSA Mini Pro", "SN103", "Blackmagic Design", "CAM003", "En maintenance"},
                {"Panasonic GH6", "SN104", "Panasonic", "CAM004", "En stock"},
                
                // Audio
                {"Shure Beta 58A", "SN201", "Shure", "MIC001", "En stock"},
                {"Audio-Technica AT2020", "SN202", "Audio-Technica", "MIC002", "En service"},
                {"Rode PodMic", "SN203", "Rode", "MIC003", "En stock"},
                {"Sennheiser MKE 600", "SN204", "Sennheiser", "MIC004", "En maintenance"},
                
                // Éclairage
                {"Aputure 300d Mark II", "SN301", "Aputure", "LGT001", "En stock"},
                {"Aputure 120d Mark II", "SN302", "Aputure", "LGT002", "En service"},
                
                // Informatique
                {"iMac 27\" M1", "SN401", "Apple", "MAC001", "En stock"},
                {"MacBook Air M2", "SN402", "Apple", "MAC002", "En service"},
                {"iPad Pro 11\"", "SN403", "Apple", "TAB001", "En stock"},
                {"Surface Studio", "SN404", "Microsoft", "WIN001", "En maintenance"}
            };
            
            for (String[] product : products) {
                try {
                    productRepo.insert(product[0], product[1], product[2], product[3], product[4]);
                } catch (Exception e) {
                    System.err.println("   ⚠️ Produit " + product[0] + " existe déjà ou erreur: " + e.getMessage());
                }
            }
            
            System.out.println("   ✓ Produits étendus créés");
            
        } catch (Exception e) {
            System.err.println("   ❌ Erreur produits: " + e.getMessage());
        }
    }
    
    /**
     * Génère des interventions réalistes
     */
    private static void generateInterventions() {
        System.out.println("🔧 Génération des interventions...");
        try {
            InterventionRepository interventionRepo = new InterventionRepository();
            
            // Vérifier si des interventions existent
            List<InterventionRow> existingInterventions = interventionRepo.findAllWithProductName();
            if (existingInterventions.size() > 10) {
                System.out.println("   ✓ Interventions déjà présentes en nombre suffisant");
                return;
            }
            
            // Récupérer quelques produits et clients pour créer des interventions
            try (Connection conn = DB.getConnection()) {
                Statement stmt = conn.createStatement();
                
                // Récupérer des produits
                ResultSet productsRs = stmt.executeQuery("SELECT id, nom FROM produits LIMIT 10");
                
                // Récupérer des clients
                ResultSet clientsRs = stmt.executeQuery("SELECT id, name FROM companies WHERE type = 'CLIENT' LIMIT 5");
                java.util.List<Long> clientIds = new java.util.ArrayList<>();
                while (clientsRs.next()) {
                    clientIds.add(clientsRs.getLong("id"));
                }
                
                String[] pannes = {
                    "Écran défaillant",
                    "Problème de connectivité WiFi",
                    "Batterie ne charge plus",
                    "Objectif bloqué",
                    "Pas de son en sortie",
                    "Surchauffe du processeur",
                    "Boutons non fonctionnels",
                    "Écran tactile ne répond plus",
                    "Problème de mise au point",
                    "Carte mémoire non reconnue"
                };
                
                String[] statuts = {"En cours", "Terminée", "En attente pièces", "Devis envoyé"};
                
                int interventionCount = 0;
                productsRs = stmt.executeQuery("SELECT id, nom FROM produits LIMIT 10");
                
                while (productsRs.next() && interventionCount < 15) {
                    try {
                        Long productId = productsRs.getLong("id");
                        String productName = productsRs.getString("nom");
                        
                        String serialNumber = "SER" + String.format("%03d", interventionCount + 1);
                        String clientNote = "Intervention sur " + productName;
                        String defectDescription = pannes[random.nextInt(pannes.length)];
                        
                        interventionRepo.insert(productId, serialNumber, clientNote, defectDescription);
                        
                        // Mettre à jour avec un statut et un client si disponible
                        if (!clientIds.isEmpty()) {
                            Long clientId = clientIds.get(random.nextInt(clientIds.size()));
                            String statut = statuts[random.nextInt(statuts.length)];
                            
                            // Utiliser insertFromImport pour avoir plus d'options
                            interventionRepo.insertFromImport(
                                productId, statut, defectDescription, "Technicien",
                                "2024-0" + (random.nextInt(9) + 1) + "-" + String.format("%02d", random.nextInt(28) + 1),
                                statut.equals("Terminée") ? "2024-0" + (random.nextInt(9) + 1) + "-" + String.format("%02d", random.nextInt(28) + 1) : null,
                                "INT" + String.format("%03d", interventionCount + 1),
                                "CLIENT", clientId
                            );
                        }
                        
                        interventionCount++;
                        
                    } catch (Exception e) {
                        System.err.println("   ⚠️ Erreur intervention: " + e.getMessage());
                    }
                }
                
                System.out.println("   ✓ " + interventionCount + " interventions créées");
            }
            
        } catch (Exception e) {
            System.err.println("   ❌ Erreur interventions: " + e.getMessage());
        }
    }
    
    /**
     * Affiche un résumé des données dans la base
     */
    private static void printDataSummary() {
        System.out.println("\n📊 RÉSUMÉ DES DONNÉES :");
        try (Connection conn = DB.getConnection()) {
            Statement stmt = conn.createStatement();
            
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM companies");
            if (rs.next()) System.out.println("   🏢 Sociétés: " + rs.getInt("count"));
            
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM produits");
            if (rs.next()) System.out.println("   📦 Produits: " + rs.getInt("count"));
            
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM categories");
            if (rs.next()) System.out.println("   📁 Catégories: " + rs.getInt("count"));
            
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM interventions");
            if (rs.next()) System.out.println("   🔧 Interventions: " + rs.getInt("count"));
            
        } catch (Exception e) {
            System.err.println("   ❌ Erreur résumé: " + e.getMessage());
        }
    }
}