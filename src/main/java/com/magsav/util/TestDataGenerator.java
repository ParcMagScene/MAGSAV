package com.magsav.util;

import com.magsav.db.DB;
import com.magsav.exception.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Générateur de données de test pour toutes les tables de MAGSAV
 */
public class TestDataGenerator {
    
    private static final Random random = new Random();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Données de référence pour génération réaliste
    private static final List<String> FABRICANTS = Arrays.asList(
        "Yamaha", "Pioneer", "JBL", "Sennheiser", "Shure", "AKG", "Audio-Technica", 
        "Behringer", "Mackie", "QSC", "Crown", "Martin Audio", "L-Acoustics", "d&b audiotechnik"
    );
    
    private static final List<String> CATEGORIES_PRINCIPALES = Arrays.asList(
        "🎤 Microphones", "🔊 Haut-parleurs", "🎛️ Consoles", "🎧 Monitoring", 
        "📡 HF/Radio", "💡 Éclairage", "🎵 Instruments", "🔌 Connectique", "⚡ Alimentation"
    );
    
    private static final List<String> SOUS_CATEGORIES = Arrays.asList(
        "Microphones à main", "Microphones serre-tête", "Enceintes actives", "Enceintes passives",
        "Consoles numériques", "Consoles analogiques", "Retours de scène", "Casques", 
        "Récepteurs HF", "Émetteurs HF", "Projecteurs LED", "Projecteurs traditionnels"
    );
    
    private static final List<String> TYPES_SOCIETES = Arrays.asList(
        "CLIENT", "FOURNISSEUR", "PARTENAIRE", "SOUS_TRAITANT"
    );
    
    private static final List<String> STATUTS_INTERVENTION = Arrays.asList(
        "EN_ATTENTE", "EN_COURS", "TERMINE", "ANNULE", "REPORTE"
    );
    
    // Constantes pour le système de demandes
    private static final List<String> TYPES_DEMANDES = Arrays.asList(
        "INTERVENTION", "PIECES", "MATERIEL", "SAV_EXTERNE", "DEVIS", "PRIX"
    );
    
    private static final List<String> STATUTS_DEMANDES = Arrays.asList(
        "EN_ATTENTE", "EN_COURS", "VALIDEE", "REFUSEE", "TERMINEE"
    );
    
    private static final List<String> PRIORITES_DEMANDES = Arrays.asList(
        "BASSE", "NORMALE", "HAUTE", "URGENTE"
    );
    
    private static final List<String> TYPES_ITEMS = Arrays.asList(
        "PIECE", "MATERIEL", "SERVICE"
    );
    
    private static final List<String> STATUTS_ITEMS = Arrays.asList(
        "EN_ATTENTE", "COMMANDE", "RECU", "INSTALLE"
    );
    
    private static final List<String> TYPES_VEHICULES = Arrays.asList(
        "VL", "PL", "SPL", "REMORQUE", "SCENE_MOBILE"
    );
    
    private static final List<String> MARQUES_VEHICULES = Arrays.asList(
        "Renault", "Peugeot", "Ford", "Mercedes", "Volkswagen", "Iveco", "MAN"
    );
    
    private static final List<String> SPECIALITES_TECHNICIENS = Arrays.asList(
        "[\"Audio\", \"Vidéo\"]", "[\"Éclairage\", \"Automatisation\"]", 
        "[\"Audio\", \"HF\"]", "[\"Vidéo\", \"Streaming\"]", "[\"Éclairage\"]", "[\"Audio\"]"
    );
    
    private static final List<String> PRENOMS = Arrays.asList(
        "Jean", "Pierre", "Marie", "Sophie", "Antoine", "Camille", "Lucas", "Emma", 
        "Thomas", "Léa", "Nicolas", "Julie", "Alexandre", "Sarah", "Maxime", "Clara"
    );
    
    private static final List<String> NOMS = Arrays.asList(
        "Martin", "Bernard", "Dubois", "Thomas", "Robert", "Richard", "Petit", "Durand", 
        "Leroy", "Moreau", "Simon", "Laurent", "Lefebvre", "Michel", "Garcia", "David"
    );
    
    /**
     * Génère un jeu de données complet pour toutes les tables
     */
    public static void generateCompleteTestData() {
        try {
            System.out.println("🎯 Génération des données de test MAGSAV...");
            
            // Vider les tables existantes (optionnel - décommenter si nécessaire)
            // clearAllTables();
            
            // Générer les données dans l'ordre des dépendances
            generateCategories(20);
            generateSocietes(50);
            generateTechniciens(10);
            generateVehicules(8);
            generateProduits(100);
            generateInterventions(30);
            generatePlanifications(25);
            generateCommandes(15);
            generateLignesCommandes(45);
            generateMouvementsStock(80);
            generateAlertesStock(12);
            generateDisponibilitesTechniciens(20);
            generateCommunications(35);
            generateSavHistory(40);
            generateDemandes(30);
            generateItemsDemandes(90);
            generateEmailTemplates();
            
            System.out.println("✅ Génération terminée avec succès !");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la génération des données de test: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Génère des catégories hiérarchiques
     */
    private static void generateCategories(int count) throws SQLException {
        System.out.println("📂 Génération des catégories...");
        
        try (Connection conn = DB.getConnection()) {
            String sql = "INSERT INTO categories (nom_categorie, parent_id) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            // Catégories principales (sans parent)
            for (int i = 0; i < Math.min(count / 2, CATEGORIES_PRINCIPALES.size()); i++) {
                stmt.setString(1, CATEGORIES_PRINCIPALES.get(i));
                stmt.setNull(2, java.sql.Types.INTEGER);
                stmt.executeUpdate();
            }
            
            // Sous-catégories (avec parent)
            for (int i = 0; i < count / 2; i++) {
                int parentId = random.nextInt(Math.min(count / 2, CATEGORIES_PRINCIPALES.size())) + 1;
                stmt.setString(1, SOUS_CATEGORIES.get(i % SOUS_CATEGORIES.size()));
                stmt.setInt(2, parentId);
                stmt.executeUpdate();
            }
        }
        System.out.println("✅ " + count + " catégories générées");
    }
    
    /**
     * Génère des sociétés (clients, fournisseurs, etc.) dans la table societes
     */
    private static void generateSocietes(int count) throws SQLException {
        System.out.println("🏢 Génération des sociétés...");
        
        try (Connection conn = DB.getConnection()) {
            // Vérifier si Mag Scène existe déjà pour éviter les doublons
            String checkSql = "SELECT COUNT(*) FROM societes WHERE nom_societe = 'Mag Scène'";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            boolean magSceneExists = false;
            try {
                var rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    magSceneExists = true;
                }
            } catch (SQLException e) {
                // Ignore l'erreur si la table n'existe pas encore
            }
            
            String sql = "INSERT INTO societes (type_societe, nom_societe, email_societe, telephone_societe, adresse_societe, notes_societe) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            String[] companyTypes = {"CLIENT", "FOURNISSEUR", "FABRICANT", "PARTENAIRE"};
            String[] sectors = {"Audiovisuel", "Informatique", "Éclairage", "Sonorisation", "Vidéo", "Multimédia"};
            
            for (int i = 1; i <= count; i++) {
                String companyName = generateCompanyName();
                String type = companyTypes[random.nextInt(companyTypes.length)];
                String fullAddress = generateAddress();
                String phone = generatePhoneNumber();
                String email = companyName.toLowerCase()
                    .replaceAll(" ", "")
                    .replaceAll("[^a-z0-9]", "") + "@example.com";
                String description = "Société spécialisée en " + sectors[random.nextInt(sectors.length)] + 
                                   ". Contact: " + email + 
                                   (random.nextBoolean() ? ". Site web: www." + companyName.toLowerCase().replaceAll(" ", "").replaceAll("[^a-z0-9]", "") + ".fr" : "");
                
                stmt.setString(1, type);
                stmt.setString(2, companyName);
                stmt.setString(3, email);
                stmt.setString(4, phone);
                stmt.setString(5, fullAddress);
                stmt.setString(6, description);
                stmt.executeUpdate();
            }
        }
        System.out.println("✅ " + count + " sociétés générées");
    }
    
    /**
     * Génère des techniciens
     */
    private static void generateTechniciens(int count) throws SQLException {
        System.out.println("👨‍🔧 Génération des techniciens...");
        
        try (Connection conn = DB.getConnection()) {
            String sql = "INSERT INTO techniciens (nom, prenom, email, telephone, specialites, statut, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            for (int i = 1; i <= count; i++) {
                String nom = NOMS.get(random.nextInt(NOMS.size()));
                String prenom = PRENOMS.get(random.nextInt(PRENOMS.size()));
                String email = prenom.toLowerCase() + "." + nom.toLowerCase() + "@magsav.com";
                String telephone = generatePhoneNumber();
                String specialites = SPECIALITES_TECHNICIENS.get(random.nextInt(SPECIALITES_TECHNICIENS.size()));
                String statut = random.nextDouble() > 0.1 ? "ACTIF" : "CONGE";
                String notes = "Technicien spécialisé généré automatiquement";
                
                stmt.setString(1, nom);
                stmt.setString(2, prenom);
                stmt.setString(3, email);
                stmt.setString(4, telephone);
                stmt.setString(5, specialites);
                stmt.setString(6, statut);
                stmt.setString(7, notes);
                stmt.executeUpdate();
            }
        }
        System.out.println("✅ " + count + " techniciens générés");
    }
    
    /**
     * Génère des véhicules
     */
    private static void generateVehicules(int count) throws SQLException {
        System.out.println("🚐 Génération des véhicules...");
        
        try (Connection conn = DB.getConnection()) {
            String sql = "INSERT INTO vehicules (immatriculation, type_vehicule, marque, modele, annee, kilometrage, statut, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            for (int i = 1; i <= count; i++) {
                String immatriculation = generateImmatriculation();
                String typeVehicule = TYPES_VEHICULES.get(random.nextInt(TYPES_VEHICULES.size()));
                String marque = MARQUES_VEHICULES.get(random.nextInt(MARQUES_VEHICULES.size()));
                String modele = generateModele(marque);
                int annee = 2015 + random.nextInt(9); // 2015-2023
                int kilometrage = random.nextInt(200000);
                String statut = random.nextDouble() > 0.2 ? "DISPONIBLE" : "EN_SERVICE";
                String notes = "Véhicule " + typeVehicule + " généré automatiquement";
                
                stmt.setString(1, immatriculation);
                stmt.setString(2, typeVehicule);
                stmt.setString(3, marque);
                stmt.setString(4, modele);
                stmt.setInt(5, annee);
                stmt.setInt(6, kilometrage);
                stmt.setString(7, statut);
                stmt.setString(8, notes);
                stmt.executeUpdate();
            }
        }
        System.out.println("✅ " + count + " véhicules générés");
    }
    
    /**
     * Génère des produits
     */
    private static void generateProduits(int count) throws SQLException {
        System.out.println("📦 Génération des produits...");
        
        try (Connection conn = DB.getConnection()) {
            String sql = "INSERT INTO produits (code_produit, nom_produit, numero_serie, nom_fabricant, uid_unique, statut_produit, categorie_principale, sous_categorie, description_produit, date_achat, nom_client, prix_achat, duree_garantie) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            for (int i = 1; i <= count; i++) {
                String fabricant = FABRICANTS.get(random.nextInt(FABRICANTS.size()));
                String codeProduit = fabricant.substring(0, 3).toUpperCase() + String.format("%04d", i);
                String nomProduit = generateProductName(fabricant);
                String numeroSerie = generateSerialNumber();
                String uid = generateUID();
                String statut = random.nextDouble() > 0.1 ? "EN_STOCK" : "EN_MAINTENANCE";
                String categorie = CATEGORIES_PRINCIPALES.get(random.nextInt(CATEGORIES_PRINCIPALES.size()));
                String sousCategorie = SOUS_CATEGORIES.get(random.nextInt(SOUS_CATEGORIES.size()));
                String description = "Produit " + fabricant + " - " + nomProduit + " généré automatiquement";
                String dateAchat = generateRandomDate(2020, 2023);
                String nomClient = "Client " + (random.nextInt(50) + 1);
                String prixAchat = String.valueOf(100 + random.nextInt(5000));
                String dureeGarantie = (1 + random.nextInt(5)) + " ans";
                
                stmt.setString(1, codeProduit);
                stmt.setString(2, nomProduit);
                stmt.setString(3, numeroSerie);
                stmt.setString(4, fabricant);
                stmt.setString(5, uid);
                stmt.setString(6, statut);
                stmt.setString(7, categorie);
                stmt.setString(8, sousCategorie);
                stmt.setString(9, description);
                stmt.setString(10, dateAchat);
                stmt.setString(11, nomClient);
                stmt.setString(12, prixAchat);
                stmt.setString(13, dureeGarantie);
                stmt.executeUpdate();
            }
        }
        System.out.println("✅ " + count + " produits générés");
    }
    
    /**
     * Génère des interventions
     */
    private static void generateInterventions(int count) throws SQLException {
        System.out.println("🔧 Génération des interventions...");
        
        try (Connection conn = DB.getConnection()) {
            String sql = "INSERT INTO interventions (produit_id, statut_intervention, description_panne, numero_serie_intervention, note_client, description_defaut, nom_detecteur, date_entree, date_sortie) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            for (int i = 1; i <= count; i++) {
                int produitId = random.nextInt(100) + 1; // Référence aux produits générés
                String statut = STATUTS_INTERVENTION.get(random.nextInt(STATUTS_INTERVENTION.size()));
                String descriptionPanne = generatePanneDescription();
                String numeroSerie = generateSerialNumber();
                String noteClient = "Note client générée automatiquement pour l'intervention " + i;
                String descriptionDefaut = generateDefautDescription();
                String nomDetecteur = PRENOMS.get(random.nextInt(PRENOMS.size())) + " " + NOMS.get(random.nextInt(NOMS.size()));
                String dateEntree = generateRandomDateTime(2023, 2024);
                String dateSortie = statut.equals("TERMINE") ? generateRandomDateTime(2023, 2024) : null;
                
                stmt.setInt(1, produitId);
                stmt.setString(2, statut);
                stmt.setString(3, descriptionPanne);
                stmt.setString(4, numeroSerie);
                stmt.setString(5, noteClient);
                stmt.setString(6, descriptionDefaut);
                stmt.setString(7, nomDetecteur);
                stmt.setString(8, dateEntree);
                if (dateSortie != null) {
                    stmt.setString(9, dateSortie);
                } else {
                    stmt.setNull(9, java.sql.Types.VARCHAR);
                }
                stmt.executeUpdate();
            }
        }
        System.out.println("✅ " + count + " interventions générées");
    }
    
    /**
     * Génère des planifications (optionnel car dépend des interventions)
     */
    private static void generatePlanifications(int count) throws SQLException {
        System.out.println("📅 Génération des planifications...");
        
        try (Connection conn = DB.getConnection()) {
            String sql = "INSERT INTO planifications (intervention_id, technicien_id, vehicule_id, client_id, date_planifiee, duree_estimee, statut, priorite, type_intervention, lieu_intervention, notes_planification) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            String[] statuts = {"PLANIFIE", "EN_COURS", "TERMINE", "ANNULE"};
            String[] priorites = {"URGENTE", "HAUTE", "NORMALE", "BASSE"};
            String[] types = {"MAINTENANCE", "DEPANNAGE", "INSTALLATION", "CONTROLE"};
            
            for (int i = 1; i <= count; i++) {
                int interventionId = random.nextInt(30) + 1;
                int technicienId = random.nextInt(10) + 1;
                int vehiculeId = random.nextInt(8) + 1;
                int clientId = random.nextInt(50) + 1; // Référence vers societes.id
                String datePlanifiee = generateRandomDateTime(2024, 2024);
                int dureeEstimee = 60 + random.nextInt(240); // 1-4 heures
                String statut = statuts[random.nextInt(statuts.length)];
                String priorite = priorites[random.nextInt(priorites.length)];
                String type = types[random.nextInt(types.length)];
                String lieu = generateAddress();
                String notes = "Planification générée automatiquement - " + type;
                
                stmt.setInt(1, interventionId);
                stmt.setInt(2, technicienId);
                stmt.setInt(3, vehiculeId);
                stmt.setInt(4, clientId);
                stmt.setString(5, datePlanifiee);
                stmt.setInt(6, dureeEstimee);
                stmt.setString(7, statut);
                stmt.setString(8, priorite);
                stmt.setString(9, type);
                stmt.setString(10, lieu);
                stmt.setString(11, notes);
                stmt.executeUpdate();
            }
        }
        System.out.println("✅ " + count + " planifications générées");
    }
    
    /**
     * Génère des commandes
     */
    private static void generateCommandes(int count) throws SQLException {
        System.out.println("🛒 Génération des commandes...");
        
        try (Connection conn = DB.getConnection()) {
            String sql = "INSERT INTO commandes (numero_commande, fournisseur_id, statut, date_commande, date_livraison_prevue, montant_ht, montant_tva, montant_ttc, conditions_paiement, adresse_livraison, notes_commande) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            String[] statuts = {"BROUILLON", "VALIDEE", "ENVOYEE", "RECUE", "ANNULEE"};
            
            for (int i = 1; i <= count; i++) {
                String numeroCommande = "CMD-" + String.format("%06d", i);
                int fournisseurId = random.nextInt(50) + 1; // Référence vers societes.id
                String statut = statuts[random.nextInt(statuts.length)];
                String dateCommande = generateRandomDate(2024, 2024);
                String dateLivraisonPrevue = generateRandomDate(2024, 2024);
                double montantHT = 100 + random.nextDouble() * 5000;
                double montantTVA = montantHT * 0.20;
                double montantTTC = montantHT + montantTVA;
                String conditionsPaiement = "30 jours fin de mois";
                String adresseLivraison = generateAddress();
                String notes = "Commande générée automatiquement";
                
                stmt.setString(1, numeroCommande);
                stmt.setInt(2, fournisseurId);
                stmt.setString(3, statut);
                stmt.setString(4, dateCommande);
                stmt.setString(5, dateLivraisonPrevue);
                stmt.setDouble(6, montantHT);
                stmt.setDouble(7, montantTVA);
                stmt.setDouble(8, montantTTC);
                stmt.setString(9, conditionsPaiement);
                stmt.setString(10, adresseLivraison);
                stmt.setString(11, notes);
                stmt.executeUpdate();
            }
        }
        System.out.println("✅ " + count + " commandes générées");
    }
    
    /**
     * Génère des lignes de commandes
     */
    private static void generateLignesCommandes(int count) throws SQLException {
        System.out.println("📋 Génération des lignes de commandes...");
        
        try (Connection conn = DB.getConnection()) {
            String sql = "INSERT INTO lignes_commandes (commande_id, produit_id, reference_fournisseur, designation, quantite_commandee, quantite_recue, prix_unitaire_ht, taux_tva, montant_ligne_ht, montant_ligne_ttc, statut_ligne, notes_ligne) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            String[] statuts = {"COMMANDEE", "PARTIELLE", "RECUE", "ANNULEE"};
            
            for (int i = 1; i <= count; i++) {
                int commandeId = random.nextInt(15) + 1;
                int produitId = random.nextInt(100) + 1;
                String referenceFournisseur = "REF-" + String.format("%06d", i);
                String designation = "Article " + FABRICANTS.get(random.nextInt(FABRICANTS.size()));
                int quantiteCommandee = 1 + random.nextInt(10);
                int quantiteRecue = random.nextInt(quantiteCommandee + 1);
                double prixUnitaireHT = 10 + random.nextDouble() * 500;
                double tauxTVA = 20.0;
                double montantLigneHT = quantiteCommandee * prixUnitaireHT;
                double montantLigneTTC = montantLigneHT * (1 + tauxTVA / 100);
                String statutLigne = statuts[random.nextInt(statuts.length)];
                String notesLigne = "Ligne générée automatiquement";
                
                stmt.setInt(1, commandeId);
                stmt.setInt(2, produitId);
                stmt.setString(3, referenceFournisseur);
                stmt.setString(4, designation);
                stmt.setInt(5, quantiteCommandee);
                stmt.setInt(6, quantiteRecue);
                stmt.setDouble(7, prixUnitaireHT);
                stmt.setDouble(8, tauxTVA);
                stmt.setDouble(9, montantLigneHT);
                stmt.setDouble(10, montantLigneTTC);
                stmt.setString(11, statutLigne);
                stmt.setString(12, notesLigne);
                stmt.executeUpdate();
            }
        }
        System.out.println("✅ " + count + " lignes de commandes générées");
    }
    
    /**
     * Génère des mouvements de stock
     */
    private static void generateMouvementsStock(int count) throws SQLException {
        System.out.println("📊 Génération des mouvements de stock...");
        
        try (Connection conn = DB.getConnection()) {
            String sql = "INSERT INTO mouvements_stock (produit_id, type_mouvement, quantite, stock_avant, stock_après, cout_unitaire, valeur_mouvement, motif, reference_document, utilisateur, date_mouvement) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            String[] types = {"ENTREE", "SORTIE", "AJUSTEMENT", "INVENTAIRE"};
            String[] motifs = {"Livraison fournisseur", "Sortie intervention", "Correction inventaire", "Contrôle annuel"};
            
            for (int i = 1; i <= count; i++) {
                int produitId = random.nextInt(100) + 1;
                String typeMouvement = types[random.nextInt(types.length)];
                int quantite = typeMouvement.equals("SORTIE") ? -(1 + random.nextInt(5)) : (1 + random.nextInt(10));
                int stockAvant = random.nextInt(50);
                int stockApres = stockAvant + quantite;
                double coutUnitaire = 10 + random.nextDouble() * 200;
                double valeurMouvement = Math.abs(quantite) * coutUnitaire;
                String motif = motifs[random.nextInt(motifs.length)];
                String referenceDocument = "DOC-" + String.format("%06d", i);
                String utilisateur = "admin";
                String dateMouvement = generateRandomDateTime(2024, 2024);
                
                stmt.setInt(1, produitId);
                stmt.setString(2, typeMouvement);
                stmt.setInt(3, quantite);
                stmt.setInt(4, stockAvant);
                stmt.setInt(5, stockApres);
                stmt.setDouble(6, coutUnitaire);
                stmt.setDouble(7, valeurMouvement);
                stmt.setString(8, motif);
                stmt.setString(9, referenceDocument);
                stmt.setString(10, utilisateur);
                stmt.setString(11, dateMouvement);
                stmt.executeUpdate();
            }
        }
        System.out.println("✅ " + count + " mouvements de stock générés");
    }
    
    /**
     * Génère des alertes de stock
     */
    private static void generateAlertesStock(int count) throws SQLException {
        System.out.println("⚠️ Génération des alertes de stock...");
        
        try (Connection conn = DB.getConnection()) {
            String sql = "INSERT INTO alertes_stock (produit_id, type_alerte, seuil_alerte, stock_actuel, statut_alerte, action_prise, notification_envoyee) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            String[] types = {"STOCK_BAS", "RUPTURE", "SURAPPROVISIONNEMENT"};
            String[] statuts = {"ACTIVE", "TRAITEE", "IGNOREE"};
            
            for (int i = 1; i <= count; i++) {
                int produitId = random.nextInt(100) + 1;
                String typeAlerte = types[random.nextInt(types.length)];
                int seuilAlerte = 5 + random.nextInt(15);
                int stockActuel = typeAlerte.equals("STOCK_BAS") ? random.nextInt(seuilAlerte) : 
                                 typeAlerte.equals("RUPTURE") ? 0 : seuilAlerte + random.nextInt(50);
                String statutAlerte = statuts[random.nextInt(statuts.length)];
                String actionPrise = statutAlerte.equals("TRAITEE") ? "Commande passée" : null;
                boolean notificationEnvoyee = random.nextBoolean();
                
                stmt.setInt(1, produitId);
                stmt.setString(2, typeAlerte);
                stmt.setInt(3, seuilAlerte);
                stmt.setInt(4, stockActuel);
                stmt.setString(5, statutAlerte);
                if (actionPrise != null) {
                    stmt.setString(6, actionPrise);
                } else {
                    stmt.setNull(6, java.sql.Types.VARCHAR);
                }
                stmt.setBoolean(7, notificationEnvoyee);
                stmt.executeUpdate();
            }
        }
        System.out.println("✅ " + count + " alertes de stock générées");
    }
    
    /**
     * Génère des disponibilités des techniciens
     */
    private static void generateDisponibilitesTechniciens(int count) throws SQLException {
        System.out.println("📅 Génération des disponibilités des techniciens...");
        
        try (Connection conn = DB.getConnection()) {
            String sql = "INSERT INTO disponibilites_techniciens (technicien_id, date_debut, date_fin, type_indispo, motif) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            String[] types = {"CONGE", "FORMATION", "MALADIE", "AUTRE"};
            String[] motifs = {"Congés payés", "Formation technique", "Arrêt maladie", "Personnel"};
            
            for (int i = 1; i <= count; i++) {
                int technicienId = random.nextInt(10) + 1;
                String dateDebut = generateRandomDateTime(2024, 2024);
                String dateFin = generateRandomDateTime(2024, 2024);
                String typeIndispo = types[random.nextInt(types.length)];
                String motif = motifs[random.nextInt(motifs.length)];
                
                stmt.setInt(1, technicienId);
                stmt.setString(2, dateDebut);
                stmt.setString(3, dateFin);
                stmt.setString(4, typeIndispo);
                stmt.setString(5, motif);
                stmt.executeUpdate();
            }
        }
        System.out.println("✅ " + count + " disponibilités générées");
    }
    
    /**
     * Génère des communications
     */
    private static void generateCommunications(int count) throws SQLException {
        System.out.println("📧 Génération des communications...");
        
        try (Connection conn = DB.getConnection()) {
            String sql = "INSERT INTO communications (planification_id, intervention_id, client_id, technicien_id, type_communication, statut, objet, contenu, destinataires) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            String[] types = {"EMAIL", "SMS", "APPEL", "REUNION"};
            String[] statuts = {"PLANIFIE", "ENVOYE", "RECU", "ECHEC"};
            
            for (int i = 1; i <= count; i++) {
                int planificationId = random.nextInt(25) + 1;
                int interventionId = random.nextInt(30) + 1;
                int clientId = random.nextInt(50) + 1; // Référence vers societes.id
                int technicienId = random.nextInt(10) + 1;
                String typeCommunication = types[random.nextInt(types.length)];
                String statut = statuts[random.nextInt(statuts.length)];
                String objet = "Communication " + typeCommunication + " - Intervention " + interventionId;
                String contenu = "Contenu de la communication générée automatiquement";
                String destinataires = "[\"client@example.com\", \"technicien@magsav.com\"]";
                
                stmt.setInt(1, planificationId);
                stmt.setInt(2, interventionId);
                stmt.setInt(3, clientId);
                stmt.setInt(4, technicienId);
                stmt.setString(5, typeCommunication);
                stmt.setString(6, statut);
                stmt.setString(7, objet);
                stmt.setString(8, contenu);
                stmt.setString(9, destinataires);
                stmt.executeUpdate();
            }
        }
        System.out.println("✅ " + count + " communications générées");
    }
    
    /**
     * Génère l'historique SAV
     */
    private static void generateSavHistory(int count) throws SQLException {
        System.out.println("📜 Génération de l'historique SAV...");
        
        try (Connection conn = DB.getConnection()) {
            String sql = "INSERT INTO sav_history (produit_id, sav_externe_id, date_debut, date_fin, statut_historique, notes_historique) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            String[] statuts = {"EN_COURS", "TERMINE", "ABANDONNE", "REPORTE"};
            
            for (int i = 1; i <= count; i++) {
                int produitId = random.nextInt(100) + 1;
                int savExterneId = random.nextInt(50) + 1;
                String dateDebut = generateRandomDate(2023, 2024);
                String dateFin = random.nextBoolean() ? generateRandomDate(2023, 2024) : null;
                String statutHistorique = statuts[random.nextInt(statuts.length)];
                String notesHistorique = "Historique SAV généré automatiquement - " + statutHistorique;
                
                stmt.setInt(1, produitId);
                stmt.setInt(2, savExterneId);
                stmt.setString(3, dateDebut);
                if (dateFin != null) {
                    stmt.setString(4, dateFin);
                } else {
                    stmt.setNull(4, java.sql.Types.VARCHAR);
                }
                stmt.setString(5, statutHistorique);
                stmt.setString(6, notesHistorique);
                stmt.executeUpdate();
            }
        }
        System.out.println("✅ " + count + " entrées d'historique SAV générées");
    }
    
    /**
     * Génère des templates d'emails
     */
    private static void generateEmailTemplates() throws SQLException {
        System.out.println("📧 Génération des templates d'emails...");
        
        try (Connection conn = DB.getConnection()) {
            String sql = "INSERT OR REPLACE INTO email_templates (nom_template, type_template, objet, contenu_html, contenu_text, variables_disponibles, actif) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            // Template intervention planifiée
            stmt.setString(1, "intervention_planifiee");
            stmt.setString(2, "INTERVENTION_PLANIFIEE");
            stmt.setString(3, "Intervention planifiée - {{intervention.numero}} - {{date.planifiee}}");
            stmt.setString(4, "<h2>Intervention Planifiée</h2><p>Bonjour {{client.nom}},</p><p>Votre intervention est planifiée pour le {{date.planifiee}} à {{heure.planifiee}}.</p>");
            stmt.setString(5, "Bonjour {{client.nom}}, votre intervention est planifiée pour le {{date.planifiee}} à {{heure.planifiee}}.");
            stmt.setString(6, "[\"client.nom\", \"intervention.numero\", \"date.planifiee\", \"heure.planifiee\"]");
            stmt.setBoolean(7, true);
            stmt.executeUpdate();
            
            // Template livraison prévue
            stmt.setString(1, "livraison_prevue");
            stmt.setString(2, "LIVRAISON_PREVUE");
            stmt.setString(3, "Livraison prévue - Commande {{commande.numero}}");
            stmt.setString(4, "<h2>Livraison Prévue</h2><p>Votre commande {{commande.numero}} sera livrée le {{date.livraison}}.</p>");
            stmt.setString(5, "Votre commande {{commande.numero}} sera livrée le {{date.livraison}}.");
            stmt.setString(6, "[\"commande.numero\", \"date.livraison\"]");
            stmt.setBoolean(7, true);
            stmt.executeUpdate();
            
        }
        System.out.println("✅ Templates d'emails générés");
    }
    
    // Méthodes utilitaires pour générer des données réalistes
    
    private static String generateCompanyName() {
        String[] prefixes = {"Techno", "Audio", "Pro", "Sound", "Music", "Event", "Stage"};
        String[] suffixes = {"Systems", "Solutions", "Services", "Productions", "Technologies", "Equipment"};
        return prefixes[random.nextInt(prefixes.length)] + " " + suffixes[random.nextInt(suffixes.length)];
    }
    
    private static String generatePhoneNumber() {
        return String.format("0%d %02d %02d %02d %02d", 
            1 + random.nextInt(6), 
            random.nextInt(100), 
            random.nextInt(100), 
            random.nextInt(100), 
            random.nextInt(100));
    }
    
    private static String generateAddress() {
        int numero = 1 + random.nextInt(200);
        String[] rues = {"rue de la Paix", "avenue des Champs", "boulevard Saint-Michel", "place de la République", "allée des Arts"};
        String[] villes = {"Paris", "Lyon", "Marseille", "Toulouse", "Nice", "Nantes", "Strasbourg", "Montpellier"};
        int codePostal = 10000 + random.nextInt(90000);
        
        return numero + " " + rues[random.nextInt(rues.length)] + ", " + codePostal + " " + villes[random.nextInt(villes.length)];
    }
    
    private static String generateImmatriculation() {
        char[] letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        return String.format("%c%c-%03d-%c%c", 
            letters[random.nextInt(letters.length)],
            letters[random.nextInt(letters.length)],
            random.nextInt(1000),
            letters[random.nextInt(letters.length)],
            letters[random.nextInt(letters.length)]);
    }
    
    private static String generateModele(String marque) {
        String[] modeles = {"Master", "Transit", "Sprinter", "Ducato", "Boxer", "Daily", "Crafter"};
        return modeles[random.nextInt(modeles.length)];
    }
    
    private static String generateProductName(String fabricant) {
        String[] types = {"Console", "Enceinte", "Micro", "Amplificateur", "Processeur", "Éclairage"};
        String[] modeles = {"Pro", "Studio", "Live", "Digital", "Analog", "Wireless"};
        return types[random.nextInt(types.length)] + " " + modeles[random.nextInt(modeles.length)] + " " + (100 + random.nextInt(900));
    }
    
    private static String generateSerialNumber() {
        return String.format("%04d%04d", random.nextInt(10000), random.nextInt(10000));
    }
    
    private static String generateUID() {
        char[] letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        return String.format("%c%c%c%c%04d", 
            letters[random.nextInt(letters.length)],
            letters[random.nextInt(letters.length)],
            letters[random.nextInt(letters.length)],
            letters[random.nextInt(letters.length)],
            random.nextInt(10000));
    }
    
    private static String generateRandomDate(int startYear, int endYear) {
        int year = startYear + random.nextInt(endYear - startYear + 1);
        int month = 1 + random.nextInt(12);
        int day = 1 + random.nextInt(28); // Simplifié pour éviter les problèmes de dates
        return LocalDate.of(year, month, day).format(DATE_FORMATTER);
    }
    
    private static String generateRandomDateTime(int startYear, int endYear) {
        int year = startYear + random.nextInt(endYear - startYear + 1);
        int month = 1 + random.nextInt(12);
        int day = 1 + random.nextInt(28);
        int hour = random.nextInt(24);
        int minute = random.nextInt(60);
        return LocalDateTime.of(year, month, day, hour, minute).format(DATETIME_FORMATTER);
    }
    
    private static String generatePanneDescription() {
        String[] pannes = {
            "Absence de signal audio", "Distorsion du son", "Coupure intermittente", 
            "Problème d'alimentation", "Connecteur défaillant", "Écran défectueux",
            "Boutons non réactifs", "Surchauffe", "Vibrations anormales"
        };
        return pannes[random.nextInt(pannes.length)];
    }
    
    private static String generateDefautDescription() {
        String[] defauts = {
            "Composant électronique défaillant", "Connectique oxydée", "Carte mère endommagée",
            "Problème logiciel", "Usure mécanique", "Court-circuit interne",
            "Calibrage nécessaire", "Mise à jour firmware", "Nettoyage requis"
        };
        return defauts[random.nextInt(defauts.length)];
    }
    
    /**
     * Génère un nom complet (prénom + nom)
     */
    private static String generateNomComplet() {
        String[] prenoms = {"Jean", "Marie", "Pierre", "Sophie", "Julien", "Camille", "Nicolas", "Laura", "Thomas", "Emma", "Antoine", "Clara", "Maxime", "Julie", "Romain", "Amélie"};
        String prenom = prenoms[random.nextInt(prenoms.length)];
        String nom = NOMS.get(random.nextInt(NOMS.size()));
        return prenom + " " + nom;
    }
    
    /**
     * Génère un email basé sur un nom complet
     */
    private static String generateEmail(String nomComplet) {
        if (nomComplet == null || nomComplet.trim().isEmpty()) {
            return "utilisateur@example.com";
        }
        String clean = nomComplet.toLowerCase()
            .replaceAll(" ", ".")
            .replaceAll("[^a-z0-9.]", "");
        return clean + "@example.com";
    }
    
    /**
     * Génère des demandes (interventions, pièces, matériel, SAV externes, devis, prix)
     */
    private static void generateDemandes(int count) throws SQLException {
        System.out.println("📋 Génération des demandes...");
        
        try (Connection conn = DB.getConnection()) {
            String sql = "INSERT INTO requests (type, title, description, status, priority, requester_name, requester_email, requester_phone, assigned_to, societe_id, intervention_id, estimated_cost, comments, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            for (int i = 1; i <= count; i++) {
                String type = TYPES_DEMANDES.get(random.nextInt(TYPES_DEMANDES.size()));
                String title = generateTitredemande(type, i);
                String description = generateDescriptionDemande(type);
                String status = STATUTS_DEMANDES.get(random.nextInt(STATUTS_DEMANDES.size()));
                String priority = PRIORITES_DEMANDES.get(random.nextInt(PRIORITES_DEMANDES.size()));
                String requesterName = generateNomComplet();
                String requesterEmail = generateEmail(requesterName);
                String requesterPhone = generatePhoneNumber();
                String assignedTo = random.nextBoolean() ? generateNomComplet() : null;
                int societeId = random.nextInt(50) + 1; // Référence vers societes.id
                Integer interventionId = type.equals("INTERVENTION") && random.nextBoolean() ? random.nextInt(30) + 1 : null;
                double estimatedCost = type.equals("DEVIS") || type.equals("PRIX") ? 100 + random.nextDouble() * 5000 : 0;
                String comments = "Demande générée automatiquement - " + type;
                String createdAt = generateRandomDateTime(2024, 2024);
                String updatedAt = generateRandomDateTime(2024, 2024);
                
                stmt.setString(1, type);
                stmt.setString(2, title);
                stmt.setString(3, description);
                stmt.setString(4, status);
                stmt.setString(5, priority);
                stmt.setString(6, requesterName);
                stmt.setString(7, requesterEmail);
                stmt.setString(8, requesterPhone);
                stmt.setString(9, assignedTo);
                stmt.setInt(10, societeId);
                if (interventionId != null) {
                    stmt.setInt(11, interventionId);
                } else {
                    stmt.setNull(11, java.sql.Types.INTEGER);
                }
                stmt.setDouble(12, estimatedCost);
                stmt.setString(13, comments);
                stmt.setString(14, createdAt);
                stmt.setString(15, updatedAt);
                stmt.executeUpdate();
            }
        }
        System.out.println("✅ " + count + " demandes générées");
    }
    
    /**
     * Génère les items/éléments des demandes
     */
    private static void generateItemsDemandes(int count) throws SQLException {
        System.out.println("📦 Génération des items de demandes...");
        
        try (Connection conn = DB.getConnection()) {
            String sql = "INSERT INTO request_items (request_id, item_type, reference, name, description, quantity, unit_price, total_price, supplier_id, status, notes, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            for (int i = 1; i <= count; i++) {
                int requestId = random.nextInt(30) + 1; // Référence vers requests.id
                String itemType = TYPES_ITEMS.get(random.nextInt(TYPES_ITEMS.size()));
                String reference = generateReferenceItem(itemType);
                String name = generateNomItem(itemType);
                String description = "Description détaillée pour " + name;
                int quantity = 1 + random.nextInt(10);
                double unitPrice = 10 + random.nextDouble() * 500;
                double totalPrice = quantity * unitPrice;
                int supplierId = random.nextInt(50) + 1; // Référence vers societes.id
                String status = STATUTS_ITEMS.get(random.nextInt(STATUTS_ITEMS.size()));
                String notes = "Item généré automatiquement - " + itemType;
                String createdAt = generateRandomDateTime(2024, 2024);
                String updatedAt = generateRandomDateTime(2024, 2024);
                
                stmt.setInt(1, requestId);
                stmt.setString(2, itemType);
                stmt.setString(3, reference);
                stmt.setString(4, name);
                stmt.setString(5, description);
                stmt.setInt(6, quantity);
                stmt.setDouble(7, unitPrice);
                stmt.setDouble(8, totalPrice);
                stmt.setInt(9, supplierId);
                stmt.setString(10, status);
                stmt.setString(11, notes);
                stmt.setString(12, createdAt);
                stmt.setString(13, updatedAt);
                stmt.executeUpdate();
            }
        }
        System.out.println("✅ " + count + " items de demandes générés");
    }
    
    // Méthodes utilitaires pour génération de données de demandes
    private static String generateTitredemande(String type, int numero) {
        return switch (type) {
            case "INTERVENTION" -> "Intervention urgente #" + numero;
            case "PIECES" -> "Demande de pièces détachées #" + numero;
            case "MATERIEL" -> "Demande de matériel #" + numero;
            case "SAV_EXTERNE" -> "SAV Externe - Réparation #" + numero;
            case "DEVIS" -> "Demande de devis #" + numero;
            case "PRIX" -> "Demande de prix #" + numero;
            default -> "Demande #" + numero;
        };
    }
    
    private static String generateDescriptionDemande(String type) {
        return switch (type) {
            case "INTERVENTION" -> "Intervention technique requise pour résoudre un problème sur site client.";
            case "PIECES" -> "Besoin de pièces détachées pour réparation ou maintenance préventive.";
            case "MATERIEL" -> "Demande de matériel supplémentaire pour projet ou remplacement.";
            case "SAV_EXTERNE" -> "Réparation à effectuer par un prestataire externe spécialisé.";
            case "DEVIS" -> "Demande d'établissement de devis pour nouveau projet ou prestation.";
            case "PRIX" -> "Demande de prix pour comparaison fournisseurs ou budgétisation.";
            default -> "Description de la demande à compléter.";
        };
    }
    
    private static String generateReferenceItem(String itemType) {
        String prefix = switch (itemType) {
            case "PIECE" -> "PC";
            case "MATERIEL" -> "MAT";
            case "SERVICE" -> "SRV";
            default -> "ITM";
        };
        return prefix + "-" + String.format("%06d", random.nextInt(999999));
    }
    
    private static String generateNomItem(String itemType) {
        String[] pieces = {"Condensateur", "Résistance", "Fusible", "Connecteur", "Câble", "Circuit intégré", "Transistor"};
        String[] materiels = {"Micro HF", "Enceinte", "Projecteur LED", "Console", "Câble XLR", "Multipaire", "Flight-case"};
        String[] services = {"Installation", "Configuration", "Formation", "Maintenance", "Réparation", "Étalonnage", "Support technique"};
        
        return switch (itemType) {
            case "PIECE" -> pieces[random.nextInt(pieces.length)];
            case "MATERIEL" -> materiels[random.nextInt(materiels.length)];
            case "SERVICE" -> services[random.nextInt(services.length)];
            default -> "Item générique";
        };
    }
    
    /**
     * Vide toutes les tables (optionnel - à utiliser avec précaution)
     */
    private static void clearAllTables() throws SQLException {
        System.out.println("🗑️ Suppression des données existantes...");
        
        try (Connection conn = DB.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement("")) {
            
            // Désactiver les contraintes FK temporairement
            stmt.execute("PRAGMA foreign_keys = OFF");
            
            // Lister toutes les tables à vider
            String[] tables = {
                "communications", "disponibilites_techniciens", "alertes_stock", 
                "mouvements_stock", "lignes_commandes", "commandes", "planifications",
                "sav_history", "interventions", "produits", "vehicules", "techniciens",
                "categories", "societes", "email_templates"
            };
            
            for (String table : tables) {
                stmt.execute("DELETE FROM " + table);
                stmt.execute("DELETE FROM sqlite_sequence WHERE name = '" + table + "'");
            }
            
            // Réactiver les contraintes FK
            stmt.execute("PRAGMA foreign_keys = ON");
        }
        System.out.println("✅ Tables vidées");
    }
    
    /**
     * Point d'entrée principal pour génération de données de test
     */
    public static void main(String[] args) {
        System.out.println("🚀 Démarrage du générateur de données de test MAGSAV");
        generateCompleteTestData();
    }
}