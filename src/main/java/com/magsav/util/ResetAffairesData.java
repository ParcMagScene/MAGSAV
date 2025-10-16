package com.magsav.util;

import com.magsav.db.H2DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

/**
 * Utilitaire pour nettoyer et régénérer les données d'affaires
 */
public class ResetAffairesData {
    
    private static final Logger logger = LoggerFactory.getLogger(ResetAffairesData.class);
    
    public static void main(String[] args) {
        System.out.println("🧹 Nettoyage et régénération des données d'affaires...");
        
        try (Connection conn = H2DB.getConnection(); Statement stmt = conn.createStatement()) {
            
            // Nettoyer les données existantes
            System.out.println("🗑️ Suppression des affaires existantes...");
            stmt.execute("DELETE FROM affaires");
            
            System.out.println("🗑️ Suppression des sociétés clientes...");
            stmt.execute("DELETE FROM societes WHERE type_societe = 'CLIENT'");
            
            System.out.println("✅ Nettoyage terminé, génération des nouvelles données...");
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du nettoyage: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Régénérer les données
        AffairesTestDataGenerator.genererDonneesTest();
    }
}