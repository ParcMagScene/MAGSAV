package com.magsav.util;

import com.magsav.db.DB;
import com.magsav.repo.*;
import com.magsav.model.Company;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Random;

/**
 * Générateur complet de données de test pour toutes les tables
 */
public class CompleteTestDataGenerator {
    
    private static final Random random = new Random();
    
    public static void main(String[] args) {
        generateCompleteTestData();
    }
    
    public static void generateCompleteTestData() {
        try {
            System.out.println("=== GÉNÉRATION COMPLÈTE DE DONNÉES DE TEST ===");
            
            // 1. Vider complètement la base
            System.out.println("🧹 Vidage de la base de données...");
            DatabaseCleaner.cleanDatabase();
            
            // 2. Générer toutes les données dans l'ordre des dépendances
            System.out.println("\n📊 Génération des nouvelles données...");
            generateUsers();
            generateCategories();
            generateCompanies();
            generateProducts();
            generateInterventions();
            generateDemandesIntervention();
            
            // 3. Afficher un résumé final
            printFinalSummary();
            
            System.out.println("\n✅ Génération complète terminée avec succès !");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Génère des utilisateurs de test
     */
    private static void generateUsers() {
        System.out.println("👥 Génération des utilisateurs...");
        try (Connection conn = DB.getConnection()) {
            Statement stmt = conn.createStatement();
            
            // Utilisateurs de test
            String[][] users = {
                {"admin", "admin123", "ADMIN", "Administrateur", "System"},
                {"technicien1", "tech123", "TECHNICIEN", "Jean", "Dupont"},
                {"technicien2", "tech123", "TECHNICIEN", "Marie", "Martin"},
                {"manager", "manager123", "MANAGER", "Pierre", "Durand"},
                {"user", "user123", "USER", "Sophie", "Bernard"}
            };
            
            for (String[] user : users) {
                try {
                    String sql = "INSERT INTO users (username, password_hash, role, first_name, last_name, created_at) " +
                               "VALUES ('" + user[0] + "', '" + user[1] + "', '" + user[2] + "', '" + 
                               user[3] + "', '" + user[4] + "', datetime('now'))";
                    stmt.executeUpdate(sql);
                } catch (Exception e) {
                    System.out.println("   ⚠️ Utilisateur " + user[0] + " existe déjà ou erreur");
                }
            }
            
            System.out.println("   ✓ " + users.length + " utilisateurs créés");
            
        } catch (Exception e) {
            System.err.println("   ❌ Erreur utilisateurs: " + e.getMessage());
        }
    }
    
    /**
     * Génère des catégories hiérarchiques
     */
    private static void generateCategories() {
        System.out.println("📁 Génération des catégories...");
        try {
            CategoryRepository categoryRepo = new CategoryRepository();
            
            // Catégories principales
            long audiovisuelId = categoryRepo.insert("Audiovisuel", null);
            long informatiqueId = categoryRepo.insert("Informatique", null);
            long eclairageId = categoryRepo.insert("Éclairage", null);
            long sonId = categoryRepo.insert("Sonorisation", null);
            long reseauId = categoryRepo.insert("Réseau", null);
            
            // Sous-catégories Audiovisuel
            long camerasId = categoryRepo.insert("Caméras", audiovisuelId);
            categoryRepo.insert("Caméras PTZ", camerasId);
            categoryRepo.insert("Caméras fixes", camerasId);
            categoryRepo.insert("Caméras portables", camerasId);
            
            categoryRepo.insert("Enregistreurs", audiovisuelId);
            categoryRepo.insert("Moniteurs", audiovisuelId);
            categoryRepo.insert("Projecteurs vidéo", audiovisuelId);
            
            // Sous-catégories Informatique
            long ordinateursId = categoryRepo.insert("Ordinateurs", informatiqueId);
            categoryRepo.insert("PC Bureau", ordinateursId);
            categoryRepo.insert("Portables", ordinateursId);
            categoryRepo.insert("Workstations", ordinateursId);
            
            categoryRepo.insert("Tablettes", informatiqueId);
            categoryRepo.insert("Serveurs", informatiqueId);
            categoryRepo.insert("Stockage", informatiqueId);
            
            // Sous-catégories Éclairage
            categoryRepo.insert("Panneaux LED", eclairageId);
            categoryRepo.insert("Projecteurs LED", eclairageId);
            categoryRepo.insert("Consoles d'éclairage", eclairageId);
            categoryRepo.insert("Gradateurs", eclairageId);
            
            // Sous-catégories Sonorisation
            long microsId = categoryRepo.insert("Microphones", sonId);
            categoryRepo.insert("Micros HF", microsId);
            categoryRepo.insert("Micros filaires", microsId);
            categoryRepo.insert("Micros-cravates", microsId);
            
            categoryRepo.insert("Enceintes", sonId);
            categoryRepo.insert("Amplificateurs", sonId);
            categoryRepo.insert("Tables de mixage", sonId);
            
            // Sous-catégories Réseau
            categoryRepo.insert("Switches", reseauId);
            categoryRepo.insert("Routeurs", reseauId);
            categoryRepo.insert("Points d'accès WiFi", reseauId);
            categoryRepo.insert("Câblage", reseauId);
            
            System.out.println("   ✓ Catégories hiérarchiques créées");
            
        } catch (Exception e) {
            System.err.println("   ❌ Erreur catégories: " + e.getMessage());
        }
    }
    
    /**
     * Génère des sociétés variées
     */
    private static void generateCompanies() {
        System.out.println("🏢 Génération des sociétés...");
        try {
            CompanyRepository companyRepo = new CompanyRepository(DB.getConnection());
            
            // Fabricants technologiques
            String[][] manufacturers = {
                {"Apple", "https://www.apple.com", "Cupertino", "Informatique", "1 Apple Park Way"},
                {"Microsoft", "https://www.microsoft.com", "Redmond", "Informatique", "1 Microsoft Way"},
                {"Sony", "https://www.sony.com", "Tokyo", "Électronique", "1-7-1 Konan"},
                {"Canon", "https://www.canon.com", "Tokyo", "Optique", "30-2 Shimomaruko"},
                {"Panasonic", "https://www.panasonic.com", "Osaka", "Électronique", "1006 Oaza Kadoma"},
                {"Blackmagic Design", "https://www.blackmagicdesign.com", "Melbourne", "Vidéo", "Port Melbourne"},
                {"Audio-Technica", "https://www.audio-technica.com", "Tokyo", "Audio", "2-46-1 Nippori"},
                {"Sennheiser", "https://www.sennheiser.com", "Wedemark", "Audio", "Am Labor 1"},
                {"Rode", "https://www.rode.com", "Sydney", "Audio", "107 Carnarvon St"},
                {"Aputure", "https://www.aputure.com", "Los Angeles", "Éclairage", "1234 Main St"},
                {"Shure", "https://www.shure.com", "Niles", "Audio", "5800 W Touhy Ave"},
                {"JBL", "https://www.jbl.com", "Los Angeles", "Audio", "8500 Balboa Blvd"},
                {"Yamaha", "https://www.yamaha.com", "Hamamatsu", "Audio", "10-1 Nakazawa-cho"},
                {"Dell", "https://www.dell.com", "Round Rock", "Informatique", "One Dell Way"},
                {"HP", "https://www.hp.com", "Palo Alto", "Informatique", "1501 Page Mill Rd"}
            };
            
            for (String[] mfg : manufacturers) {
                try {
                    Company manufacturer = new Company(mfg[0], Company.CompanyType.MANUFACTURER);
                    manufacturer.setWebsite(mfg[1]);
                    manufacturer.setCity(mfg[2]);
                    manufacturer.setSector(mfg[3]);
                    manufacturer.setAddress(mfg[4]);
                    companyRepo.save(manufacturer);
                } catch (Exception e) {
                    System.out.println("   ⚠️ Fabricant " + mfg[0] + " existe déjà");
                }
            }
            
            // Clients variés
            String[][] clients = {
                {"École Nationale Supérieure de Lyon", "12 rue de la République", "Lyon", "04 78 28 37 28", "contact@ensl.fr"},
                {"Université Claude Bernard Lyon 1", "43 bd du 11 novembre 1918", "Villeurbanne", "04 72 44 80 00", "scolarite@univ-lyon1.fr"},
                {"École Centrale de Lyon", "36 avenue Guy de Collongue", "Écully", "04 72 18 60 00", "info@ec-lyon.fr"},
                {"INSA Lyon", "20 avenue Albert Einstein", "Villeurbanne", "04 72 43 83 83", "communication@insa-lyon.fr"},
                {"Salle Paul Bocuse", "20 place Bellecour", "Lyon", "04 78 42 10 10", "events@bocuse-lyon.com"},
                {"Opéra de Lyon", "1 place de la Comédie", "Lyon", "04 69 85 54 54", "technique@opera-lyon.com"},
                {"Palais des Congrès", "50 quai Charles de Gaulle", "Lyon", "04 72 82 29 29", "technique@palaisdescongreslyon.com"},
                {"Musée des Confluences", "86 quai Perrache", "Lyon", "04 72 69 05 05", "technique@museedesconfluences.fr"},
                {"Hôpital Édouard Herriot", "5 place d'Arsonval", "Lyon", "04 72 11 73 11", "informatique@chu-lyon.fr"},
                {"Centre Culturel Charlie Chaplin", "4 rue du 8 mai 1945", "Vaulx-en-Velin", "04 72 04 81 18", "technique@centrechaplin.com"},
                {"Théâtre National Populaire", "8 place Lazare Goujon", "Villeurbanne", "04 78 03 30 00", "technique@tnp-villeurbanne.com"},
                {"Mairie de Lyon", "1 place de la Comédie", "Lyon", "04 72 10 30 30", "informatique@mairie-lyon.fr"}
            };
            
            for (String[] client : clients) {
                try {
                    Company company = new Company(client[0], Company.CompanyType.CLIENT);
                    company.setAddress(client[1]);
                    company.setCity(client[2]);
                    company.setPhone(client[3]);
                    company.setEmail(client[4]);
                    companyRepo.save(company);
                } catch (Exception e) {
                    System.out.println("   ⚠️ Client " + client[0] + " existe déjà");
                }
            }
            
            // Fournisseurs
            String[][] suppliers = {
                {"TechnoServices Lyon", "25 rue de la Technologie", "Lyon", "04 78 90 12 34", "contact@technoservices.fr"},
                {"Matériel Pro Distribution", "18 avenue des Frères Lumière", "Lyon", "04 78 85 67 89", "vente@materiel-pro.com"},
                {"Audiovisuel Rhône-Alpes", "45 cours Lafayette", "Lyon", "04 72 56 78 90", "commercial@audiovisuel-ra.fr"}
            };
            
            for (String[] supplier : suppliers) {
                try {
                    Company company = new Company(supplier[0], Company.CompanyType.SUPPLIER);
                    company.setAddress(supplier[1]);
                    company.setCity(supplier[2]);
                    company.setPhone(supplier[3]);
                    company.setEmail(supplier[4]);
                    companyRepo.save(company);
                } catch (Exception e) {
                    System.out.println("   ⚠️ Fournisseur " + supplier[0] + " existe déjà");
                }
            }
            
            System.out.println("   ✓ Sociétés variées créées");
            
        } catch (Exception e) {
            System.err.println("   ❌ Erreur sociétés: " + e.getMessage());
        }
    }
    
    /**
     * Génère des produits réalistes
     */
    private static void generateProducts() {
        System.out.println("📦 Génération des produits...");
        try {
            ProductRepository productRepo = new ProductRepository();
            
            // Produits réalistes avec numéros de série
            String[][] products = {
                // Caméras et vidéo
                {"Sony FX9", "SN-FX9-001", "Sony", "CAM001", "En stock"},
                {"Canon C300 Mark III", "CN-C300-002", "Canon", "CAM002", "En service"},
                {"Blackmagic URSA Mini Pro", "BM-URSA-003", "Blackmagic Design", "CAM003", "En maintenance"},
                {"Panasonic GH6", "PAN-GH6-004", "Panasonic", "CAM004", "En stock"},
                {"Sony A7S III", "SN-A7S-005", "Sony", "CAM005", "En service"},
                
                // Audio
                {"Shure Beta 58A", "SH-B58-101", "Shure", "MIC001", "En stock"},
                {"Audio-Technica AT2020", "AT-2020-102", "Audio-Technica", "MIC002", "En service"},
                {"Rode PodMic", "RD-POD-103", "Rode", "MIC003", "En stock"},
                {"Sennheiser MKE 600", "SN-MKE-104", "Sennheiser", "MIC004", "En maintenance"},
                {"Shure SM57", "SH-SM57-105", "Shure", "MIC005", "En stock"},
                {"JBL EON615", "JBL-EON-201", "JBL", "SPK001", "En service"},
                {"Yamaha HS8", "YMH-HS8-202", "Yamaha", "SPK002", "En stock"},
                
                // Éclairage
                {"Aputure 300d Mark II", "APT-300D-301", "Aputure", "LGT001", "En stock"},
                {"Aputure 120d Mark II", "APT-120D-302", "Aputure", "LGT002", "En service"},
                {"Aputure MC", "APT-MC-303", "Aputure", "LGT003", "En stock"},
                
                // Informatique
                {"iMac 27\" M1", "MAC-27-401", "Apple", "MAC001", "En stock"},
                {"MacBook Pro 16\" M2", "MBP-16-402", "Apple", "MAC002", "En service"},
                {"iPad Pro 12.9\"", "IPD-PRO-403", "Apple", "TAB001", "En stock"},
                {"Surface Studio 2", "SFC-ST2-404", "Microsoft", "WIN001", "En maintenance"},
                {"Dell XPS 15", "DLL-XPS-405", "Dell", "WIN002", "En stock"},
                {"HP Z4 Workstation", "HP-Z4-406", "HP", "WIN003", "En service"},
                
                // Réseau
                {"Switch Cisco 24 ports", "CSC-SW24-501", "Cisco", "NET001", "En stock"},
                {"Routeur Cisco", "CSC-RTR-502", "Cisco", "NET002", "En service"},
                {"Point d'accès WiFi", "WAP-AC-503", "Cisco", "NET003", "En stock"}
            };
            
            for (String[] product : products) {
                try {
                    productRepo.insert(product[0], product[1], product[2], product[3], product[4]);
                    Thread.sleep(10); // Éviter les collisions de timestamps
                } catch (Exception e) {
                    System.out.println("   ⚠️ Produit " + product[0] + " existe déjà ou erreur: " + e.getMessage());
                }
            }
            
            System.out.println("   ✓ " + products.length + " produits créés");
            
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
            
            // Récupérer des produits et clients pour créer des interventions
            try (Connection conn = DB.getConnection()) {
                Statement stmt = conn.createStatement();
                
                // Récupérer des produits
                ResultSet productsRs = stmt.executeQuery("SELECT id, nom FROM produits LIMIT 15");
                
                // Récupérer des clients
                ResultSet clientsRs = stmt.executeQuery("SELECT id, name FROM companies WHERE type = 'CLIENT' LIMIT 8");
                java.util.List<Long> clientIds = new java.util.ArrayList<>();
                while (clientsRs.next()) {
                    clientIds.add(clientsRs.getLong("id"));
                }
                
                String[] pannes = {
                    "Écran défaillant - pixels morts visibles",
                    "Problème de connectivité WiFi - ne se connecte plus",
                    "Batterie ne charge plus - voyant rouge fixe",
                    "Objectif bloqué en position zoom",
                    "Pas de son en sortie - problème amplificateur",
                    "Surchauffe du processeur - arrêt automatique",
                    "Boutons de contrôle non fonctionnels",
                    "Écran tactile ne répond plus aux touches",
                    "Problème de mise au point automatique",
                    "Carte mémoire non reconnue par l'appareil",
                    "Ventilateur bruyant - roulements usés",
                    "Port USB endommagé - connexion instable",
                    "Clavier défaillant - touches qui collent",
                    "Problème d'alimentation - s'éteint aléatoirement",
                    "Objectif rayé - impact sur la qualité image"
                };
                
                String[] statuts = {"En cours", "Terminée", "En attente pièces", "Devis envoyé", "En diagnostic"};
                String[] detecteurs = {"Technicien1", "Technicien2", "Manager", "Client"};
                
                int interventionCount = 0;
                java.util.List<Long> productIds = new java.util.ArrayList<>();
                
                // Collecter les IDs des produits
                while (productsRs.next()) {
                    productIds.add(productsRs.getLong("id"));
                }
                
                // Créer 25 interventions
                for (int i = 0; i < 25 && i < productIds.size(); i++) {
                    try {
                        Long productId = productIds.get(i % productIds.size());
                        String statut = statuts[random.nextInt(statuts.length)];
                        String panne = pannes[random.nextInt(pannes.length)];
                        String detecteur = detecteurs[random.nextInt(detecteurs.length)];
                        
                        // Dates aléatoires dans les 3 derniers mois
                        int jourEntree = random.nextInt(90) + 1;
                        String dateEntree = "2024-" + String.format("%02d", 10 - (jourEntree / 30)) + "-" + String.format("%02d", (jourEntree % 30) + 1);
                        
                        String dateSortie = null;
                        if (statut.equals("Terminée")) {
                            int jourSortie = jourEntree - random.nextInt(15) - 1;
                            if (jourSortie > 0) {
                                dateSortie = "2024-" + String.format("%02d", 10 - (jourSortie / 30)) + "-" + String.format("%02d", (jourSortie % 30) + 1);
                            }
                        }
                        
                        String suiviNo = "INT" + String.format("%04d", i + 1);
                        Long clientId = !clientIds.isEmpty() ? clientIds.get(random.nextInt(clientIds.size())) : null;
                        
                        interventionRepo.insertFromImport(
                            productId, statut, panne, detecteur,
                            dateEntree, dateSortie, suiviNo, 
                            "CLIENT", clientId
                        );
                        
                        interventionCount++;
                        
                    } catch (Exception e) {
                        System.err.println("   ⚠️ Erreur intervention " + (i+1) + ": " + e.getMessage());
                    }
                }
                
                System.out.println("   ✓ " + interventionCount + " interventions créées");
            }
            
        } catch (Exception e) {
            System.err.println("   ❌ Erreur interventions: " + e.getMessage());
        }
    }
    
    /**
     * Génère quelques demandes d'intervention
     */
    private static void generateDemandesIntervention() {
        System.out.println("📋 Génération des demandes d'intervention...");
        try (Connection conn = DB.getConnection()) {
            Statement stmt = conn.createStatement();
            
            // Récupérer quelques produits et clients
            ResultSet productsRs = stmt.executeQuery("SELECT id FROM produits LIMIT 5");
            ResultSet clientsRs = stmt.executeQuery("SELECT id FROM companies WHERE type = 'CLIENT' LIMIT 3");
            
            java.util.List<Long> productIds = new java.util.ArrayList<>();
            java.util.List<Long> clientIds = new java.util.ArrayList<>();
            
            while (productsRs.next()) productIds.add(productsRs.getLong("id"));
            while (clientsRs.next()) clientIds.add(clientsRs.getLong("id"));
            
            if (!productIds.isEmpty() && !clientIds.isEmpty()) {
                String[] descriptions = {
                    "Problème de fonctionnement constaté ce matin",
                    "Équipement en panne depuis hier",
                    "Maintenance préventive à effectuer",
                    "Dysfonctionnement intermittent signalé",
                    "Réparation urgente demandée"
                };
                
                for (int i = 0; i < 5 && i < productIds.size(); i++) {
                    try {
                        Long productId = productIds.get(i);
                        Long clientId = clientIds.get(i % clientIds.size());
                        String description = descriptions[i];
                        
                        String sql = "INSERT INTO demandes_intervention " +
                                   "(product_id, proprietaire_type, proprietaire_id, description_probleme, " +
                                   "statut, date_demande, demandeur_nom, demandeur_email) VALUES (" +
                                   productId + ", 'CLIENT', " + clientId + ", '" + description + "', " +
                                   "'EN_ATTENTE', datetime('now'), 'Jean Dupont', 'j.dupont@client.com')";
                        
                        stmt.executeUpdate(sql);
                        
                    } catch (Exception e) {
                        System.err.println("   ⚠️ Erreur demande " + (i+1) + ": " + e.getMessage());
                    }
                }
                
                System.out.println("   ✓ 5 demandes d'intervention créées");
            }
            
        } catch (Exception e) {
            System.err.println("   ❌ Erreur demandes: " + e.getMessage());
        }
    }
    
    /**
     * Affiche un résumé final de toutes les données
     */
    private static void printFinalSummary() {
        System.out.println("\n📊 RÉSUMÉ FINAL DES DONNÉES :");
        try (Connection conn = DB.getConnection()) {
            Statement stmt = conn.createStatement();
            
            String[] tables = {"users", "companies", "categories", "produits", "interventions", "demandes_intervention"};
            String[] labels = {"👥 Utilisateurs", "🏢 Sociétés", "📁 Catégories", "📦 Produits", "🔧 Interventions", "📋 Demandes"};
            
            for (int i = 0; i < tables.length; i++) {
                try {
                    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM " + tables[i]);
                    if (rs.next()) {
                        System.out.println("   " + labels[i] + ": " + rs.getInt("count"));
                    }
                } catch (Exception e) {
                    System.out.println("   " + labels[i] + ": Erreur lecture");
                }
            }
            
        } catch (Exception e) {
            System.err.println("   ❌ Erreur résumé: " + e.getMessage());
        }
    }
}