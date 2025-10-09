package com.magsav.util;

import com.magsav.model.ProductSituation;
import com.magsav.db.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilitaire pour valider et corriger les situations de produits dans la base de données
 */
public class ProductSituationCleaner {
    
    public static void main(String[] args) {
        System.out.println("=== Validation des situations de produits ===");
        
        try {
            validateAndCleanProductSituations();
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void validateAndCleanProductSituations() throws SQLException {
        try (Connection conn = DB.getConnection()) {
            // 1. Compter les situations actuelles
            System.out.println("\n1. État actuel des situations:");
            Map<String, Integer> currentSituations = getCurrentSituations(conn);
            for (Map.Entry<String, Integer> entry : currentSituations.entrySet()) {
                String situation = entry.getKey();
                int count = entry.getValue();
                boolean isValid = ProductSituation.isValid(situation);
                System.out.printf("  - %-20s: %3d produits %s%n", 
                    "\"" + situation + "\"", count, isValid ? "✓" : "✗");
            }
            
            // 2. Identifier les situations à corriger
            System.out.println("\n2. Correction des situations non conformes:");
            int correctedCount = 0;
            
            // Corriger "SAV Mag" vers "SAV Mag Scene" s'il y en a
            int savMagCount = correctSituation(conn, "SAV Mag", "SAV Mag Scene");
            if (savMagCount > 0) {
                System.out.printf("  - %d produits corrigés: 'SAV Mag' → 'SAV Mag Scene'%n", savMagCount);
                correctedCount += savMagCount;
            }
            
            // Corriger les situations totalement invalides vers "En stock"
            int invalidCount = correctInvalidSituations(conn);
            if (invalidCount > 0) {
                System.out.printf("  - %d produits corrigés: situations invalides → 'En stock'%n", invalidCount);
                correctedCount += invalidCount;
            }
            
            if (correctedCount == 0) {
                System.out.println("  ✓ Aucune correction nécessaire");
            }
            
            // 3. État final
            System.out.println("\n3. État final des situations:");
            Map<String, Integer> finalSituations = getCurrentSituations(conn);
            int totalProducts = 0;
            for (Map.Entry<String, Integer> entry : finalSituations.entrySet()) {
                String situation = entry.getKey();
                int count = entry.getValue();
                totalProducts += count;
                boolean isValid = ProductSituation.isValid(situation);
                System.out.printf("  - %-20s: %3d produits %s%n", 
                    "\"" + situation + "\"", count, isValid ? "✓" : "✗");
            }
            
            System.out.printf("%nTotal: %d produits%n", totalProducts);
            
            // 4. Vérification finale
            boolean allValid = true;
            for (String situation : finalSituations.keySet()) {
                if (!ProductSituation.isValid(situation)) {
                    allValid = false;
                    break;
                }
            }
            
            System.out.println("\n4. Validation finale:");
            if (allValid) {
                System.out.println("  ✓ Toutes les situations sont conformes");
            } else {
                System.out.println("  ✗ Il reste des situations non conformes");
            }
        }
    }
    
    private static Map<String, Integer> getCurrentSituations(Connection conn) throws SQLException {
        Map<String, Integer> situations = new HashMap<>();
        String sql = "SELECT situation, COUNT(*) as count FROM produits GROUP BY situation ORDER BY situation";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String situation = rs.getString("situation");
                if (situation == null) situation = "NULL";
                int count = rs.getInt("count");
                situations.put(situation, count);
            }
        }
        
        return situations;
    }
    
    private static int correctSituation(Connection conn, String oldSituation, String newSituation) throws SQLException {
        String sql = "UPDATE produits SET situation = ? WHERE situation = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newSituation);
            stmt.setString(2, oldSituation);
            return stmt.executeUpdate();
        }
    }
    
    private static int correctInvalidSituations(Connection conn) throws SQLException {
        // Construire la condition WHERE pour exclure les situations valides
        String[] validSituations = ProductSituation.getAllLabels();
        StringBuilder whereClause = new StringBuilder("situation NOT IN (");
        for (int i = 0; i < validSituations.length; i++) {
            if (i > 0) whereClause.append(", ");
            whereClause.append("?");
        }
        whereClause.append(") OR situation IS NULL");
        
        String sql = "UPDATE produits SET situation = 'En stock' WHERE " + whereClause;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < validSituations.length; i++) {
                stmt.setString(i + 1, validSituations[i]);
            }
            return stmt.executeUpdate();
        }
    }
}