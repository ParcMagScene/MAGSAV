package com.magsav.repo;

import com.magsav.util.TestDatabaseConfig;
import com.magsav.model.ProductSituation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;

/**
 * Tests d'intégration pour la validation des situations dans ProductRepository
 */
class ProductRepositoryValidationTest {
    
    private static Connection keeper;
    private ProductRepository repository;
    
    @BeforeAll
    static void setUpClass() throws Exception {
        keeper = TestDatabaseConfig.setupSharedInMemoryDb("ProductRepositoryValidationTest");
    }
    
    @AfterAll
    static void tearDownClass() throws Exception {
        TestDatabaseConfig.cleanupKeeper(keeper);
    }
    
    @BeforeEach
    void setUp() {
        repository = new ProductRepository();
    }
    
    @AfterEach
    void tearDown() {
        // Nettoyer les produits de test
        try (var conn = com.magsav.db.DB.getConnection()) {
            var stmt = conn.prepareStatement("DELETE FROM produits WHERE nom LIKE 'Test Validation%'");
            stmt.executeUpdate();
        } catch (Exception e) {
            // Ignorer les erreurs de nettoyage
        }
    }
    

    
    @Test
    @DisplayName("Doit accepter toutes les situations valides lors de l'insertion")
    void testInsertWithValidSituations() {
        String[] validSituations = ProductSituation.getAllLabels();
        
        for (int index = 0; index < validSituations.length; index++) {
            final int i = index; // Variable finale pour lambda
            String situation = validSituations[i];
            String nom = "Test Validation " + i;
            String sn = "VAL" + String.format("%03d", i);
            
            assertDoesNotThrow(() -> {
                long id = repository.insert(nom, sn, "TestFab", "VAL-UID-" + i, situation);
                assertTrue(id > 0, "L'ID retourné devrait être positif");
                
                // Vérifier que le produit a été inséré avec la bonne situation
                var product = repository.findById(id);
                assertTrue(product.isPresent(), "Le produit devrait être trouvé");
                assertEquals(situation, product.get().situation(), "La situation devrait correspondre");
            }, "L'insertion avec la situation '" + situation + "' ne devrait pas lever d'exception");
        }
    }
    
    @Test
    @DisplayName("Doit rejeter les situations invalides lors de l'insertion")
    void testInsertWithInvalidSituations() {
        String[] invalidSituations = {"SAV Mag", "Perdu", "Stock magasin"};
        
        for (int index = 0; index < invalidSituations.length; index++) {
            final int i = index; // Variable finale pour lambda
            String situation = invalidSituations[i];
            String nom = "Test Validation Invalid " + i;
            String sn = "VALINV" + String.format("%03d", i);
            
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                repository.insert(nom, sn, "TestFab", "VALINV-UID-" + i, situation);
            }, "L'insertion avec la situation '" + situation + "' devrait lever une IllegalArgumentException");
            
            assertTrue(exception.getMessage().contains("Situation non valide"),
                "Le message d'erreur devrait mentionner que la situation n'est pas valide");
            assertTrue(exception.getMessage().contains(situation),
                "Le message d'erreur devrait contenir la situation invalide");
        }
    }
    
    @Test
    @DisplayName("Doit accepter null comme situation (défaut à 'En stock')")
    void testInsertWithNullSituation() {
        assertDoesNotThrow(() -> {
            long id = repository.insert("Test Validation Null", "VALNULL", "TestFab", "VALNULL-UID", null);
            assertTrue(id > 0, "L'ID retourné devrait être positif");
            
            // Vérifier que la situation par défaut a été appliquée
            var product = repository.findById(id);
            assertTrue(product.isPresent(), "Le produit devrait être trouvé");
            assertEquals("En stock", product.get().situation(), "La situation devrait être 'En stock' par défaut");
        }, "L'insertion avec situation null ne devrait pas lever d'exception");
    }
    
    @Test
    @DisplayName("Doit accepter les situations valides lors de la mise à jour")
    void testUpdateWithValidSituations() {
        // Créer un produit de test
        long id = repository.insert("Test Validation Update", "VALUPD", "TestFab", "VALUPD-UID", "En stock");
        
        String[] validSituations = ProductSituation.getAllLabels();
        
        for (String situation : validSituations) {
            assertDoesNotThrow(() -> {
                repository.updateSituation(id, situation);
                
                // Vérifier que la mise à jour a fonctionné
                var product = repository.findById(id);
                assertTrue(product.isPresent(), "Le produit devrait être trouvé");
                assertEquals(situation, product.get().situation(), "La situation devrait être mise à jour");
            }, "La mise à jour avec la situation '" + situation + "' ne devrait pas lever d'exception");
        }
    }
    
    @Test
    @DisplayName("Doit rejeter les situations invalides lors de la mise à jour")
    void testUpdateWithInvalidSituations() {
        // Créer un produit de test
        long id = repository.insert("Test Validation Update Invalid", "VALUPDNV", "TestFab", "VALUPDNV-UID", "En stock");
        
        String[] invalidSituations = {"SAV Mag", "Perdu", "Stock magasin"};
        
        for (String situation : invalidSituations) {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                repository.updateSituation(id, situation);
            }, "La mise à jour avec la situation '" + situation + "' devrait lever une IllegalArgumentException");
            
            assertTrue(exception.getMessage().contains("Situation non valide"),
                "Le message d'erreur devrait mentionner que la situation n'est pas valide");
            assertTrue(exception.getMessage().contains(situation),
                "Le message d'erreur devrait contenir la situation invalide");
            
            // Vérifier que la situation n'a pas été modifiée
            var product = repository.findById(id);
            assertTrue(product.isPresent(), "Le produit devrait être trouvé");
            assertEquals("En stock", product.get().situation(), "La situation ne devrait pas avoir changé");
        }
    }
}