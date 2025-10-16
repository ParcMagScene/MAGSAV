package com.magsav.utils;

import com.magsav.db.H2DB;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Utilitaire pour importer les données CSV dans H2
 */
public class CSVImporter {
    
    public static void main(String[] args) {
        try {
            System.out.println("🔄 Démarrage de l'import CSV vers H2...");
            
            // Initialiser H2
            H2DB.init();
            
            // Obtenir une connexion
            Connection conn = H2DB.getConnection();
            conn.setAutoCommit(false);
            
            Statement stmt = conn.createStatement();
            
            try {
                // Vider les tables existantes d'abord (en respectant les contraintes FK)
                System.out.println("🗑️ Suppression des données existantes...");
                stmt.executeUpdate("DELETE FROM requests");
                stmt.executeUpdate("DELETE FROM affaires");
                stmt.executeUpdate("DELETE FROM users");
                
                // Import des utilisateurs
                System.out.println("👥 Import des utilisateurs...");
                String usersCsvPath = "/Users/reunion/MAGSAV/users.csv";
                stmt.executeUpdate("INSERT INTO users SELECT * FROM CSVREAD('" + usersCsvPath + "')");
                
                // Import des affaires
                System.out.println("💼 Import des affaires...");
                String affairesCsvPath = "/Users/reunion/MAGSAV/affaires.csv";
                stmt.executeUpdate("INSERT INTO affaires SELECT * FROM CSVREAD('" + affairesCsvPath + "')");
                
                // Import des demandes
                System.out.println("📋 Import des demandes...");
                String requestsCsvPath = "/Users/reunion/MAGSAV/requests.csv";
                stmt.executeUpdate("INSERT INTO requests SELECT * FROM CSVREAD('" + requestsCsvPath + "')");
                
                // Commit des changements
                conn.commit();
                
                // Vérification des imports
                System.out.println("✅ Vérification des données importées :");
                
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
                if (rs.next()) {
                    System.out.println("   👥 Users: " + rs.getInt(1));
                }
                
                rs = stmt.executeQuery("SELECT COUNT(*) FROM affaires");
                if (rs.next()) {
                    System.out.println("   💼 Affaires: " + rs.getInt(1));
                }
                
                rs = stmt.executeQuery("SELECT COUNT(*) FROM requests");
                if (rs.next()) {
                    System.out.println("   📋 Requests: " + rs.getInt(1));
                }
                
                System.out.println("🎉 Import CSV terminé avec succès !");
                
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("❌ Erreur lors de l'import : " + e.getMessage());
                e.printStackTrace();
            } finally {
                stmt.close();
                conn.close();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur fatale : " + e.getMessage());
            e.printStackTrace();
        }
        
        // Forcer la sortie pour éviter les conflits JavaFX
        System.exit(0);
    }
}