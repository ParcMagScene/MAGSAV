package com.magsav.util;

import com.magsav.db.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * Utilitaire pour synchroniser les catégories entre la table produits et la table categories
 */
public class CategorySyncTool {

    public static void main(String[] args) {
        System.out.println("=== Outil de synchronisation des catégories ===");
        syncCategories();
    }

    public static void syncCategories() {
        try {
            Set<String> categoriesInProducts = getCategoriesFromProducts();
            Set<String> categoriesInTable = getCategoriesFromTable();
            
            System.out.println("Catégories trouvées dans la table 'produits': " + categoriesInProducts);
            System.out.println("Catégories trouvées dans la table 'categories': " + categoriesInTable);
            
            // Trouver les catégories manquantes
            Set<String> missingCategories = new HashSet<>(categoriesInProducts);
            missingCategories.removeAll(categoriesInTable);
            
            if (!missingCategories.isEmpty()) {
                System.out.println("Catégories manquantes dans la table 'categories': " + missingCategories);
                
                // Ajouter les catégories manquantes
                for (String category : missingCategories) {
                    insertCategory(category);
                    System.out.println("✅ Catégorie ajoutée: " + category);
                }
            } else {
                System.out.println("✅ Toutes les catégories sont synchronisées");
            }
            
            // Faire de même pour les sous-catégories
            syncSubcategories();
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la synchronisation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static Set<String> getCategoriesFromProducts() throws SQLException {
        Set<String> categories = new HashSet<>();
        String sql = "SELECT DISTINCT category FROM produits WHERE category IS NOT NULL AND TRIM(category) != ''";
        
        try (Connection c = DB.getConnection(); 
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                String category = rs.getString("category");
                if (category != null && !category.trim().isEmpty()) {
                    categories.add(category.trim());
                }
            }
        }
        return categories;
    }
    
    private static Set<String> getCategoriesFromTable() throws SQLException {
        Set<String> categories = new HashSet<>();
        String sql = "SELECT nom FROM categories WHERE parent_id IS NULL";
        
        try (Connection c = DB.getConnection(); 
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                String nom = rs.getString("nom");
                if (nom != null && !nom.trim().isEmpty()) {
                    categories.add(nom.trim());
                }
            }
        }
        return categories;
    }
    
    private static void insertCategory(String categoryName) throws SQLException {
        String sql = "INSERT INTO categories(nom, parent_id) VALUES(?, NULL)";
        
        try (Connection c = DB.getConnection(); 
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, categoryName);
            ps.executeUpdate();
        }
    }
    
    private static void syncSubcategories() throws SQLException {
        System.out.println("\n=== Synchronisation des sous-catégories ===");
        
        String sql = """
            SELECT DISTINCT category, subcategory 
            FROM produits 
            WHERE category IS NOT NULL AND TRIM(category) != ''
              AND subcategory IS NOT NULL AND TRIM(subcategory) != ''
        """;
        
        try (Connection c = DB.getConnection(); 
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                String category = rs.getString("category");
                String subcategory = rs.getString("subcategory");
                
                if (category != null && subcategory != null && 
                    !category.trim().isEmpty() && !subcategory.trim().isEmpty()) {
                    
                    // Vérifier si la sous-catégorie existe déjà
                    if (!subcategoryExists(c, category.trim(), subcategory.trim())) {
                        insertSubcategory(c, category.trim(), subcategory.trim());
                        System.out.println("✅ Sous-catégorie ajoutée: " + category + " > " + subcategory);
                    }
                }
            }
        }
    }
    
    private static boolean subcategoryExists(Connection c, String parentName, String subcategoryName) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM categories c1 
            JOIN categories c2 ON c1.id = c2.parent_id 
            WHERE c1.nom = ? AND c2.nom = ?
        """;
        
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, parentName);
            ps.setString(2, subcategoryName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
    
    private static void insertSubcategory(Connection c, String parentName, String subcategoryName) throws SQLException {
        // D'abord trouver l'ID du parent
        String findParentSql = "SELECT id FROM categories WHERE nom = ? AND parent_id IS NULL";
        Long parentId = null;
        
        try (PreparedStatement ps = c.prepareStatement(findParentSql)) {
            ps.setString(1, parentName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    parentId = rs.getLong("id");
                }
            }
        }
        
        if (parentId != null) {
            String insertSql = "INSERT INTO categories(nom, parent_id) VALUES(?, ?)";
            try (PreparedStatement ps = c.prepareStatement(insertSql)) {
                ps.setString(1, subcategoryName);
                ps.setLong(2, parentId);
                ps.executeUpdate();
            }
        } else {
            System.out.println("⚠️ Parent introuvable pour la sous-catégorie: " + parentName + " > " + subcategoryName);
        }
    }
}