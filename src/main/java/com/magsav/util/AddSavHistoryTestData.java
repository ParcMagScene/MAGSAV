package com.magsav.util;

import com.magsav.db.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Script pour ajouter des données de test d'historique SAV
 */
public class AddSavHistoryTestData {
    public static void main(String[] args) {
        try (Connection conn = DB.getConnection()) {
            // Vérifier s'il y a déjà des données d'historique
            String checkSql = "SELECT COUNT(*) FROM sav_history";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            
            if (rs.getInt(1) > 0) {
                System.out.println("ℹ️  Des données d'historique SAV existent déjà");
                return;
            }
            
            // Récupérer quelques SAV externes
            String getSavSql = "SELECT id, nom FROM societes WHERE type = 'SAV_EXTERNE' LIMIT 2";
            PreparedStatement getSavStmt = conn.prepareStatement(getSavSql);
            ResultSet savRs = getSavStmt.executeQuery();
            
            if (!savRs.next()) {
                System.out.println("⚠️  Aucun SAV externe trouvé. Créons-en un d'abord.");
                
                // Créer un SAV externe de test
                String insertSavSql = "INSERT INTO societes (type, nom, email, phone, adresse, notes) VALUES ('SAV_EXTERNE', 'TechRepair Pro', 'contact@techrepair.com', '01.23.45.67.89', '123 Rue de la Réparation, 75001 Paris', 'SAV spécialisé en matériel audio professionnel')";
                PreparedStatement insertSavStmt = conn.prepareStatement(insertSavSql);
                insertSavStmt.executeUpdate();
                
                // Re-récupérer le SAV créé
                savRs = getSavStmt.executeQuery();
                savRs.next();
            }
            
            long savId = savRs.getLong("id");
            String savNom = savRs.getString("nom");
            
            // Récupérer quelques produits pour créer l'historique
            String getProdSql = "SELECT id, nom FROM produits LIMIT 5";
            PreparedStatement getProdStmt = conn.prepareStatement(getProdSql);
            ResultSet prodRs = getProdStmt.executeQuery();
            
            String[][] testData = {
                // date_debut, date_fin, statut, notes
                {"2024-08-15", "2024-09-02", "Réparé", "Problème alimentation - Condensateur remplacé"},
                {"2024-09-10", "2024-09-25", "Remplacé", "Capteur défectueux - Remplacé sous garantie"},
                {"2024-07-20", "2024-08-15", "Réparé", "Connecteur audio défaillant"},
                {"2024-10-01", null, "En cours", "Diagnostic en cours - Problème intermittent"}
            };
            
            int count = 0;
            while (prodRs.next() && count < testData.length) {
                long productId = prodRs.getLong("id");
                String productName = prodRs.getString("nom");
                
                String insertHistorySql = "INSERT INTO sav_history (product_id, sav_externe_id, date_debut, date_fin, statut, notes) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement insertHistoryStmt = conn.prepareStatement(insertHistorySql);
                insertHistoryStmt.setLong(1, productId);
                insertHistoryStmt.setLong(2, savId);
                insertHistoryStmt.setString(3, testData[count][0]); // date_debut
                insertHistoryStmt.setString(4, testData[count][1]); // date_fin
                insertHistoryStmt.setString(5, testData[count][2]); // statut
                insertHistoryStmt.setString(6, testData[count][3]); // notes
                
                insertHistoryStmt.executeUpdate();
                
                System.out.println("✅ Ajouté historique SAV pour: " + productName);
                count++;
            }
            
            System.out.println("✅ " + count + " entrées d'historique SAV créées pour " + savNom);
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout des données: " + e.getMessage());
            e.printStackTrace();
        }
    }
}