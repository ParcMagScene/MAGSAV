package com.magsav.util;

import com.magsav.db.DB;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Utilitaire pour vider complètement la base et créer des données fraîches
 */
public class FreshDataGenerator {
    
    /**
     * Échappe les apostrophes pour SQL
     */
    private static String escapeSql(String str) {
        if (str == null) return "NULL";
        return str.replace("'", "''");
    }
    
    public static void main(String[] args) {
        try {
            System.out.println("=== RÉGÉNÉRATION COMPLÈTE DES DONNÉES ===");
            
            // 1. Vider toutes les tables
            cleanDatabase();
            
            // 2. Générer de nouvelles données
            generateFreshData();
            
            // 3. Afficher le résumé
            printSummary();
            
            System.out.println("\n✅ Régénération terminée avec succès !");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Vide complètement toutes les tables
     */
    private static void cleanDatabase() {
        System.out.println("🧹 Vidage complet de la base de données...");
        
        try (Connection conn = DB.getConnection()) {
            Statement stmt = conn.createStatement();
            
            // Désactiver les contraintes de clés étrangères temporairement
            stmt.executeUpdate("PRAGMA foreign_keys = OFF");
            
            // Vider toutes les tables dans l'ordre
            String[] tables = {
                "interventions",
                "demandes_intervention", 
                "demandes_creation_proprietaire",
                "demandes_elevation_privilege",
                "sav_history",
                "produits",
                "categories", 
                "companies",
                "societes",
                "users"
            };
            
            for (String table : tables) {
                try {
                    stmt.executeUpdate("DELETE FROM " + table);
                    System.out.println("   ✓ Table " + table + " vidée");
                } catch (Exception e) {
                    System.out.println("   ⚠️ Erreur pour " + table + ": " + e.getMessage());
                }
            }
            
            // Réinitialiser les compteurs auto-increment
            stmt.executeUpdate("DELETE FROM sqlite_sequence");
            System.out.println("   ✓ Compteurs auto-increment réinitialisés");
            
            // Réactiver les contraintes
            stmt.executeUpdate("PRAGMA foreign_keys = ON");
            
            System.out.println("   ✅ Base de données complètement vidée");
            
        } catch (Exception e) {
            System.err.println("   ❌ Erreur lors du vidage: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Génère des données de test fraîches
     */
    private static void generateFreshData() {
        System.out.println("\n📊 Génération de nouvelles données...");
        
        try (Connection conn = DB.getConnection()) {
            Statement stmt = conn.createStatement();
            
            // 1. Utilisateurs
            System.out.println("👥 Création des utilisateurs...");
            String[][] users = {
                {"admin", "admin@magsav.com", "admin123", "ADMIN", "Administrateur System"},
                {"jean.dupont", "jean.dupont@magsav.com", "tech123", "USER", "Jean Dupont"},
                {"marie.martin", "marie.martin@magsav.com", "tech123", "USER", "Marie Martin"},
                {"pierre.durand", "pierre.durand@magsav.com", "manager123", "USER", "Pierre Durand"},
                {"sophie.bernard", "sophie.bernard@magsav.com", "user123", "USER", "Sophie Bernard"}
            };
            
            for (String[] user : users) {
                String sql = "INSERT INTO users (username, email, password_hash, role, full_name, created_at) " +
                           "VALUES ('" + user[0] + "', '" + user[1] + "', '" + user[2] + "', '" + 
                           user[3] + "', '" + user[4] + "', datetime('now'))";
                stmt.executeUpdate(sql);
            }
            System.out.println("   ✓ " + users.length + " utilisateurs créés");
            
            // 2. Catégories hiérarchiques
            System.out.println("📁 Création des catégories...");
            
            // Catégories principales
            stmt.executeUpdate("INSERT INTO categories (nom, parent_id) VALUES ('Audiovisuel', NULL)");
            stmt.executeUpdate("INSERT INTO categories (nom, parent_id) VALUES ('Informatique', NULL)"); 
            stmt.executeUpdate("INSERT INTO categories (nom, parent_id) VALUES ('Éclairage', NULL)");
            stmt.executeUpdate("INSERT INTO categories (nom, parent_id) VALUES ('Sonorisation', NULL)");
            stmt.executeUpdate("INSERT INTO categories (nom, parent_id) VALUES ('Réseau', NULL)");
            
            // Sous-catégories Audiovisuel (id=1)
            stmt.executeUpdate("INSERT INTO categories (nom, parent_id) VALUES ('Caméras', 1)");
            stmt.executeUpdate("INSERT INTO categories (nom, parent_id) VALUES ('Caméras PTZ', 6)");
            stmt.executeUpdate("INSERT INTO categories (nom, parent_id) VALUES ('Caméras fixes', 6)");
            stmt.executeUpdate("INSERT INTO categories (nom, parent_id) VALUES ('Moniteurs', 1)");
            stmt.executeUpdate("INSERT INTO categories (nom, parent_id) VALUES ('Enregistreurs', 1)");
            
            // Sous-catégories Informatique (id=2)
            stmt.executeUpdate("INSERT INTO categories (nom, parent_id) VALUES ('Ordinateurs', 2)");
            stmt.executeUpdate("INSERT INTO categories (nom, parent_id) VALUES ('PC Bureau', 11)");
            stmt.executeUpdate("INSERT INTO categories (nom, parent_id) VALUES ('Portables', 11)");
            stmt.executeUpdate("INSERT INTO categories (nom, parent_id) VALUES ('Tablettes', 2)");
            stmt.executeUpdate("INSERT INTO categories (nom, parent_id) VALUES ('Serveurs', 2)");
            
            // Sous-catégories Éclairage (id=3)
            stmt.executeUpdate("INSERT INTO categories (nom, parent_id) VALUES ('Panneaux LED', 3)");
            stmt.executeUpdate("INSERT INTO categories (nom, parent_id) VALUES ('Projecteurs LED', 3)");
            stmt.executeUpdate("INSERT INTO categories (nom, parent_id) VALUES ('Consoles éclairage', 3)");
            
            // Sous-catégories Sonorisation (id=4)
            stmt.executeUpdate("INSERT INTO categories (nom, parent_id) VALUES ('Microphones', 4)");
            stmt.executeUpdate("INSERT INTO categories (nom, parent_id) VALUES ('Micros HF', 19)");
            stmt.executeUpdate("INSERT INTO categories (nom, parent_id) VALUES ('Micros filaires', 19)");
            stmt.executeUpdate("INSERT INTO categories (nom, parent_id) VALUES ('Enceintes', 4)");
            stmt.executeUpdate("INSERT INTO categories (nom, parent_id) VALUES ('Amplificateurs', 4)");
            
            // Sous-catégories Réseau (id=5)  
            stmt.executeUpdate("INSERT INTO categories (nom, parent_id) VALUES ('Switches', 5)");
            stmt.executeUpdate("INSERT INTO categories (nom, parent_id) VALUES ('Routeurs', 5)");
            stmt.executeUpdate("INSERT INTO categories (nom, parent_id) VALUES ('WiFi', 5)");
            
            System.out.println("   ✓ 26 catégories hiérarchiques créées");
            
            // 3. Sociétés
            System.out.println("🏢 Création des sociétés...");
            
            // Fabricants technologiques
            String[][] manufacturers = {
                {"Apple", "MANUFACTURER", "Cupertino", "https://www.apple.com", "1 Apple Park Way"},
                {"Sony", "MANUFACTURER", "Tokyo", "https://www.sony.com", "1-7-1 Konan"},
                {"Canon", "MANUFACTURER", "Tokyo", "https://www.canon.com", "30-2 Shimomaruko"},
                {"Panasonic", "MANUFACTURER", "Osaka", "https://www.panasonic.com", "1006 Oaza Kadoma"},
                {"Blackmagic Design", "MANUFACTURER", "Melbourne", "https://www.blackmagicdesign.com", "Port Melbourne"},
                {"Audio-Technica", "MANUFACTURER", "Tokyo", "https://www.audio-technica.com", "2-46-1 Nippori"},
                {"Shure", "MANUFACTURER", "Niles", "https://www.shure.com", "5800 W Touhy Ave"},
                {"Aputure", "MANUFACTURER", "Los Angeles", "https://www.aputure.com", "1234 Main St"},
                {"Dell", "MANUFACTURER", "Round Rock", "https://www.dell.com", "One Dell Way"},
                {"HP", "MANUFACTURER", "Palo Alto", "https://www.hp.com", "1501 Page Mill Rd"}
            };
            
            for (String[] mfg : manufacturers) {
                String sql = "INSERT INTO companies (name, type, city, website, address, created_at, updated_at) VALUES " +
                           "('" + escapeSql(mfg[0]) + "', '" + mfg[1] + "', '" + escapeSql(mfg[2]) + "', '" + 
                           escapeSql(mfg[3]) + "', '" + escapeSql(mfg[4]) + "', datetime('now'), datetime('now'))";
                stmt.executeUpdate(sql);
            }
            
            // Clients
            String[][] clients = {
                {"École Nationale Supérieure de Lyon", "CLIENT", "Lyon", "12 rue de la République", "04 78 28 37 28"},
                {"Université Claude Bernard Lyon 1", "CLIENT", "Villeurbanne", "43 bd du 11 novembre 1918", "04 72 44 80 00"},
                {"École Centrale de Lyon", "CLIENT", "Écully", "36 avenue Guy de Collongue", "04 72 18 60 00"},
                {"INSA Lyon", "CLIENT", "Villeurbanne", "20 avenue Albert Einstein", "04 72 43 83 83"},
                {"Salle Paul Bocuse", "CLIENT", "Lyon", "20 place Bellecour", "04 78 42 10 10"},
                {"Opéra de Lyon", "CLIENT", "Lyon", "1 place de la Comédie", "04 69 85 54 54"},
                {"Musée des Confluences", "CLIENT", "Lyon", "86 quai Perrache", "04 72 69 05 05"},
                {"Hôpital Édouard Herriot", "CLIENT", "Lyon", "5 place d''Arsonval", "04 72 11 73 11"}
            };
            
            for (String[] client : clients) {
                String sql = "INSERT INTO companies (name, type, city, address, phone, created_at, updated_at) VALUES " +
                           "('" + escapeSql(client[0]) + "', '" + client[1] + "', '" + escapeSql(client[2]) + "', '" + 
                           escapeSql(client[3]) + "', '" + escapeSql(client[4]) + "', datetime('now'), datetime('now'))";
                stmt.executeUpdate(sql);
            }
            
            // Fournisseurs
            String[][] suppliers = {
                {"TechnoServices Lyon", "SUPPLIER", "Lyon", "25 rue de la Technologie", "04 78 90 12 34"},
                {"Matériel Pro Distribution", "SUPPLIER", "Lyon", "18 avenue des Frères Lumière", "04 78 85 67 89"},
                {"Audiovisuel Rhône-Alpes", "SUPPLIER", "Lyon", "45 cours Lafayette", "04 72 56 78 90"}
            };
            
            for (String[] supplier : suppliers) {
                String sql = "INSERT INTO companies (name, type, city, address, phone, created_at, updated_at) VALUES " +
                           "('" + escapeSql(supplier[0]) + "', '" + supplier[1] + "', '" + escapeSql(supplier[2]) + "', '" + 
                           escapeSql(supplier[3]) + "', '" + escapeSql(supplier[4]) + "', datetime('now'), datetime('now'))";
                stmt.executeUpdate(sql);
            }
            
            System.out.println("   ✓ " + (manufacturers.length + clients.length + suppliers.length) + " sociétés créées");
            
            // 4. Produits réalistes
            System.out.println("📦 Création des produits...");
            
            String[][] products = {
                // Caméras 
                {"Sony FX9", "SN-FX9-2024-001", "Sony", "CAM001", "En stock"},
                {"Canon C300 Mark III", "CN-C300-2024-002", "Canon", "CAM002", "En service"},
                {"Blackmagic URSA Mini Pro", "BM-URSA-2024-003", "Blackmagic Design", "CAM003", "En stock"},
                {"Panasonic GH6", "PAN-GH6-2024-004", "Panasonic", "CAM004", "En service"},
                {"Sony A7S III", "SN-A7S-2024-005", "Sony", "CAM005", "En stock"},
                
                // Informatique 
                {"iMac 27\" M2", "MAC-27-2024-001", "Apple", "MAC001", "En stock"},
                {"MacBook Pro 16\" M2", "MBP-16-2024-002", "Apple", "MAC002", "En service"},
                {"iPad Pro 12.9\"", "IPD-PRO-2024-003", "Apple", "TAB001", "En stock"},
                {"Dell XPS 15", "DLL-XPS-2024-004", "Dell", "WIN001", "En stock"},
                {"HP Z4 Workstation", "HP-Z4-2024-005", "HP", "WIN002", "En service"},
                
                // Audio 
                {"Shure Beta 58A", "SH-B58-2024-101", "Shure", "MIC001", "En stock"},
                {"Audio-Technica AT2020", "AT-2020-2024-102", "Audio-Technica", "MIC002", "En service"},
                {"Shure SM57", "SH-SM57-2024-103", "Shure", "MIC003", "En stock"},
                
                // Éclairage 
                {"Aputure 300d Mark II", "APT-300D-2024-301", "Aputure", "LGT001", "En stock"},
                {"Aputure 120d Mark II", "APT-120D-2024-302", "Aputure", "LGT002", "En service"}
            };
            
            for (String[] product : products) {
                String sql = "INSERT INTO produits (nom, sn, fabricant, code, situation) VALUES " +
                           "('" + escapeSql(product[0]) + "', '" + escapeSql(product[1]) + "', '" + escapeSql(product[2]) + "', '" + 
                           escapeSql(product[3]) + "', '" + escapeSql(product[4]) + "')";
                stmt.executeUpdate(sql);
            }
            System.out.println("   ✓ " + products.length + " produits créés");
            
            // 5. Interventions réalistes
            System.out.println("🔧 Création des interventions...");
            
            String[] pannes = {
                "Écran défaillant - pixels morts",
                "Problème connectivité WiFi",
                "Batterie ne charge plus",
                "Objectif bloqué",
                "Pas de son en sortie",
                "Surchauffe processeur",
                "Boutons non fonctionnels"
            };
            
            String[] statuts = {"En cours", "Terminée", "En attente pièces", "Devis envoyé"};
            
            // 10 interventions d'exemple
            for (int i = 1; i <= 10; i++) {
                long productId = (i % products.length) + 1;
                String panne = pannes[i % pannes.length];
                String statut = statuts[i % statuts.length];
                
                String sql = "INSERT INTO interventions (product_id, statut, panne, " +
                           "detecteur, date_entree, owner_type) VALUES " +
                           "(" + productId + ", '" + escapeSql(statut) + "', '" + escapeSql(panne) + "', 'Technicien1', " +
                           "date('now', '-" + (i * 5) + " days'), 'MAGSAV')";
                stmt.executeUpdate(sql);
            }
            System.out.println("   ✓ 10 interventions créées");
            
        } catch (Exception e) {
            System.err.println("   ❌ Erreur génération: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Affiche un résumé des données créées
     */
    private static void printSummary() {
        System.out.println("\n📊 RÉSUMÉ DES NOUVELLES DONNÉES :");
        
        try (Connection conn = DB.getConnection()) {
            Statement stmt = conn.createStatement();
            
            String[] queries = {
                "SELECT COUNT(*) as count FROM users",
                "SELECT COUNT(*) as count FROM companies", 
                "SELECT COUNT(*) as count FROM categories",
                "SELECT COUNT(*) as count FROM produits",
                "SELECT COUNT(*) as count FROM interventions"
            };
            
            String[] labels = {"👥 Utilisateurs", "🏢 Sociétés", "📁 Catégories", "📦 Produits", "🔧 Interventions"};
            
            for (int i = 0; i < queries.length; i++) {
                var rs = stmt.executeQuery(queries[i]);
                if (rs.next()) {
                    System.out.println("   " + labels[i] + ": " + rs.getInt("count"));
                }
            }
            
        } catch (Exception e) {
            System.err.println("   ❌ Erreur résumé: " + e.getMessage());
        }
    }
}