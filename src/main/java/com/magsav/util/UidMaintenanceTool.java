package com.magsav.util;

import com.magsav.db.DB;
import com.magsav.service.IdService;
import com.magsav.repo.ProductRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Utilitaire pour s'assurer que tous les produits ont un UID généré
 */
public class UidMaintenanceTool {
    
    public static void main(String[] args) {
        System.out.println("=== Maintenance des UIDs ===");
        generateMissingUids();
        System.out.println("=== Terminé ===");
    }
    
    public static void generateMissingUids() {
        ProductRepository productRepo = new ProductRepository();
        
        try (Connection conn = DB.getConnection()) {
            // Récupérer les produits sans UID
            String selectSql = "SELECT id FROM produits WHERE uid IS NULL OR uid = ''";
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            ResultSet rs = selectStmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                long productId = rs.getLong("id");
                String newUid = IdService.generateUniqueUid(productRepo);
                
                // Mettre à jour le produit avec le nouvel UID
                String updateSql = "UPDATE produits SET uid = ? WHERE id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setString(1, newUid);
                updateStmt.setLong(2, productId);
                updateStmt.executeUpdate();
                updateStmt.close();
                
                System.out.println("UID généré pour produit " + productId + ": " + newUid);
                count++;
            }
            
            System.out.println("Total UIDs générés: " + count);
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la génération des UIDs: " + e.getMessage());
            e.printStackTrace();
        }
    }
}