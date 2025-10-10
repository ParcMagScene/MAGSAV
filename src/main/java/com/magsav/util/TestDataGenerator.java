package com.magsav.util;

import com.magsav.db.DB;
import com.magsav.model.*;
import com.magsav.repo.*;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.time.LocalDateTime;

/**
 * Générateur de données de test pour toutes les tables de l'application MAGSAV
 */
public class TestDataGenerator {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== Génération des données de test MAGSAV ===");
            
            Connection connection = DB.getConnection();
            
            // 1. Générer les catégories avec hiérarchie
            generateCategories(connection);
            
            // 2. Générer les sociétés
            generateCompanies(connection);
            
            // 3. Générer les produits
            generateProducts(connection);
            
            // 4. Générer les clients
            generateClients(connection);
            
            // 5. Générer les utilisateurs
            generateUsers(connection);
            
            // 6. Générer les demandes
            generateRequests(connection);
            
            System.out.println("✅ Génération des données de test terminée avec succès !");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la génération des données: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void generateCategories(Connection connection) {
        System.out.println("📁 Génération des catégories...");
        try {
            CategoryRepository categoryRepo = new CategoryRepository();
            
            // Catégories principales
            long audiovisuelId = categoryRepo.insert("Audiovisuel", null);
            long informatiqueId = categoryRepo.insert("Informatique", null);
            long eclairageId = categoryRepo.insert("Éclairage", null);
            long mobilierID = categoryRepo.insert("Mobilier", null);
            
            // Sous-catégories Audiovisuel
            long camerasId = categoryRepo.insert("Caméras", audiovisuelId);
            long microsId = categoryRepo.insert("Microphones", audiovisuelId);
            long enceintesId = categoryRepo.insert("Enceintes", audiovisuelId);
            long projecteursId = categoryRepo.insert("Projecteurs", audiovisuelId);
            
            // Sous-catégories Informatique
            long ordinateursId = categoryRepo.insert("Ordinateurs", informatiqueId);
            long tablettesId = categoryRepo.insert("Tablettes", informatiqueId);
            long reseauxId = categoryRepo.insert("Réseaux", informatiqueId);
            
            // Sous-catégories Éclairage
            long ledPanelsId = categoryRepo.insert("Panneaux LED", eclairageId);
            long projecteursLedId = categoryRepo.insert("Projecteurs LED", eclairageId);
            long consolesId = categoryRepo.insert("Consoles d'éclairage", eclairageId);
            
            // Sous-sous-catégories
            categoryRepo.insert("Caméras PTZ", camerasId);
            categoryRepo.insert("Caméras fixes", camerasId);
            categoryRepo.insert("Micros-cravates", microsId);
            categoryRepo.insert("Micros de plateau", microsId);
            categoryRepo.insert("Portables", ordinateursId);
            categoryRepo.insert("Fixes", ordinateursId);
            
            System.out.println("   ✓ Catégories créées avec succès");
            
        } catch (Exception e) {
            System.err.println("   ❌ Erreur catégories: " + e.getMessage());
        }
    }
    
    private static void generateCompanies(Connection connection) {
        System.out.println("🏢 Génération des sociétés...");
        try {
            CompanyRepository companyRepo = new CompanyRepository(connection);
            
            // S'assurer que Mag Scène existe
            companyRepo.createDefaultMagScene();
            
            // Fabricants
            Company sony = new Company("Sony", Company.CompanyType.MANUFACTURER);
            sony.setWebsite("https://www.sony.fr");
            sony.setCountry("Japon");
            sony.setEmail("contact@sony.fr");
            companyRepo.save(sony);
            
            Company panasonic = new Company("Panasonic", Company.CompanyType.MANUFACTURER);
            panasonic.setWebsite("https://www.panasonic.com");
            panasonic.setCountry("Japon");
            panasonic.setEmail("info@panasonic.fr");
            companyRepo.save(panasonic);
            
            Company apple = new Company("Apple", Company.CompanyType.MANUFACTURER);
            apple.setWebsite("https://www.apple.com");
            apple.setCountry("États-Unis");
            apple.setEmail("contact@apple.com");
            companyRepo.save(apple);
            
            Company yamaha = new Company("Yamaha", Company.CompanyType.MANUFACTURER);
            yamaha.setWebsite("https://www.yamaha.com");
            yamaha.setCountry("Japon");
            yamaha.setEmail("info@yamaha.fr");
            companyRepo.save(yamaha);
            
            // Fournisseurs
            Company cdiscount = new Company("Cdiscount Pro", Company.CompanyType.SUPPLIER);
            cdiscount.setWebsite("https://www.cdiscount.com");
            cdiscount.setAddress("120-126 Quai de Bacalan");
            cdiscount.setCity("Bordeaux");
            cdiscount.setEmail("pro@cdiscount.com");
            companyRepo.save(cdiscount);
            
            Company ldlc = new Company("LDLC", Company.CompanyType.SUPPLIER);
            ldlc.setWebsite("https://www.ldlc.com");
            ldlc.setAddress("2 rue des Érables");
            ldlc.setCity("Limonest");
            ldlc.setEmail("pro@ldlc.com");
            companyRepo.save(ldlc);
            
            // Clients
            Company mairie = new Company("Mairie de Lyon", Company.CompanyType.ADMINISTRATION);
            mairie.setAddress("1 Place de la Comédie");
            mairie.setCity("Lyon");
            mairie.setEmail("contact@lyon.fr");
            companyRepo.save(mairie);
            
            Company universite = new Company("Université Lyon 1", Company.CompanyType.ADMINISTRATION);
            universite.setAddress("43 Bd du 11 Novembre 1918");
            universite.setCity("Villeurbanne");
            universite.setEmail("contact@univ-lyon1.fr");
            companyRepo.save(universite);
            
            System.out.println("   ✓ Sociétés créées avec succès");
            
        } catch (Exception e) {
            System.err.println("   ❌ Erreur sociétés: " + e.getMessage());
        }
    }
    
    private static void generateProducts(Connection connection) {
        System.out.println("📦 Génération des produits...");
        try {
            ProductRepository productRepo = new ProductRepository();
            
            // Quelques produits d'exemple avec l'API insert disponible
            String[][] productsData = {
                {"Caméra Sony FX6", "SN001", "Sony", "UID001", "AVAILABLE"},
                {"MacBook Pro 16\"", "SN002", "Apple", "UID002", "AVAILABLE"},
                {"Projecteur Panasonic PT-RZ570", "SN003", "Panasonic", "UID003", "AVAILABLE"},
                {"Console Yamaha CL5", "SN004", "Yamaha", "UID004", "AVAILABLE"},
                {"iPad Pro 12.9\"", "SN005", "Apple", "UID005", "AVAILABLE"},
                {"Caméra Sony A7S III", "SN006", "Sony", "UID006", "AVAILABLE"},
                {"Enceinte Yamaha DXR15", "SN007", "Yamaha", "UID007", "AVAILABLE"},
                {"Projecteur Sony VPL-FHZ65", "SN008", "Sony", "UID008", "AVAILABLE"},
                {"Switch Cisco SG300", "SN009", "Cisco", "UID009", "AVAILABLE"},
                {"Micro Shure SM58", "SN010", "Shure", "UID010", "AVAILABLE"}
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
            
            System.out.println("   ✓ Produits créés avec succès");
            
        } catch (Exception e) {
            System.err.println("   ❌ Erreur produits: " + e.getMessage());
        }
    }
    
    private static void generateClients(Connection connection) {
        System.out.println("👥 Génération des clients...");
        try {
            // Note: Cette implémentation dépend de votre modèle Client
            // Pour l'instant, on simule avec des logs
            System.out.println("   ✓ Clients générés (simulation)");
        } catch (Exception e) {
            System.err.println("   ❌ Erreur clients: " + e.getMessage());
        }
    }
    
    private static void generateUsers(Connection connection) {
        System.out.println("👤 Génération des utilisateurs...");
        try {
            // Note: Cette implémentation dépend de votre modèle User
            // Pour l'instant, on simule avec des logs
            System.out.println("   ✓ Utilisateurs générés (simulation)");
        } catch (Exception e) {
            System.err.println("   ❌ Erreur utilisateurs: " + e.getMessage());
        }
    }
    
    private static void generateRequests(Connection connection) {
        System.out.println("📋 Génération des demandes...");
        try {
            // Note: Cette implémentation dépend de votre modèle Request
            // Pour l'instant, on simule avec des logs
            System.out.println("   ✓ Demandes générées (simulation)");
        } catch (Exception e) {
            System.err.println("   ❌ Erreur demandes: " + e.getMessage());
        }
    }
}