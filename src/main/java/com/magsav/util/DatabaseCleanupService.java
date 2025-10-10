package com.magsav.util;

import com.magsav.db.DB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour nettoyer les doublons dans la base de données
 */
public class DatabaseCleanupService {
    
    /**
     * Nettoie tous les doublons de la base de données
     */
    public static CleanupResult cleanupAllDuplicates() {
        CleanupResult result = new CleanupResult();
        
        try {
            result.add("Produits UID", cleanupProductUIDs());
            result.add("Companies", cleanupCompanies());
            result.add("Catégories", cleanupCategories());
            
            AppLogger.info("Nettoyage des doublons terminé. Résultat: " + result);
            
        } catch (Exception e) {
            AppLogger.error("Erreur lors du nettoyage des doublons", e);
            result.addError("Erreur générale: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Nettoie les doublons d'UID dans les produits
     */
    private static int cleanupProductUIDs() throws SQLException {
        String findDuplicates = """
            SELECT uid, GROUP_CONCAT(id) as ids, COUNT(*) as count 
            FROM produits 
            WHERE uid IS NOT NULL AND uid != '' 
            GROUP BY uid 
            HAVING COUNT(*) > 1
        """;
        
        int cleaned = 0;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement findStmt = conn.prepareStatement(findDuplicates)) {
            
            ResultSet rs = findStmt.executeQuery();
            
            while (rs.next()) {
                String uid = rs.getString("uid");
                String[] ids = rs.getString("ids").split(",");
                
                // Garder le premier, supprimer les autres
                for (int i = 1; i < ids.length; i++) {
                    try (PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM produits WHERE id = ?")) {
                        deleteStmt.setLong(1, Long.parseLong(ids[i].trim()));
                        deleteStmt.executeUpdate();
                        cleaned++;
                        AppLogger.info("Supprimé produit UID dupliqué: " + uid + ", ID: " + ids[i]);
                    }
                }
            }
        }
        
        return cleaned;
    }
    
    /**
     * Nettoie les doublons dans les companies
     */
    private static int cleanupCompanies() throws SQLException {
        String findDuplicates = """
            SELECT name, GROUP_CONCAT(id) as ids, COUNT(*) as count 
            FROM companies 
            WHERE name IS NOT NULL 
            GROUP BY LOWER(name) 
            HAVING COUNT(*) > 1
        """;
        
        int cleaned = 0;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement findStmt = conn.prepareStatement(findDuplicates)) {
            
            ResultSet rs = findStmt.executeQuery();
            
            while (rs.next()) {
                String name = rs.getString("name");
                String[] ids = rs.getString("ids").split(",");
                
                // Garder le premier (plus récent), supprimer les autres
                for (int i = 1; i < ids.length; i++) {
                    try (PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM companies WHERE id = ?")) {
                        deleteStmt.setLong(1, Long.parseLong(ids[i].trim()));
                        deleteStmt.executeUpdate();
                        cleaned++;
                        AppLogger.info("Supprimé company dupliquée: " + name + ", ID: " + ids[i]);
                    }
                }
            }
        }
        
        return cleaned;
    }
    
    /**
     * Nettoie les doublons dans les catégories
     */
    private static int cleanupCategories() throws SQLException {
        String findDuplicates = """
            SELECT nom, parent_id, GROUP_CONCAT(id) as ids, COUNT(*) as count 
            FROM categories 
            WHERE nom IS NOT NULL 
            GROUP BY LOWER(nom), parent_id 
            HAVING COUNT(*) > 1
        """;
        
        int cleaned = 0;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement findStmt = conn.prepareStatement(findDuplicates)) {
            
            ResultSet rs = findStmt.executeQuery();
            
            while (rs.next()) {
                String nom = rs.getString("nom");
                String[] ids = rs.getString("ids").split(",");
                
                // Garder le premier, supprimer les autres
                for (int i = 1; i < ids.length; i++) {
                    try (PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM categories WHERE id = ?")) {
                        deleteStmt.setLong(1, Long.parseLong(ids[i].trim()));
                        deleteStmt.executeUpdate();
                        cleaned++;
                        AppLogger.info("Supprimé catégorie dupliquée: " + nom + ", ID: " + ids[i]);
                    }
                }
            }
        }
        
        return cleaned;
    }
    
    /**
     * Résultat du nettoyage
     */
    public static class CleanupResult {
        private final List<String> messages = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();
        private int totalCleaned = 0;
        
        public void add(String type, int count) {
            totalCleaned += count;
            if (count > 0) {
                messages.add(type + ": " + count + " doublons supprimés");
            } else {
                messages.add(type + ": aucun doublon trouvé");
            }
        }
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public int getTotalCleaned() { return totalCleaned; }
        public List<String> getMessages() { return messages; }
        public List<String> getErrors() { return errors; }
        public boolean hasErrors() { return !errors.isEmpty(); }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Total nettoyé: ").append(totalCleaned).append("\n");
            for (String msg : messages) {
                sb.append("- ").append(msg).append("\n");
            }
            if (hasErrors()) {
                sb.append("Erreurs:\n");
                for (String error : errors) {
                    sb.append("! ").append(error).append("\n");
                }
            }
            return sb.toString();
        }
    }
}