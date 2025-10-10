package com.magsav.util;

import com.magsav.db.DB;
import com.magsav.model.Company;
import com.magsav.repo.*;

/**
 * Générateur simplifié de données de test
 */
public class SimpleTestDataGenerator {
    
    public static void generateTestData() {
        try {
            // Vérifier d'abord s'il y a déjà des données
            if (hasExistingData()) {
                System.out.println("⚠️ Données existantes détectées - génération ignorée");
                return;
            }
            
            System.out.println("=== Génération de données de test ===");
            
            // 1. Générer les catégories
            generateCategories();
            
            // 2. Générer les sociétés
            generateCompanies();
            
            // 3. Générer les produits
            generateProducts();
            
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
            
            // Vérifier s'il y a des companies
            rs = stmt.executeQuery("SELECT COUNT(*) FROM companies");
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
            
            // Vérifier s'il y a des produits
            rs = stmt.executeQuery("SELECT COUNT(*) FROM produits");
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
            CompanyRepository companyRepo = new CompanyRepository(DB.getConnection());
            
            // S'assurer que Mag Scène existe
            companyRepo.createDefaultMagScene();
            
            // Fabricants
            Company sony = new Company("Sony", Company.CompanyType.MANUFACTURER);
            sony.setWebsite("https://www.sony.fr");
            sony.setCountry("Japon");
            companyRepo.save(sony);
            
            Company apple = new Company("Apple", Company.CompanyType.MANUFACTURER);
            apple.setWebsite("https://www.apple.com");
            apple.setCountry("États-Unis");
            companyRepo.save(apple);
            
            Company yamaha = new Company("Yamaha", Company.CompanyType.MANUFACTURER);
            yamaha.setWebsite("https://www.yamaha.com");
            yamaha.setCountry("Japon");
            companyRepo.save(yamaha);
            
            // Fournisseurs
            Company ldlc = new Company("LDLC", Company.CompanyType.SUPPLIER);
            ldlc.setWebsite("https://www.ldlc.com");
            ldlc.setCity("Limonest");
            companyRepo.save(ldlc);
            
            // Clients
            Company mairie = new Company("Mairie de Lyon", Company.CompanyType.ADMINISTRATION);
            mairie.setCity("Lyon");
            companyRepo.save(mairie);
            
            Company clientA = new Company("Hôpital de la Croix-Rousse", Company.CompanyType.CLIENT);
            clientA.setAddress("103 Grande Rue de la Croix-Rousse");
            clientA.setCity("Lyon");
            clientA.setPhone("04 72 07 17 17");
            clientA.setEmail("contact@chu-lyon.fr");
            companyRepo.save(clientA);
            
            Company clientB = new Company("Théâtre des Célestins", Company.CompanyType.CLIENT);
            clientB.setAddress("4 rue Charles Dullin");
            clientB.setCity("Lyon");
            clientB.setPhone("04 72 77 40 00");
            clientB.setEmail("direction@celestins-lyon.org");
            companyRepo.save(clientB);
            
            Company clientC = new Company("Centre Culturel Charlie Chaplin", Company.CompanyType.CLIENT);
            clientC.setAddress("12 avenue Charlie Chaplin");
            clientC.setCity("Vaulx-en-Velin");
            clientC.setPhone("04 72 04 81 18");
            clientC.setEmail("accueil@4c-vaulxenvelin.com");
            companyRepo.save(clientC);
            
            System.out.println("   ✓ Sociétés créées");
            
        } catch (Exception e) {
            System.err.println("   ❌ Erreur sociétés: " + e.getMessage());
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
}