package com.magsav.debug;

import com.magsav.db.DB;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class SimpleRequestsCheck {
    public static void main(String[] args) {
        System.out.println("=== DEBUT ANALYSE REQUESTS ===");
        try (Connection conn = DB.getConnection()) {
            System.out.println("=== ANALYSE TABLE REQUESTS ===");
            
            // Compter par type
            String countSql = "SELECT type, COUNT(*) as count FROM requests GROUP BY type ORDER BY type";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(countSql)) {
                
                System.out.println("TYPES ET QUANTITÉS:");
                while (rs.next()) {
                    System.out.println("  " + rs.getString("type") + ": " + rs.getInt("count"));
                }
            }
            
            // Montrer quelques exemples
            System.out.println("\nEXEMPLES RÉCENTS:");
            String exampleSql = "SELECT id, type, title, status FROM requests ORDER BY created_at DESC LIMIT 10";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(exampleSql)) {
                
                while (rs.next()) {
                    System.out.println("  ID=" + rs.getLong("id") + 
                                       ", Type=" + rs.getString("type") + 
                                       ", Titre=" + rs.getString("title") + 
                                       ", Statut=" + rs.getString("status"));
                }
            }
            
            // Vérifier les PIECES et MATERIEL spécifiquement
            System.out.println("\nDEMANDES PIECES/MATERIEL:");
            String piecesMaterielSql = "SELECT id, type, title, status FROM requests WHERE type IN ('PIECES', 'MATERIEL') ORDER BY created_at DESC";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(piecesMaterielSql)) {
                
                while (rs.next()) {
                    System.out.println("  ID=" + rs.getLong("id") + 
                                       ", Type=" + rs.getString("type") + 
                                       ", Titre=" + rs.getString("title") + 
                                       ", Statut=" + rs.getString("status"));
                }
            }
            
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
}