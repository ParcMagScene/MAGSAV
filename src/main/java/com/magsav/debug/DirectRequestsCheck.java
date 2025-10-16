package com.magsav.debug;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DirectRequestsCheck {
    public static void main(String[] args) {
        try {
            Class.forName("org.h2.Driver");
            String url = "jdbc:h2:./data/magsav_h2;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1";
            
            try (Connection conn = DriverManager.getConnection(url, "sa", "")) {
                System.out.println("=== ANALYSE DIRECTE TABLE REQUESTS ===");
                
                // Compter par type
                String countSql = "SELECT type, COUNT(*) as count FROM requests GROUP BY type ORDER BY type";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(countSql)) {
                    
                    System.out.println("TYPES ET QUANTITÉS:");
                    while (rs.next()) {
                        System.out.println("  " + rs.getString("type") + ": " + rs.getInt("count"));
                    }
                }
                
                // Vérifier spécifiquement PIECES et MATERIEL
                System.out.println("\nDEMANDES PIECES:");
                String piecesSql = "SELECT id, title, status FROM requests WHERE type = 'PIECES' ORDER BY created_at DESC LIMIT 5";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(piecesSql)) {
                    
                    while (rs.next()) {
                        System.out.println("  ID=" + rs.getLong("id") + 
                                           ", Titre=" + rs.getString("title") + 
                                           ", Statut=" + rs.getString("status"));
                    }
                }
                
                System.out.println("\nDEMANDES INTERVENTIONS:");
                String interventionsSql = "SELECT id, title, status FROM requests WHERE type = 'INTERVENTION' ORDER BY created_at DESC LIMIT 5";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(interventionsSql)) {
                    
                    while (rs.next()) {
                        System.out.println("  ID=" + rs.getLong("id") + 
                                           ", Titre=" + rs.getString("title") + 
                                           ", Statut=" + rs.getString("status"));
                    }
                }
                
                // Vérifier la table demandes_intervention 
                System.out.println("\nTABLE DEMANDES_INTERVENTION:");
                try {
                    String demandesInterventionSql = "SELECT COUNT(*) as count FROM demandes_intervention";
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(demandesInterventionSql)) {
                        
                        if (rs.next()) {
                            System.out.println("  Total demandes_intervention: " + rs.getInt("count"));
                        }
                    }
                } catch (Exception e) {
                    System.out.println("  Table demandes_intervention n'existe pas ou erreur: " + e.getMessage());
                }
                
            }
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
}