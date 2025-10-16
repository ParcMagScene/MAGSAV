package com.magsav.util;

import com.magsav.db.H2DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Outil de débogage pour vérifier le contenu de la table interventions
 */
public class InterventionsDebugger {
    
    public static void debugAllInterventions() {
        System.out.println("=== DIAGNOSTIC INTERVENTIONS ===");
        
        try (Connection conn = H2DB.getConnection()) {
            // Vérifier les interventions
            debugInterventions(conn);
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du diagnostic des interventions: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("==========================");
    }
    
    private static void debugInterventions(Connection conn) throws SQLException {
        System.out.println("🔧 DEBUG - Table INTERVENTIONS:");
        
        String sql = "SELECT id, produit_id, statut_intervention, description_panne, date_debut, date_fin, technicien_id FROM interventions ORDER BY id";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println("  📋 Intervention ID=" + rs.getInt("id") + 
                                 ", Produit=" + rs.getInt("produit_id") + 
                                 ", Statut=" + rs.getString("statut_intervention") + 
                                 ", Panne=" + rs.getString("description_panne") + 
                                 ", Technicien=" + rs.getInt("technicien_id"));
            }
            
            System.out.println("📊 Total interventions dans la DB: " + count);
        }
    }
    
    public static void main(String[] args) {
        debugAllInterventions();
    }
}