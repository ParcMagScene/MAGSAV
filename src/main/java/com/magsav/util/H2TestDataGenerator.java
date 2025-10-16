package com.magsav.util;

import com.magsav.db.H2DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Générateur de données de test pour H2
 * Génère des données cohérentes pour toutes les tables
 */
public class H2TestDataGenerator {
    
    private static final Random random = new Random();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Méthode statique appelable depuis l'interface utilisateur
     */
    public static void generateCompleteTestData() throws Exception {
        System.out.println("🔄 Génération de données de test pour H2...");
        
        // Initialiser H2 si pas déjà fait
        H2DB.init();
        Connection conn = H2DB.getConnection();
        conn.setAutoCommit(false);
        
        try {
            // Vider les tables dans l'ordre (contraintes FK)
            clearTables(conn);
            
            // Générer les données de base
            generateCategories(conn);
            generateSocietes(conn);
            generateUsers(conn);
            generateTechniciens(conn);
            generateVehicules(conn);
            generateProduits(conn);
            generateInterventions(conn);
            generateDemandesIntervention(conn);
            generateRequests(conn);
            generateAffaires(conn);
            generateDevis(conn);
            generateCommandes(conn);
            generatePlanifications(conn);
            generateEmailTemplates(conn);
            
            conn.commit();
            System.out.println("🎉 Données de test générées avec succès !");
            
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.close();
        }
    }

    public static void main(String[] args) {
        try {
            generateCompleteTestData();
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la génération : " + e.getMessage());
            e.printStackTrace();
        }
        
        System.exit(0);
    }
    
    private static void clearTables(Connection conn) throws SQLException {
        System.out.println("🗑️ Suppression des données existantes...");
        
        String[] tables = {
            "lignes_commandes", "commandes", "mouvements_stock", "alertes_stock",
            "planifications", "disponibilites_techniciens", "communications",
            "lignes_devis", "devis", "request_items", "requests", "affaires",
            "interventions", "demandes_intervention", "produits", "vehicules", "techniciens", "users",
            "societes", "categories", "email_templates", "configuration_google",
            "sav_history", "sync_history"
        };
        
        for (String table : tables) {
            conn.createStatement().executeUpdate("DELETE FROM " + table);
        }
    }
    
    private static void generateCategories(Connection conn) throws SQLException {
        System.out.println("📋 Génération des catégories...");
        
        String sql = "INSERT INTO categories (nom_categorie, parent_id) VALUES (?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        
        // Catégories principales
        String[] mainCategories = {
            "Éclairage", "Son", "Vidéo", "Scène", "Structure", "Sécurité", "Régie"
        };
        
        for (String cat : mainCategories) {
            stmt.setString(1, cat);
            stmt.setNull(2, java.sql.Types.INTEGER);
            stmt.executeUpdate();
        }
        
        // Sous-catégories pour Éclairage
        String[] lightingSubcats = {
            "Projecteurs LED", "Projecteurs traditionnels", "Consoles d'éclairage", "Accessoires éclairage"
        };
        
        for (String subcat : lightingSubcats) {
            stmt.setString(1, subcat);
            stmt.setInt(2, 1); // Parent = Éclairage
            stmt.executeUpdate();
        }
        
        stmt.close();
        System.out.println("  ✅ " + (mainCategories.length + lightingSubcats.length) + " catégories créées");
    }
    
    private static void generateSocietes(Connection conn) throws SQLException {
        System.out.println("🏢 Génération des sociétés...");
        
        String sql = """
            INSERT INTO societes (type_societe, nom_societe, raison_sociale, siret, 
                                 adresse_societe, code_postal, ville, pays, 
                                 telephone_societe, email_societe, is_active, 
                                 date_creation, date_modification) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        String now = LocalDate.now().format(DATE_FORMAT);
        
        String[][] companies = {
            {"COMPANY", "Théâtre National", "Théâtre National de France", "12345678901234", "1 Place du Théâtre", "75001", "Paris", "France", "01.42.44.45.46", "contact@theatre-national.fr"},
            {"COMPANY", "Productions Scène", "Productions Scène SARL", "23456789012345", "15 Rue de la Scène", "69001", "Lyon", "France", "04.78.25.36.47", "info@productions-scene.com"},
            {"COMPANY", "Éclairage Pro", "Éclairage Pro SAS", "34567890123456", "8 Avenue des Lumières", "13001", "Marseille", "France", "04.91.52.63.74", "contact@eclairage-pro.fr"},
            {"INDIVIDUAL", "Martin Dubois", null, null, "25 Rue des Artistes", "31000", "Toulouse", "France", "05.61.22.33.44", "martin.dubois@email.com"},
            {"INDIVIDUAL", "Sophie Laurent", null, null, "12 Boulevard des Arts", "44000", "Nantes", "France", "02.40.11.22.33", "sophie.laurent@email.com"}
        };
        
        for (String[] company : companies) {
            stmt.setString(1, company[0]);
            stmt.setString(2, company[1]);
            stmt.setString(3, company[2]);
            stmt.setString(4, company[3]);
            stmt.setString(5, company[4]);
            stmt.setString(6, company[5]);
            stmt.setString(7, company[6]);
            stmt.setString(8, company[7]);
            stmt.setString(9, company[8]);
            stmt.setString(10, company[9]);
            stmt.setInt(11, 1);
            stmt.setString(12, now);
            stmt.setString(13, now);
            stmt.executeUpdate();
        }
        
        stmt.close();
        System.out.println("  ✅ " + companies.length + " sociétés créées");
    }
    
    private static void generateUsers(Connection conn) throws SQLException {
        System.out.println("👥 Génération des utilisateurs...");
        
        String sql = """
            INSERT INTO users (username, password_hash, nom, prenom, email, telephone, 
                              role, specialite, is_active, created_at, updated_at) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        String now = LocalDate.now().format(DATE_FORMAT);
        
        String[][] users = {
            {"admin", "$2a$10$dummyHash1", "Administrateur", "Système", "admin@magsav.com", "01.00.00.00.00", "ADMINISTRATEUR", "Administration"},
            {"jdupont", "$2a$10$dummyHash2", "Dupont", "Jean", "jean.dupont@magsav.com", "01.23.45.67.89", "Technicien Mag Scène", "Éclairage"},
            {"mmartin", "$2a$10$dummyHash3", "Martin", "Marie", "marie.martin@magsav.com", "01.34.56.78.90", "Technicien Mag Scène", "Son"},
            {"pdurand", "$2a$10$dummyHash4", "Durand", "Pierre", "pierre.durand@magsav.com", "01.45.67.89.01", "COLLABORATEUR", "Régie"},
            {"abernard", "$2a$10$dummyHash5", "Bernard", "Anne", "anne.bernard@magsav.com", "01.56.78.90.12", "COLLABORATEUR", "Scène"},
            {"lgarcia", "$2a$10$dummyHash6", "Garcia", "Luis", "luis.garcia@magsav.com", "01.67.89.01.23", "ADMINISTRATEUR", "Système"},
            {"cmorel", "$2a$10$dummyHash7", "Morel", "Camille", "camille.morel@magsav.com", "01.78.90.12.34", "COLLABORATEUR", "Technique"}
        };
        
        for (String[] user : users) {
            stmt.setString(1, user[0]);
            stmt.setString(2, user[1]);
            stmt.setString(3, user[2]);
            stmt.setString(4, user[3]);
            stmt.setString(5, user[4]);
            stmt.setString(6, user[5]);
            stmt.setString(7, user[6]);
            stmt.setString(8, user[7]);
            stmt.setBoolean(9, true);
            stmt.setString(10, now);
            stmt.setString(11, now);
            stmt.executeUpdate();
        }
        
        stmt.close();
        System.out.println("  ✅ " + users.length + " utilisateurs créés");
    }
    
    private static void generateTechniciens(Connection conn) throws SQLException {
        System.out.println("🔧 Génération des techniciens...");
        
        String sql = """
            INSERT INTO techniciens (nom, prenom, email, telephone, specialites, 
                                   statut, date_creation, date_modification) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        String now = LocalDate.now().format(DATE_FORMAT);
        
        String[][] techniciens = {
            {"Techno", "Lucas", "lucas.techno@magsav.com", "01.11.22.33.44", "Éclairage,Son", "ACTIF"},
            {"Lumière", "Camille", "camille.lumiere@magsav.com", "01.22.33.44.55", "Éclairage", "ACTIF"},
            {"Sonore", "Alex", "alex.sonore@magsav.com", "01.33.44.55.66", "Son,Vidéo", "ACTIF"},
            {"Scénique", "Jordan", "jordan.scenique@magsav.com", "01.44.55.66.77", "Scène,Structure", "ACTIF"}
        };
        
        for (String[] tech : techniciens) {
            stmt.setString(1, tech[0]);
            stmt.setString(2, tech[1]);
            stmt.setString(3, tech[2]);
            stmt.setString(4, tech[3]);
            stmt.setString(5, tech[4]);
            stmt.setString(6, tech[5]);
            stmt.setString(7, now);
            stmt.setString(8, now);
            stmt.executeUpdate();
        }
        
        stmt.close();
        System.out.println("  ✅ " + techniciens.length + " techniciens créés");
    }
    
    private static void generateVehicules(Connection conn) throws SQLException {
        System.out.println("🚐 Génération des véhicules...");
        
        String sql = """
            INSERT INTO vehicules (immatriculation, type_vehicule, marque, modele, 
                                  annee, kilometrage, statut, created_at, updated_at) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        String now = LocalDate.now().format(DATE_FORMAT);
        
        String[][] vehicules = {
            {"AB-123-CD", "Utilitaire", "Renault", "Master", "2020", "45000", "Disponible"},
            {"EF-456-GH", "Camion", "Mercedes", "Sprinter", "2019", "62000", "Disponible"},
            {"IJ-789-KL", "Fourgon", "Ford", "Transit", "2021", "28000", "En mission"},
            {"MN-012-OP", "Utilitaire", "Peugeot", "Boxer", "2018", "78000", "Maintenance"}
        };
        
        for (String[] vehicule : vehicules) {
            stmt.setString(1, vehicule[0]);
            stmt.setString(2, vehicule[1]);
            stmt.setString(3, vehicule[2]);
            stmt.setString(4, vehicule[3]);
            stmt.setInt(5, Integer.parseInt(vehicule[4]));
            stmt.setInt(6, Integer.parseInt(vehicule[5]));
            stmt.setString(7, vehicule[6]);
            stmt.setString(8, now);
            stmt.setString(9, now);
            stmt.executeUpdate();
        }
        
        stmt.close();
        System.out.println("  ✅ " + vehicules.length + " véhicules créés");
    }
    
    private static void generateProduits(Connection conn) throws SQLException {
        System.out.println("📦 Génération des produits...");
        
        String sql = """
            INSERT INTO produits (code_produit, nom_produit, numero_serie, nom_fabricant, 
                                categorie_principale, statut_produit, date_achat, prix_achat) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        String baseDate = LocalDate.now().minusDays(random.nextInt(365)).format(DATE_FORMAT);
        
        String[][] produits = {
            {"LED001", "Projecteur LED 200W", "LED200-2023-001", "ETC", "Éclairage", "DISPONIBLE", "850.00"},
            {"CONSOLE01", "Console ETC Ion", "ION-2022-456", "ETC", "Éclairage", "DISPONIBLE", "12500.00"},
            {"MIC001", "Micro sans fil", "SENN-2023-789", "Sennheiser", "Son", "DISPONIBLE", "650.00"},
            {"CABLE01", "Câble DMX 5m", "DMX5M-001", "Neutrik", "Éclairage", "DISPONIBLE", "25.00"},
            {"SPOT001", "Projecteur traditionnel 1kW", "TRAD1K-123", "Robert Juliat", "Éclairage", "MAINTENANCE", "420.00"}
        };
        
        for (int i = 0; i < produits.length; i++) {
            String[] produit = produits[i];
            stmt.setString(1, produit[0]);
            stmt.setString(2, produit[1]);
            stmt.setString(3, produit[2]);
            stmt.setString(4, produit[3]);
            stmt.setString(5, produit[4]);
            stmt.setString(6, produit[5]);
            stmt.setString(7, baseDate);
            stmt.setString(8, produit[6]);
            stmt.executeUpdate();
        }
        
        stmt.close();
        System.out.println("  ✅ " + produits.length + " produits créés");
    }
    
    private static void generateInterventions(Connection conn) throws SQLException {
        System.out.println("🔧 Génération des interventions...");
        
        String sql = """
            INSERT INTO interventions (produit_id, statut_intervention, description_panne, 
                                     date_entree, detecteur_societe_id) 
            VALUES (?, ?, ?, ?, ?)
            """;
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        String baseDate = LocalDate.now().minusDays(random.nextInt(30)).format(DATE_FORMAT);
        
        String[] pannes = {
            "Dysfonctionnement LED - scintillement",
            "Panne alimentation - ne s'allume plus",
            "Problème DMX - ne répond plus aux commandes",
            "Surchauffe détectée",
            "Maintenance préventive"
        };
        
        for (int i = 1; i <= 5; i++) {
            stmt.setInt(1, i); // produit_id
            stmt.setString(2, random.nextBoolean() ? "EN_COURS" : "TERMINE");
            stmt.setString(3, pannes[random.nextInt(pannes.length)]);
            stmt.setString(4, baseDate);
            stmt.setInt(5, random.nextInt(3) + 1); // societe_id
            stmt.executeUpdate();
        }
        
        stmt.close();
        System.out.println("  ✅ 5 interventions créées");
    }
    
    private static void generateDemandesIntervention(Connection conn) throws SQLException {
        System.out.println("📝 Génération des demandes d'intervention...");
        
        // Créer d'abord la table si elle n'existe pas
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS demandes_intervention (
                id INTEGER AUTO_INCREMENT PRIMARY KEY,
                statut VARCHAR(50) DEFAULT 'EN_ATTENTE',
                type_demande VARCHAR(50) NOT NULL DEFAULT 'INTERVENTIONS',
                product_id BIGINT,
                produit_nom VARCHAR(255),
                produit_sn VARCHAR(100),
                produit_uid VARCHAR(100),
                produit_fabricant VARCHAR(255),
                produit_category VARCHAR(100),
                produit_subcategory VARCHAR(100),
                produit_description TEXT,
                type_proprietaire VARCHAR(50),
                proprietaire_id BIGINT,
                demande_creation_proprietaire_id BIGINT,
                proprietaire_nom_temp VARCHAR(255),
                proprietaire_details_temp TEXT,
                panne_description TEXT,
                client_note TEXT,
                detecteur VARCHAR(255),
                detector_societe_id BIGINT,
                demandeur_nom VARCHAR(255),
                date_demande TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        
        conn.createStatement().executeUpdate(createTableSQL);
        
        // Insérer quelques demandes d'intervention de test
        String sql = """
            INSERT INTO demandes_intervention (
                statut, type_demande, produit_nom, produit_sn, produit_uid,
                panne_description, client_note, demandeur_nom, detector_societe_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        
        String[][] demandes = {
            {"EN_ATTENTE", "INTERVENTIONS", "Console Audio YAMAHA M7CL", "YM7CL-2023-001", "UID-YAMAHA-001", 
             "Problème de grésillement sur le canal 8", "Urgent pour spectacle de demain", "Marie Productions", "1"},
            {"EN_COURS", "INTERVENTIONS", "Projecteur LED ARRI L7-C", "ARRI-LED-2022-156", "UID-ARRI-156",
             "Projecteur qui ne s'allume plus", "Remplacement nécessaire", "Théâtre Municipal", "2"},
            {"EN_ATTENTE", "INTERVENTIONS", "Micro HF SHURE QLXD", "SHURE-QLXD-789", "UID-SHURE-789",
             "Coupures intermittentes", "Fréquences à vérifier", "Studio Son", "3"}
        };
        
        for (String[] demande : demandes) {
            stmt.setString(1, demande[0]);
            stmt.setString(2, demande[1]);
            stmt.setString(3, demande[2]);
            stmt.setString(4, demande[3]);
            stmt.setString(5, demande[4]);
            stmt.setString(6, demande[5]);
            stmt.setString(7, demande[6]);
            stmt.setString(8, demande[7]);
            stmt.setString(9, demande[8]);
            stmt.executeUpdate();
        }
        
        stmt.close();
        System.out.println("  ✅ " + demandes.length + " demandes d'intervention créées");
    }
    
    private static void generateRequests(Connection conn) throws SQLException {
        System.out.println("📝 Génération des demandes...");
        
        // Récupérer les IDs réels des sociétés
        PreparedStatement idQuery = conn.prepareStatement("SELECT id FROM societes ORDER BY id LIMIT 4");
        ResultSet rs = idQuery.executeQuery();
        List<Integer> societeIdsList = new ArrayList<>();
        while (rs.next()) {
            societeIdsList.add(rs.getInt("id"));
        }
        rs.close();
        idQuery.close();
        
        if (societeIdsList.isEmpty()) {
            System.out.println("  ⚠️  Aucune société trouvée pour générer les demandes");
            return;
        }
        
        String sql = """
            INSERT INTO requests (type, title, description, status, priority, 
                                requester_name, requester_email, societe_id, 
                                created_at, updated_at) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        String now = LocalDate.now().format(DATE_FORMAT);
        
        String[][] requests = {
            {"PIECE", "Lampe LED défectueuse", "Besoin d'une lampe LED de remplacement pour projecteur PAR64", "EN_ATTENTE", "HAUTE", "Jean Dupont", "j.dupont@theatre.fr"},
            {"MATERIEL", "Location console d'éclairage", "Demande de location console ETC Ion pour spectacle", "EN_COURS", "NORMALE", "Marie Martin", "m.martin@productions.com"},
            {"INTERVENTION", "Problème de son", "Grésillement sur le canal 3 de la console", "EN_ATTENTE", "NORMALE", "Pierre Durand", "p.durand@eclairage-pro.fr"},
            {"PIECE", "Câbles XLR endommagés", "Remplacement de 5 câbles XLR 3m suite à détérioration", "NOUVEAU", "BASSE", "Sophie Laurent", "s.laurent@email.com"},
            {"MATERIEL", "Projecteurs LED supplémentaires", "Demande de 10 projecteurs LED pour événement", "EN_ATTENTE", "NORMALE", "Anne Bernard", "a.bernard@email.com"},
            {"INTERVENTION", "Réparation projecteur LED", "Le projecteur LED ne fonctionne plus correctement", "EN_ATTENTE", "HAUTE", "Jean Dupont", "j.dupont@theatre.fr"}
        };
        
        for (int i = 0; i < requests.length; i++) {
            String[] request = requests[i];
            stmt.setString(1, request[0]);
            stmt.setString(2, request[1]);
            stmt.setString(3, request[2]);
            stmt.setString(4, request[3]);
            stmt.setString(5, request[4]);
            stmt.setString(6, request[5]);
            stmt.setString(7, request[6]);
            // Utiliser les sociétés en boucle si on en a moins que de demandes
            stmt.setInt(8, societeIdsList.get(i % societeIdsList.size()));
            stmt.setString(9, now);
            stmt.setString(10, now);
            stmt.executeUpdate();
        }
        
        stmt.close();
        System.out.println("  ✅ " + requests.length + " demandes créées");
    }
    
    private static void generateAffaires(Connection conn) throws SQLException {
        System.out.println("💼 Génération des affaires...");
        
        // Récupérer les IDs réels des sociétés
        PreparedStatement idQuery = conn.prepareStatement("SELECT id FROM societes ORDER BY id LIMIT 3");
        ResultSet rs = idQuery.executeQuery();
        int[] societeIds = new int[3];
        int index = 0;
        while (rs.next() && index < 3) {
            societeIds[index++] = rs.getInt("id");
        }
        rs.close();
        idQuery.close();
        
        if (index < 3) {
            System.out.println("  ⚠️  Pas assez de sociétés pour générer toutes les affaires");
            return;
        }
        
        String sql = """
            INSERT INTO affaires (reference, nom, description, client_id, client_nom, 
                                statut, type, priorite, montant_estime, 
                                date_creation, technicien_responsable) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        String now = LocalDate.now().format(DATE_FORMAT);
        
        String[][] affaires = {
            {"AFF-2025-001", "Spectacle Théâtre National", "Installation complète éclairage pour nouvelle saison", "Théâtre National", "EN_COURS", "SPECTACLE", "HAUTE", "25000.00", "Jean Dupont"},
            {"AFF-2025-002", "Concert Productions Scène", "Sonorisation concert en plein air", "Productions Scène", "PROSPECTION", "CONCERT", "NORMALE", "15000.00", "Marie Martin"},
            {"AFF-2025-003", "Maintenance Éclairage Pro", "Contrat de maintenance annuel", "Éclairage Pro", "SIGNEE", "MAINTENANCE", "NORMALE", "8000.00", "Pierre Durand"}
        };
        
        for (int i = 0; i < affaires.length; i++) {
            String[] affaire = affaires[i];
            stmt.setString(1, affaire[0]);
            stmt.setString(2, affaire[1]);
            stmt.setString(3, affaire[2]);
            stmt.setInt(4, societeIds[i]);
            stmt.setString(5, affaire[3]);
            stmt.setString(6, affaire[4]);
            stmt.setString(7, affaire[5]);
            stmt.setString(8, affaire[6]);
            stmt.setBigDecimal(9, new java.math.BigDecimal(affaire[7]));
            stmt.setString(10, now);
            stmt.setString(11, affaire[8]);
            stmt.executeUpdate();
        }
        
        stmt.close();
        System.out.println("  ✅ " + affaires.length + " affaires créées");
    }
    
    private static void generateDevis(Connection conn) throws SQLException {
        System.out.println("📄 Génération des devis...");
        
        // Récupérer les IDs des affaires existantes
        PreparedStatement selectAffaires = conn.prepareStatement("SELECT id FROM affaires ORDER BY id LIMIT 2");
        ResultSet affairesResult = selectAffaires.executeQuery();
        
        List<Integer> affaireIds = new ArrayList<>();
        while (affairesResult.next()) {
            affaireIds.add(affairesResult.getInt("id"));
        }
        affairesResult.close();
        selectAffaires.close();
        
        // Récupérer les IDs des sociétés existantes pour client_id
        PreparedStatement selectSocietes = conn.prepareStatement("SELECT id FROM societes ORDER BY id LIMIT 2");
        ResultSet societesResult = selectSocietes.executeQuery();
        
        List<Integer> societeIds = new ArrayList<>();
        while (societesResult.next()) {
            societeIds.add(societesResult.getInt("id"));
        }
        societesResult.close();
        selectSocietes.close();
        
        if (affaireIds.isEmpty() || societeIds.isEmpty()) {
            System.out.println("  ⚠️ Aucune affaire ou société trouvée - devis non générés");
            return;
        }
        
        String sql = """
            INSERT INTO devis (numero, affaire_id, client_id, client_nom, objet, 
                             statut, montant_ht, date_creation) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        String now = LocalDate.now().format(DATE_FORMAT);
        
        String[][] devisData = {
            {"DEV-2025-001", "Théâtre National", "Éclairage scène principale", "ENVOYE", "20000.00"},
            {"DEV-2025-002", "Productions Scène", "Sonorisation concert", "BROUILLON", "12000.00"}
        };
        
        for (int i = 0; i < devisData.length && i < affaireIds.size() && i < societeIds.size(); i++) {
            String[] devi = devisData[i];
            stmt.setString(1, devi[0]);
            stmt.setInt(2, affaireIds.get(i));
            stmt.setInt(3, societeIds.get(i));
            stmt.setString(4, devi[1]);
            stmt.setString(5, devi[2]);
            stmt.setString(6, devi[3]);
            stmt.setBigDecimal(7, new java.math.BigDecimal(devi[4]));
            stmt.setString(8, now);
            stmt.executeUpdate();
        }
        
        stmt.close();
        System.out.println("  ✅ " + devisData.length + " devis créés");
    }
    
    private static void generateCommandes(Connection conn) throws SQLException {
        System.out.println("🛒 Génération des commandes...");
        
        String sql = """
            INSERT INTO commandes (numero_commande, fournisseur_id, statut, date_commande, 
                                 montant_ht, montant_tva, montant_ttc, date_creation, date_modification) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        String now = LocalDate.now().format(DATE_FORMAT);
        
        String[][] commandes = {
            {"CMD-2025-001", "1", "LIVREE", "5500.00", "1100.00", "6600.00"},
            {"CMD-2025-002", "2", "EN_COURS", "3200.00", "640.00", "3840.00"}
        };
        
        for (String[] commande : commandes) {
            stmt.setString(1, commande[0]);
            stmt.setInt(2, Integer.parseInt(commande[1]));
            stmt.setString(3, commande[2]);
            stmt.setString(4, now);
            stmt.setBigDecimal(5, new java.math.BigDecimal(commande[3]));
            stmt.setBigDecimal(6, new java.math.BigDecimal(commande[4]));
            stmt.setBigDecimal(7, new java.math.BigDecimal(commande[5]));
            stmt.setString(8, now);
            stmt.setString(9, now);
            stmt.executeUpdate();
        }
        
        stmt.close();
        System.out.println("  ✅ " + commandes.length + " commandes créées");
    }
    
    private static void generatePlanifications(Connection conn) throws SQLException {
        System.out.println("📅 Génération des planifications...");
        
        // Récupérer les IDs des interventions existantes
        PreparedStatement selectInterventions = conn.prepareStatement("SELECT id FROM interventions ORDER BY id LIMIT 3");
        ResultSet interventionsResult = selectInterventions.executeQuery();
        
        List<Integer> interventionIds = new ArrayList<>();
        while (interventionsResult.next()) {
            interventionIds.add(interventionsResult.getInt("id"));
        }
        interventionsResult.close();
        selectInterventions.close();
        
        // Récupérer les IDs des techniciens existants
        PreparedStatement selectTechniciens = conn.prepareStatement("SELECT id FROM techniciens ORDER BY id LIMIT 4");
        ResultSet techniciensResult = selectTechniciens.executeQuery();
        
        List<Integer> technicienIds = new ArrayList<>();
        while (techniciensResult.next()) {
            technicienIds.add(techniciensResult.getInt("id"));
        }
        techniciensResult.close();
        selectTechniciens.close();
        
        // Récupérer les IDs des véhicules existants
        PreparedStatement selectVehicules = conn.prepareStatement("SELECT id FROM vehicules ORDER BY id LIMIT 4");
        ResultSet vehiculesResult = selectVehicules.executeQuery();
        
        List<Integer> vehiculeIds = new ArrayList<>();
        while (vehiculesResult.next()) {
            vehiculeIds.add(vehiculesResult.getInt("id"));
        }
        vehiculesResult.close();
        selectVehicules.close();
        
        if (interventionIds.isEmpty() || technicienIds.isEmpty() || vehiculeIds.isEmpty()) {
            System.out.println("  ⚠️ Données manquantes - planifications non générées");
            return;
        }
        
        String sql = """
            INSERT INTO planifications (intervention_id, technicien_id, vehicule_id, 
                                      date_planifiee, duree_estimee, statut, priorite, 
                                      type_intervention, date_creation, date_modification) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        String now = LocalDate.now().format(DATE_FORMAT);
        String futurDate = LocalDate.now().plusDays(7).format(DATE_FORMAT);
        
        for (int i = 0; i < Math.min(3, interventionIds.size()); i++) {
            stmt.setInt(1, interventionIds.get(i));
            stmt.setInt(2, technicienIds.get(random.nextInt(technicienIds.size())));
            stmt.setInt(3, vehiculeIds.get(random.nextInt(vehiculeIds.size())));
            stmt.setString(4, futurDate);
            stmt.setInt(5, 120); // durée en minutes
            stmt.setString(6, "PLANIFIEE");
            stmt.setString(7, "NORMALE");
            stmt.setString(8, "MAINTENANCE");
            stmt.setString(9, now);
            stmt.setString(10, now);
            stmt.executeUpdate();
        }
        
        stmt.close();
        System.out.println("  ✅ 3 planifications créées");
    }
    
    private static void generateEmailTemplates(Connection conn) throws SQLException {
        System.out.println("📧 Génération des templates email...");
        
        String sql = """
            INSERT INTO email_templates (nom_template, type_template, objet, contenu_html, 
                                       actif, date_creation, date_modification) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        String now = LocalDate.now().format(DATE_FORMAT);
        
        String[][] templates = {
            {"Confirmation Intervention", "INTERVENTION", "Confirmation de votre intervention", "<p>Bonjour,</p><p>Votre intervention est confirmée pour le {{DATE}}.</p><p>Cordialement</p>"},
            {"Devis Envoyé", "DEVIS", "Votre devis MAGSAV", "<p>Bonjour,</p><p>Veuillez trouver ci-joint votre devis n°{{NUMERO}}.</p><p>Cordialement</p>"},
            {"Rappel Maintenance", "MAINTENANCE", "Rappel maintenance", "<p>Bonjour,</p><p>Nous vous rappelons que la maintenance de votre matériel est prévue le {{DATE}}.</p><p>Cordialement</p>"}
        };
        
        for (String[] template : templates) {
            stmt.setString(1, template[0]);
            stmt.setString(2, template[1]);
            stmt.setString(3, template[2]);
            stmt.setString(4, template[3]);
            stmt.setBoolean(5, true);
            stmt.setString(6, now);
            stmt.setString(7, now);
            stmt.executeUpdate();
        }
        
        stmt.close();
        System.out.println("  ✅ " + templates.length + " templates email créés");
    }
}