package com.magsav.service;

import com.magsav.util.TestDatabaseConfig;
import com.magsav.db.DB;
import com.magsav.exception.InvalidUidException;
import com.magsav.repo.ProductRepository;
import com.magsav.repo.InterventionRepository;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour ProductServiceStatic
 */
public class ProductServiceStaticTest {
    
    static Connection keeper;
    ProductRepository productRepo;
    InterventionRepository interventionRepo;

    @BeforeAll
    static void keepMemoryDb() throws Exception {
        keeper = TestDatabaseConfig.setupSharedInMemoryDb("ProductServiceStaticTest");
    }

    @AfterAll
    static void closeKeeper() throws Exception {
        TestDatabaseConfig.cleanupKeeper(keeper);
    }

    @BeforeEach
    void setUp() {
        productRepo = new ProductRepository();
        interventionRepo = new InterventionRepository();
        
        // Nettoyer les données existantes
        cleanTestData();
        // Données de test
        setupTestData();
    }
    
    void cleanTestData() {
        try (var conn = DB.getConnection(); var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM produits");
            stmt.execute("DELETE FROM interventions");
        } catch (Exception e) {
            // Ignorer les erreurs de nettoyage
        }
    }
    
    void setupTestData() {
        // Créer quelques produits de test
        productRepo.insert("Ordinateur portable", "SN001", "Dell", "ABC1234", "En stock");
        productRepo.insert("Imprimante", "SN002", "HP", "DEF5678", "En service");
        productRepo.insert("Écran", "SN003", "Samsung", "GHI9012", "En réparation");
    }

    @Test
    @DisplayName("Doit récupérer tous les produits")
    void testFindAllProducts() {
        // When
        List<ProductRepository.ProductRow> products = ProductServiceStatic.findAllProducts();
        
        // Then
        assertNotNull(products);
        assertEquals(3, products.size());
        
        // Vérifier que les produits sont bien triés
        assertTrue(products.stream().anyMatch(p -> "Ordinateur portable".equals(p.nom())));
        assertTrue(products.stream().anyMatch(p -> "Imprimante".equals(p.nom())));
    }

    @Test
    @DisplayName("Doit rechercher des produits par nom")
    void testSearchProductsByName() {
        // When
        List<ProductRepository.ProductRow> results = ProductServiceStatic.searchProducts("ordinateur");
        
        // Then
        assertEquals(1, results.size());
        assertEquals("Ordinateur portable", results.get(0).nom());
    }

    @Test
    @DisplayName("Doit rechercher des produits par fabricant")
    void testSearchProductsByManufacturer() {
        // When
        List<ProductRepository.ProductRow> results = ProductServiceStatic.searchProducts("dell");
        
        // Then
        assertEquals(1, results.size());
        assertEquals("Dell", results.get(0).fabricant());
    }

    @Test
    @DisplayName("Doit retourner tous les produits si recherche vide")
    void testSearchProductsWithEmptyQuery() {
        // When
        List<ProductRepository.ProductRow> results = ProductServiceStatic.searchProducts("");
        
        // Then
        assertEquals(3, results.size());
    }

    @Test
    @DisplayName("Doit retourner liste vide si aucun produit trouvé")
    void testSearchProductsNoResults() {
        // When
        List<ProductRepository.ProductRow> results = ProductServiceStatic.searchProducts("inexistant");
        
        // Then
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Doit valider un UID correct")
    void testValidateUidValid() {
        // Should not throw
        assertDoesNotThrow(() -> ProductServiceStatic.validateUidOrThrow("ABC1234"));
        assertDoesNotThrow(() -> ProductServiceStatic.validateUidOrThrow("XYZ9999"));
    }

    @Test
    @DisplayName("Doit rejeter un UID invalide")
    void testValidateUidInvalid() {
        // UID trop court
        assertThrows(InvalidUidException.class, 
            () -> ProductServiceStatic.validateUidOrThrow("AB123"));
        
        // UID avec format incorrect
        assertThrows(InvalidUidException.class, 
            () -> ProductServiceStatic.validateUidOrThrow("1234ABC"));
        
        // UID null
        assertThrows(InvalidUidException.class, 
            () -> ProductServiceStatic.validateUidOrThrow(null));
        
        // UID vide
        assertThrows(InvalidUidException.class, 
            () -> ProductServiceStatic.validateUidOrThrow(""));
    }

    @Test
    @DisplayName("Doit calculer les statistiques des produits")
    void testGetProductStatistics() {
        // When
        ProductServiceStatic.ProductStatistics stats = ProductServiceStatic.getProductStatistics();
        
        // Then
        assertNotNull(stats);
        assertEquals(3, stats.totalProducts());
        // Note: Sans interventions réelles, productsWithInterventions sera 0
        assertEquals(0, stats.productsWithInterventions());
    }

    @Test
    @DisplayName("Doit récupérer un produit par ID")
    void testFindProductById() {
        // Given
        long productId = productRepo.insert("Test Product", "SN004", "Test Manufacturer", "TEST123", "En service");
        
        // When
        var result = ProductServiceStatic.findProductById(productId);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("Test Product", result.get().nom());
        assertEquals("Test Manufacturer", result.get().fabricant());
    }

    @Test
    @DisplayName("Doit retourner empty si produit non trouvé")
    void testFindProductByIdNotFound() {
        // When
        var result = ProductServiceStatic.findProductById(999999L);
        
        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Doit récupérer les interventions d'un produit")
    void testGetProductInterventions() {
        // Given
        long productId = productRepo.insert("Product with interventions", "SN005", "Manufacturer", "TEST456", "En stock");
        
        // When
        var interventions = ProductServiceStatic.getProductInterventions(productId);
        
        // Then
        assertNotNull(interventions);
        // Sans interventions créées, la liste sera vide
        assertTrue(interventions.isEmpty());
    }
}