package com.magsav.util;

import com.magsav.model.ProductSituation;
import com.magsav.repo.ProductRepository;

/**
 * Utilitaire pour tester la validation des situations de produits
 */
public class ProductSituationValidator {
    
    public static void main(String[] args) {
        System.out.println("=== Test de validation des situations de produits ===");
        
        // Test des situations valides
        System.out.println("\n1. Situations valides:");
        String[] validSituations = ProductSituation.getAllLabels();
        for (String situation : validSituations) {
            System.out.println("  - " + situation + ": " + ProductSituation.isValid(situation));
        }
        
        // Test des situations invalides
        System.out.println("\n2. Situations invalides:");
        String[] invalidSituations = {"Situation invalide", "En réparation", "SAV Mag", null, ""};
        for (String situation : invalidSituations) {
            System.out.println("  - \"" + situation + "\": " + ProductSituation.isValid(situation));
        }
        
        // Test d'insertion avec situation valide
        System.out.println("\n3. Test d'insertion avec situation valide:");
        try {
            ProductRepository repo = new ProductRepository();
            long id = repo.insert("Produit Test 1", "SN001", "TestFab", "UID001", "En stock");
            System.out.println("  ✓ Insertion réussie avec ID: " + id);
            
            // Nettoyage
            try (var conn = com.magsav.db.DB.getConnection()) {
                var stmt = conn.prepareStatement("DELETE FROM produits WHERE id = ?");
                stmt.setLong(1, id);
                stmt.executeUpdate();
                System.out.println("  ✓ Produit test supprimé");
            }
        } catch (Exception e) {
            System.out.println("  ✗ Erreur: " + e.getMessage());
        }
        
        // Test d'insertion avec situation invalide
        System.out.println("\n4. Test d'insertion avec situation invalide:");
        try {
            ProductRepository repo = new ProductRepository();
            repo.insert("Produit Test 2", "SN002", "TestFab", "UID002", "Situation invalide");
            System.out.println("  ✗ Insertion réussie (ne devrait pas se produire)");
        } catch (IllegalArgumentException e) {
            System.out.println("  ✓ Validation réussie: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  ✗ Erreur inattendue: " + e.getMessage());
        }
        
        // Test de mise à jour avec situation invalide
        System.out.println("\n5. Test de mise à jour avec situation invalide:");
        try {
            ProductRepository repo = new ProductRepository();
            // D'abord créer un produit valide
            long id = repo.insert("Produit Test 3", "SN003", "TestFab", "UID003", "En stock");
            System.out.println("  ✓ Produit créé avec ID: " + id);
            
            // Essayer de mettre à jour avec une situation invalide
            repo.updateSituation(id, "Situation invalide");
            System.out.println("  ✗ Mise à jour réussie (ne devrait pas se produire)");
            
            // Nettoyage
            try (var conn = com.magsav.db.DB.getConnection()) {
                var stmt = conn.prepareStatement("DELETE FROM produits WHERE id = ?");
                stmt.setLong(1, id);
                stmt.executeUpdate();
            }
        } catch (IllegalArgumentException e) {
            System.out.println("  ✓ Validation réussie: " + e.getMessage());
            // Nettoyage en cas d'erreur après création
            try (var conn = com.magsav.db.DB.getConnection()) {
                var stmt = conn.prepareStatement("DELETE FROM produits WHERE nom = 'Produit Test 3'");
                stmt.executeUpdate();
            } catch (Exception ignored) {}
        } catch (Exception e) {
            System.out.println("  ✗ Erreur inattendue: " + e.getMessage());
        }
        
        System.out.println("\n=== Fin des tests ===");
    }
}