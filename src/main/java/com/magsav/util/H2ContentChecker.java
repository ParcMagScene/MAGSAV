package com.magsav.util;

import com.magsav.db.H2DB;
import java.sql.*;

/**
 * Vérification du contenu de la base H2
 */
public class H2ContentChecker {
    
    public static void main(String[] args) {
        System.out.println("🔍 Vérification du contenu H2...");
        
        try {
            H2DB.init();
            
            try (Connection conn = H2DB.getConnection()) {
                checkTable(conn, "produits");
                checkTable(conn, "societes");
                checkTable(conn, "categories");
                checkTable(conn, "requests");
                checkTable(conn, "affaires");
                checkTable(conn, "interventions");
                checkTable(conn, "users");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        } finally {
            H2DB.shutdown();
        }
    }
    
    private static void checkTable(Connection conn, String tableName) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM " + tableName);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("📊 " + tableName + ": " + count + " enregistrements");
            }
            
        } catch (SQLException e) {
            System.out.println("⚠️ " + tableName + ": Erreur - " + e.getMessage());
        }
    }
}