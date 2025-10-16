package com.magsav.util;

import com.magsav.db.DB;
import java.sql.*;
import java.util.Random;
import java.util.Arrays;
import java.util.List;

/**
 * Générateur spécialisé UNIQUEMENT pour les demandes/requests
 * Évite les conflits de database lock en étant complètement isolé
 */
public class RequestsOnlyGenerator {
    
    private static final Random random = new Random();
    
    private static final List<String> TYPES_DEMANDES = Arrays.asList(
        "PIECE", "MATERIEL", "INTERVENTION", "DEVIS", "PRIX"
    );
    
    private static final List<String> STATUTS_DEMANDES = Arrays.asList(
        "EN_ATTENTE", "VALIDEE", "REFUSEE", "EN_COURS", "TERMINEE"
    );
    
    private static final List<String> PRIORITES_DEMANDES = Arrays.asList(
        "BASSE", "NORMALE", "HAUTE", "URGENTE"
    );
    
    private static final List<String> PRENOMS = Arrays.asList(
        "Antoine", "Marie", "Pierre", "Sophie", "Lucas", "Emma", "Thomas", "Chloe",
        "Nicolas", "Julie", "Alexandre", "Sarah", "Maxime", "Laura", "Julien", "Camille"
    );
    
    private static final List<String> NOMS = Arrays.asList(
        "Martin", "Bernard", "Dubois", "Thomas", "Robert", "Richard", "Petit", "Durand",
        "Leroy", "Moreau", "Simon", "Laurent", "Lefebvre", "Michel", "Garcia", "David"
    );
    
    /**
     * Génère uniquement les demandes/requests de manière isolée
     */
    public static void generateRequestsOnly(int count) {
        System.out.println("🎯 Génération isolée de " + count + " demandes/requests...");
        
        // Vérifier d'abord s'il y a déjà des requests
        try (Connection conn = DB.getConnection()) {
            PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM requests");
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("✅ Des requests existent déjà (" + rs.getInt(1) + ") - génération ignorée");
                return;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification: " + e.getMessage());
            return;
        }
        
        // Attendre pour éviter les conflits
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        
        try (Connection conn = DB.getConnection()) {
            // Configuration SQLite optimisée pour éviter les locks (AVANT la transaction)
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA busy_timeout = 60000");
                stmt.execute("PRAGMA journal_mode = WAL");
                stmt.execute("PRAGMA synchronous = NORMAL");
            }
            
            // Démarrer la transaction après les pragmas
            conn.setAutoCommit(false);
            
            String sql = "INSERT INTO requests (type, title, description, status, priority, requester_name, requester_email, requester_phone, assigned_to, societe_id, intervention_id, estimated_cost, comments, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement prepStmt = conn.prepareStatement(sql);
            
            for (int i = 1; i <= count; i++) {
                String type = TYPES_DEMANDES.get(random.nextInt(TYPES_DEMANDES.size()));
                String title = generateTitle(type, i);
                String description = generateDescription(type);
                String status = STATUTS_DEMANDES.get(random.nextInt(STATUTS_DEMANDES.size()));
                String priority = PRIORITES_DEMANDES.get(random.nextInt(PRIORITES_DEMANDES.size()));
                String requesterName = generateName();
                String requesterEmail = generateEmail(requesterName);
                String requesterPhone = generatePhone();
                String assignedTo = random.nextBoolean() ? generateName() : null;
                
                // Utiliser des valeurs simples pour éviter les requêtes supplémentaires
                int societeId = 1 + random.nextInt(10); // IDs simples 1-10
                Integer interventionId = type.equals("INTERVENTION") && random.nextBoolean() ? 1 + random.nextInt(5) : null;
                double estimatedCost = (type.equals("DEVIS") || type.equals("PRIX")) ? 100 + random.nextDouble() * 2000 : 0;
                String comments = "Demande générée - " + type;
                String createdAt = "2024-" + String.format("%02d", 1 + random.nextInt(12)) + "-" + String.format("%02d", 1 + random.nextInt(28)) + " 10:00:00";
                String updatedAt = createdAt;
                
                prepStmt.setString(1, type);
                prepStmt.setString(2, title);
                prepStmt.setString(3, description);
                prepStmt.setString(4, status);
                prepStmt.setString(5, priority);
                prepStmt.setString(6, requesterName);
                prepStmt.setString(7, requesterEmail);
                prepStmt.setString(8, requesterPhone);
                prepStmt.setString(9, assignedTo);
                prepStmt.setInt(10, societeId);
                if (interventionId != null) {
                    prepStmt.setInt(11, interventionId);
                } else {
                    prepStmt.setNull(11, Types.INTEGER);
                }
                prepStmt.setDouble(12, estimatedCost);
                prepStmt.setString(13, comments);
                prepStmt.setString(14, createdAt);
                prepStmt.setString(15, updatedAt);
                
                prepStmt.addBatch();
                
                // Exécuter par petits lots pour éviter les locks
                if (i % 5 == 0) {
                    prepStmt.executeBatch();
                    prepStmt.clearBatch();
                    conn.commit();
                    System.out.println("✅ " + i + "/" + count + " requests générées");
                }
            }
            
            // Exécuter le reste
            prepStmt.executeBatch();
            conn.commit();
            
            System.out.println("✅ " + count + " demandes/requests générées avec succès !");
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la génération des requests: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String generateTitle(String type, int num) {
        switch (type) {
            case "PIECE":
                return "Demande pièce #" + num + " - " + (random.nextBoolean() ? "Câble audio" : "Connecteur XLR");
            case "MATERIEL":
                return "Demande matériel #" + num + " - " + (random.nextBoolean() ? "Micro sans fil" : "Enceinte mobile");
            case "INTERVENTION":
                return "Intervention #" + num + " - " + (random.nextBoolean() ? "Maintenance préventive" : "Dépannage urgent");
            case "DEVIS":
                return "Devis #" + num + " - " + (random.nextBoolean() ? "Sonorisation événement" : "Installation fixe");
            case "PRIX":
                return "Demande de prix #" + num + " - " + (random.nextBoolean() ? "Location matériel" : "Prestations techniques");
            default:
                return "Demande #" + num;
        }
    }
    
    private static String generateDescription(String type) {
        switch (type) {
            case "PIECE":
                return "Besoin d'une pièce détachée pour réparation d'équipement audio professionnel.";
            case "MATERIEL":
                return "Location ou achat de matériel audio/vidéo pour événement ou installation.";
            case "INTERVENTION":
                return "Intervention technique requise sur site pour maintenance ou dépannage.";
            case "DEVIS":
                return "Demande de devis pour prestation audiovisuelle complète.";
            case "PRIX":
                return "Demande de tarification pour services ou location d'équipements.";
            default:
                return "Demande générée automatiquement pour test.";
        }
    }
    
    private static String generateName() {
        String prenom = PRENOMS.get(random.nextInt(PRENOMS.size()));
        String nom = NOMS.get(random.nextInt(NOMS.size()));
        return prenom + " " + nom;
    }
    
    private static String generateEmail(String name) {
        String[] parts = name.toLowerCase().split(" ");
        String domain = random.nextBoolean() ? "gmail.com" : "company.fr";
        return parts[0] + "." + parts[1] + "@" + domain;
    }
    
    private static String generatePhone() {
        return "0" + (1 + random.nextInt(9)) + " " + 
               String.format("%02d", random.nextInt(100)) + " " + 
               String.format("%02d", random.nextInt(100)) + " " + 
               String.format("%02d", random.nextInt(100)) + " " + 
               String.format("%02d", random.nextInt(100));
    }
    
    /**
     * Point d'entrée principal pour exécution standalone
     */
    public static void main(String[] args) {
        try {
            System.out.println("🚀 Générateur de requests isolé - MAGSAV");
            generateRequestsOnly(30);
        } catch (Exception e) {
            System.err.println("❌ Erreur fatale: " + e.getMessage());
            e.printStackTrace();
        }
    }
}