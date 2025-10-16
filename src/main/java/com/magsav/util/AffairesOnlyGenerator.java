package com.magsav.util;

import com.magsav.db.DB;
import java.sql.*;
import java.util.Random;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Générateur spécialisé UNIQUEMENT pour les affaires
 * Évite les conflits et génère les données selon le schéma AffairesService
 */
public class AffairesOnlyGenerator {
    
    private static final Random random = new Random();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Génère uniquement les affaires avec le bon schéma
     */
    public static void generateAffairesOnly(int count) {
        System.out.println("💼 Génération isolée de " + count + " affaires...");
        
        // Vérifier d'abord s'il y a déjà des affaires
        try (Connection conn = DB.getConnection()) {
            PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM affaires");
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("✅ Des affaires existent déjà (" + rs.getInt(1) + ") - génération ignorée");
                return;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification: " + e.getMessage());
            return;
        }
        
        try (Connection conn = DB.getConnection()) {
            // Configuration SQLite optimisée
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA busy_timeout = 30000");
                stmt.execute("PRAGMA journal_mode = WAL");
                stmt.execute("PRAGMA synchronous = NORMAL");
            }
            
            conn.setAutoCommit(false);
            
            String sql = """
                INSERT INTO affaires (reference, nom, description, client_id, client_nom, statut, type, priorite,
                                     montant_estime, devise_code, date_creation, date_echeance, 
                                     commercial_responsable, technicien_responsable, chef_projet, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
            PreparedStatement prepStmt = conn.prepareStatement(sql);
            
            String[] statuts = {"PROSPECTION", "QUALIFIEE", "EN_COURS", "NEGOCIE", "GAGNEE", "PERDUE", "ANNULEE"};
            String[] types = {"LOCATION", "VENTE", "SAV", "MAINTENANCE", "INSTALLATION"};
            String[] priorites = {"BASSE", "NORMALE", "HAUTE", "URGENTE"};
            String[] projets = {"Festival d'été", "Spectacle de danse", "Conférence d'entreprise", "Concert acoustique", 
                              "Événement sportif", "Mariage", "Soirée d'entreprise", "Théâtre", "Concert rock", "Exposition"};
            
            String[] prenoms = {"Antoine", "Marie", "Pierre", "Sophie", "Lucas", "Emma", "Thomas", "Chloe",
                               "Nicolas", "Julie", "Alexandre", "Sarah", "Maxime", "Laura", "Julien", "Camille"};
            String[] noms = {"Martin", "Bernard", "Dubois", "Thomas", "Robert", "Richard", "Petit", "Durand",
                            "Leroy", "Moreau", "Simon", "Laurent", "Lefebvre", "Michel", "Garcia", "David"};
            
            for (int i = 1; i <= count; i++) {
                // Générer référence au format AF + 5 chiffres
                String reference = String.format("AF%05d", 10000 + i);
                
                int clientId = 1 + random.nextInt(10); // IDs simples 1-10
                String nomProjet = projets[random.nextInt(projets.length)] + " " + (2024 + random.nextInt(2));
                String description = "Affaire commerciale pour " + nomProjet.toLowerCase() + 
                                  ". Prestation complète audiovisuelle avec équipement et techniciens.";
                
                String commercial = prenoms[random.nextInt(prenoms.length)] + " " + noms[random.nextInt(noms.length)];
                String technicien = prenoms[random.nextInt(prenoms.length)] + " " + noms[random.nextInt(noms.length)];
                String chefProjet = prenoms[random.nextInt(prenoms.length)] + " " + noms[random.nextInt(noms.length)];
                
                LocalDate dateCreation = LocalDate.now().minusDays(random.nextInt(365));
                LocalDate dateEcheance = dateCreation.plusDays(30 + random.nextInt(120));
                
                prepStmt.setString(1, reference);
                prepStmt.setString(2, nomProjet);
                prepStmt.setString(3, description);
                prepStmt.setInt(4, clientId);
                prepStmt.setString(5, "Client Société " + clientId);
                prepStmt.setString(6, statuts[random.nextInt(statuts.length)]);
                prepStmt.setString(7, types[random.nextInt(types.length)]);
                prepStmt.setString(8, priorites[random.nextInt(priorites.length)]);
                prepStmt.setDouble(9, 1500 + random.nextDouble() * 25000); // Montant entre 1500 et 26500€
                prepStmt.setString(10, "EUR");
                prepStmt.setString(11, dateCreation.format(DATE_FORMATTER));
                prepStmt.setString(12, dateEcheance.format(DATE_FORMATTER));
                prepStmt.setString(13, commercial);
                prepStmt.setString(14, technicien);
                prepStmt.setString(15, chefProjet);
                prepStmt.setString(16, "Affaire générée automatiquement - " + reference);
                
                prepStmt.addBatch();
                
                // Exécuter par lots de 5
                if (i % 5 == 0) {
                    prepStmt.executeBatch();
                    prepStmt.clearBatch();
                    conn.commit();
                    System.out.println("✅ " + i + "/" + count + " affaires générées");
                }
            }
            
            // Exécuter le reste
            prepStmt.executeBatch();
            conn.commit();
            
            System.out.println("✅ " + count + " affaires générées avec succès !");
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la génération des affaires: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Point d'entrée principal pour exécution standalone
     */
    public static void main(String[] args) {
        try {
            System.out.println("🚀 Générateur d'affaires isolé - MAGSAV");
            generateAffairesOnly(25);
        } catch (Exception e) {
            System.err.println("❌ Erreur fatale: " + e.getMessage());
            e.printStackTrace();
        }
    }
}