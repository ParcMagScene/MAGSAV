package com.magsav.util;

import com.magsav.db.H2DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Générateur de données de test spécifique pour les affaires
 */
public class AffairesTestDataGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(AffairesTestDataGenerator.class);
    private static final Random random = new Random();
    
    // Noms d'affaires réalistes pour Mag Scène
    private static final String[] NOMS_AFFAIRES = {
        "Sonorisation Concert Summer Festival",
        "Éclairage Spectacle Théâtre National",
        "Location Matériel Mariage Château",
        "Installation Studio Enregistrement",
        "Régie Son Festival Jazz",
        "Éclairage Concert Rock Arena",
        "Sonorisation Conférence International",
        "Location Scène Mobile Festival",
        "Installation Home Cinema Luxe",
        "Régie Lumière Comédie Musicale",
        "Sonorisation Événement Corporate",
        "Éclairage Mariage Domaine Prestige",
        "Location Matériel Concert Privé",
        "Installation Salle Spectacle",
        "Régie Son Tournage Film"
    };
    
    private static final String[] DESCRIPTIONS = {
        "Prestation complète de sonorisation avec matériel haute qualité",
        "Éclairage scénique avec jeux de lumière synchronisés",
        "Location de matériel audiovisuel pour événement privé",
        "Installation technique complète avec formation utilisateur",
        "Régie technique avec équipe spécialisée sur site",
        "Prestation événementielle clé en main",
        "Maintenance et support technique 24h/24",
        "Location matériel avec service de livraison et montage"
    };
    
    private static final String[] COMMERCIAUX = {
        "Jean MARTIN", "Marie DUPONT", "Pierre BERNARD", "Sophie MOREAU", "Luc PETIT"
    };
    
    private static final String[] TECHNICIENS = {
        "Michel SOUND", "David LIGHT", "Patrick TECH", "Sylvain MIX", "François STAGE"
    };
    
    public static void genererDonneesTest() {
        System.out.println("🎬 Génération de données de test pour les affaires...");
        logger.info("🎬 Génération de données de test pour les affaires...");
        
        try (Connection conn = H2DB.getConnection()) {
            // Vérifier si des sociétés existent (nécessaires pour les clients)
            if (!verifierSocietesExistantes(conn)) {
                System.out.println("📊 Création de sociétés clientes pour les affaires...");
                logger.info("📊 Création de sociétés clientes pour les affaires...");
                creerSocietesClientes(conn);
            }
            
            // Créer les affaires
            System.out.println("💼 Création des affaires...");
            creerAffaires(conn);
            
            System.out.println("✅ Génération des données d'affaires terminée avec succès");
            logger.info("✅ Génération des données d'affaires terminée avec succès");
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la génération des données d'affaires: " + e.getMessage());
            logger.error("❌ Erreur lors de la génération des données d'affaires: {}", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur génération données affaires", e);
        }
    }
    
    private static boolean verifierSocietesExistantes(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM societes WHERE type_societe = 'CLIENT'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }
    
    private static void creerSocietesClientes(Connection conn) throws SQLException {
        String[] nomsClients = {
            "Festival de Musique de Cannes", "Théâtre National de Paris", "Château de Versailles Events",
            "Studio Recording Pro", "Arena Concert Hall", "Palais des Congrès Lyon",
            "Domaine Wedding Prestige", "Corporate Events International", "Jazz Club Saint-Germain",
            "Salle Pleyel Production"
        };
        
        String sql = """
            INSERT INTO societes (nom_societe, siret, adresse_societe, ville, code_postal, pays, telephone_societe, email_societe, 
                                type_societe, secteur, date_creation) 
            VALUES (?, ?, ?, ?, ?, 'France', ?, ?, 'CLIENT', 'Événementiel', ?)
            """;
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String nom : nomsClients) {
                ps.setString(1, nom);
                ps.setString(2, genererSiret());
                ps.setString(3, genererAdresse());
                ps.setString(4, genererVille());
                ps.setString(5, genererCodePostal());
                ps.setString(6, genererTelephone());
                ps.setString(7, nom.toLowerCase().replace(" ", "") + "@events.fr");
                ps.setString(8, Date.valueOf(LocalDate.now().minusDays(random.nextInt(365))).toString());
                ps.executeUpdate();
            }
        }
        System.out.println("✅ " + nomsClients.length + " sociétés clientes créées");
        logger.info("✅ {} sociétés clientes créées", nomsClients.length);
    }
    
    private static void creerAffaires(Connection conn) throws SQLException {
        // Récupérer les IDs des sociétés clientes
        List<Integer> clientIds = new ArrayList<>();
        String selectClients = "SELECT id, nom_societe FROM societes WHERE type_societe = 'CLIENT' LIMIT 10";
        try (PreparedStatement ps = conn.prepareStatement(selectClients);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clientIds.add(rs.getInt("id"));
            }
        }
        
        if (clientIds.isEmpty()) {
            System.out.println("⚠️ Aucune société cliente trouvée - impossible de créer des affaires");
            logger.warn("⚠️ Aucune société cliente trouvée - impossible de créer des affaires");
            return;
        }
        
        String sql = """
            INSERT INTO affaires (reference, nom, description, client_id, client_nom, statut, type, priorite,
                                montant_estime, montant_reel, taux_marge, devise_code, date_creation, date_echeance,
                                commercial_responsable, technicien_responsable, notes) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'EUR', ?, ?, ?, ?, ?)
            """;
        
        String[] statuts = {"PROSPECTION", "QUALIFIEE", "EN_COURS", "NEGOCIE", "GAGNEE", "PERDUE", "ANNULEE"};
        String[] types = {"VENTE_MATERIEL", "MAINTENANCE", "FORMATION", "CONSEIL", "PROJET", "SAV"};
        String[] priorites = {"FAIBLE", "NORMALE", "HAUTE", "CRITIQUE"};
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < NOMS_AFFAIRES.length; i++) {
                Integer clientId = clientIds.get(random.nextInt(clientIds.size()));
                String clientNom = obtenirNomClient(conn, clientId);
                
                double montantEstime = 1000 + random.nextDouble() * 49000; // Entre 1K et 50K
                double montantReel = montantEstime * (0.8 + random.nextDouble() * 0.4); // ±20%
                double tauxMarge = 10 + random.nextDouble() * 40; // Entre 10% et 50%
                
                ps.setString(1, genererReference("AFF"));
                ps.setString(2, NOMS_AFFAIRES[i]);
                ps.setString(3, DESCRIPTIONS[random.nextInt(DESCRIPTIONS.length)]);
                ps.setInt(4, clientId);
                ps.setString(5, clientNom);
                ps.setString(6, statuts[random.nextInt(statuts.length)]);
                ps.setString(7, types[random.nextInt(types.length)]);
                ps.setString(8, priorites[random.nextInt(priorites.length)]);
                ps.setDouble(9, montantEstime);
                ps.setDouble(10, montantReel);
                ps.setDouble(11, tauxMarge);
                ps.setDate(12, Date.valueOf(LocalDate.now().minusDays(random.nextInt(180))));
                ps.setDate(13, Date.valueOf(LocalDate.now().plusDays(random.nextInt(90))));
                ps.setString(14, COMMERCIAUX[random.nextInt(COMMERCIAUX.length)]);
                ps.setString(15, TECHNICIENS[random.nextInt(TECHNICIENS.length)]);
                ps.setString(16, "Affaire générée pour tests - " + LocalDate.now());
                
                ps.executeUpdate();
            }
        }
        
        System.out.println("✅ " + NOMS_AFFAIRES.length + " affaires créées avec succès");
        logger.info("✅ {} affaires créées avec succès", NOMS_AFFAIRES.length);
    }
    
    private static String obtenirNomClient(Connection conn, int clientId) throws SQLException {
        String sql = "SELECT nom_societe FROM societes WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nom_societe");
                }
            }
        }
        return "Client #" + clientId;
    }
    
    private static String genererReference(String prefix) {
        return prefix + "-" + LocalDate.now().getYear() + "-" + String.format("%04d", random.nextInt(9999));
    }
    
    private static String genererSiret() {
        return String.format("%014d", Math.abs(random.nextLong() % 100000000000000L));
    }
    
    private static String genererAdresse() {
        String[] rues = {"Avenue des Arts", "Rue de la Musique", "Boulevard du Spectacle", "Place de l'Opéra"};
        return (1 + random.nextInt(199)) + " " + rues[random.nextInt(rues.length)];
    }
    
    private static String genererVille() {
        String[] villes = {"Paris", "Lyon", "Marseille", "Toulouse", "Nice", "Nantes", "Cannes", "Avignon"};
        return villes[random.nextInt(villes.length)];
    }
    
    private static String genererCodePostal() {
        return String.format("%05d", 10000 + random.nextInt(90000));
    }
    
    private static String genererTelephone() {
        return String.format("0%d %02d %02d %02d %02d", 
            1 + random.nextInt(9), random.nextInt(100), random.nextInt(100), 
            random.nextInt(100), random.nextInt(100));
    }
    
    public static void main(String[] args) {
        System.out.println("🎬 Générateur de données de test pour les affaires Mag Scène");
        genererDonneesTest();
    }
}