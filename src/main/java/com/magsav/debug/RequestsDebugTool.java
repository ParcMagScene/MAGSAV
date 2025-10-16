package com.magsav.debug;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class RequestsDebugTool {
    public static void main(String[] args) {
        try {
            // Connexion directe à H2
            String url = "jdbc:h2:./data/magsav_h2;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1";
            
            try (Connection conn = DriverManager.getConnection(url, "sa", "");
                 Statement stmt = conn.createStatement()) {
                
                System.out.println("=== CONTENU TABLE DEMANDES ===");
                ResultSet rs = stmt.executeQuery("SELECT id, type, statut, fournisseur_nom, date_creation, commentaire FROM demandes ORDER BY id");
                
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.printf("ID: %d | Type: %s | Statut: %s | Fournisseur: %s | Date: %s | Commentaire: %s%n",
                        rs.getInt("id"),
                        rs.getString("type"),
                        rs.getString("statut"),
                        rs.getString("fournisseur_nom"),
                        rs.getString("date_creation"),
                        rs.getString("commentaire")
                    );
                }
                
                System.out.println("Total: " + count + " demandes");
                
                // Vérifier les types distincts
                System.out.println("\n=== TYPES DISTINCTS ===");
                rs = stmt.executeQuery("SELECT DISTINCT type FROM demandes");
                while (rs.next()) {
                    System.out.println("Type: " + rs.getString("type"));
                }
                
                // Vérifier les statuts distincts
                System.out.println("\n=== STATUTS DISTINCTS ===");
                rs = stmt.executeQuery("SELECT DISTINCT statut FROM demandes");
                while (rs.next()) {
                    System.out.println("Statut: " + rs.getString("statut"));
                }
                
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}