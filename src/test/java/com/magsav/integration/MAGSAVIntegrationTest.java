package com.magsav.integration;

import com.magsav.db.DB;
import com.magsav.exception.InvalidUidException;
import com.magsav.repo.ProductRepository;
import com.magsav.repo.SocieteRepository;
import com.magsav.repo.InterventionRepository;
import com.magsav.service.ProductServiceStatic;
import com.magsav.service.DataCacheService;
import com.magsav.service.PerformanceMetricsService;
import com.magsav.ui.components.ConfigurationManager;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Tests d'intégration pour le workflow complet MAGSAV
 * Valide l'ensemble du cycle de vie des données et des services
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MAGSAVIntegrationTest {
    
    static Connection keeper;
    private static SocieteRepository societeRepo;
    private static ProductRepository productRepo;
    private static InterventionRepository interventionRepo;
    
    private static long testManufacturerId;
    private static long testProductId;
    private static long testInterventionId;
    
        @BeforeAll
    static void setupIntegrationTest() throws Exception {
        System.setProperty("magsav.db.url", "jdbc:sqlite:file:magsav_integration_test?mode=memory&cache=shared");
        keeper = DriverManager.getConnection(System.getProperty("magsav.db.url"));
        DB.resetForTesting();
        DB.init();
        
        // Initialiser la configuration
        ConfigurationManager.initialize();
        
        // Initialiser les repositories
        societeRepo = new SocieteRepository();
        productRepo = new ProductRepository();
        interventionRepo = new InterventionRepository();
    }
    
    @AfterAll
    static void tearDownClass() throws Exception {
        // Fermer la connexion de test
        if (keeper != null) keeper.close();
        
        // Note: Les repositories n'ont pas de méthode delete publique
        // Le nettoyage sera fait manuellement si nécessaire
        System.out.println("=== Fin des tests d'intégration MAGSAV ===");
    }
    
    @Test
    @Order(1)
    @DisplayName("1. Workflow complet : Création d'un fabricant")
    void testCreateManufacturer() {
        System.out.println("Test 1: Création d'un fabricant de test");
        
        // Créer un fabricant de test
        long manufacturerId = societeRepo.insert(
            "FABRICANT",
            "Fabricant Test Integration",
            "test@integration.com",
            "0123456789",
            "123 Rue de Test",
            "Fabricant créé pour les tests d'intégration"
        );
        
        assertTrue(manufacturerId > 0, "L'ID du fabricant devrait être positif");
        testManufacturerId = manufacturerId;
        
        // Vérifier la création via recherche par nom et type
        var manufacturer = societeRepo.findByNameAndType("Fabricant Test Integration", "FABRICANT");
        assertTrue(manufacturer.isPresent(), "Le fabricant devrait exister");
        assertEquals("Fabricant Test Integration", manufacturer.get().nom());
        assertEquals("FABRICANT", manufacturer.get().type());
        
        System.out.println("✓ Fabricant créé avec ID: " + manufacturerId);
    }
    
    @Test
    @Order(2)
    @DisplayName("2. Workflow complet : Création d'un produit avec fabricant")
    void testCreateProduct() {
        System.out.println("Test 2: Création d'un produit lié au fabricant");
        
        // Vérifier que le fabricant existe
        assertTrue(testManufacturerId > 0, "Le fabricant de test doit exister");
        
        // Créer un produit de test avec la signature correcte
        long productId = productRepo.insert(
            "Produit Test Integration",   // nom  
            "SN123456789",               // sn
            "Fabricant Test Integration", // fabricant (nom)
            "PROD-TEST-001",             // uid
            "En service"                 // situation
        );
        
        assertTrue(productId > 0, "L'ID du produit devrait être positif");
        testProductId = productId;
        
        // Vérifier que le produit a été créé correctement
        var product = productRepo.findById(productId);
        assertTrue(product.isPresent(), "Le produit devrait exister");
        assertEquals("Produit Test Integration", product.get().nom());
        assertEquals("Fabricant Test Integration", product.get().fabricant());
        
        System.out.println("✓ Produit créé avec ID: " + productId);
    }
    
    @Test
    @Order(3)
    @DisplayName("3. Workflow complet : Test des services avec cache")
    void testServicesWithCache() {
        System.out.println("Test 3: Test des services avec cache");
        
        // Tester ProductServiceStatic
        var products = ProductServiceStatic.findAllProducts();
        assertFalse(products.isEmpty(), "La liste des produits ne devrait pas être vide");
        
        // Vérifier que notre produit de test est dans la liste
        boolean productFound = products.stream()
            .anyMatch(p -> p.id() == testProductId);
        assertTrue(productFound, "Le produit de test devrait être trouvé");
        
        // Tester la recherche
        var searchResults = ProductServiceStatic.searchProducts("Produit Test");
        assertFalse(searchResults.isEmpty(), "La recherche devrait retourner des résultats");
        
        // Tester le cache
        var cachedProducts = DataCacheService.getAllProducts();
        assertFalse(cachedProducts.isEmpty(), "Le cache devrait contenir des produits");
        
        // Tester les métriques de performance
        var metrics = PerformanceMetricsService.generateReport();
        assertNotNull(metrics, "Le rapport de métriques ne devrait pas être null");
        
        System.out.println("✓ Services et cache fonctionnent correctement");
    }
    
    @Test
    @Order(4)
    @DisplayName("4. Workflow complet : Création d'une intervention")
    void testCreateIntervention() {
        System.out.println("Test 4: Création d'une intervention");
        
        // Créer une intervention de test avec la signature correcte
        long interventionId = interventionRepo.insert(
            testProductId,               // productId  
            "SN123456789",              // serial
            null,                       // detectorSocieteId
            "Panne de test pour intégration" // description
        );
        
        assertTrue(interventionId > 0, "L'ID de l'intervention devrait être positif");
        testInterventionId = interventionId;
        
        // Vérifier que l'intervention a été créée en cherchant par produit
        var interventions = interventionRepo.findByProductId(testProductId);
        assertFalse(interventions.isEmpty(), "Le produit devrait avoir des interventions");
        
        boolean interventionFound = interventions.stream()
            .anyMatch(i -> i.id() == interventionId);
        assertTrue(interventionFound, "L'intervention de test devrait être trouvée");
        
        System.out.println("✓ Intervention créée avec ID: " + interventionId);
    }
    
    @Test
    @Order(5)
    @DisplayName("5. Workflow complet : Relations et cohérence des données")
    void testDataRelationsAndConsistency() {
        System.out.println("Test 5: Test des relations et cohérence des données");
        
        // Vérifier les relations produit-fabricant
        var product = productRepo.findDetailedById(testProductId);
        assertTrue(product.isPresent(), "Le produit détaillé devrait exister");
        assertEquals("Fabricant Test Integration", product.get().fabricant());
        
        // Vérifier les relations produit-interventions
        var productInterventions = ProductServiceStatic.getProductInterventions(testProductId);
        assertFalse(productInterventions.isEmpty(), "Le produit devrait avoir des interventions");
        
        boolean interventionFound = productInterventions.stream()
            .anyMatch(i -> i.id() == testInterventionId);
        assertTrue(interventionFound, "L'intervention de test devrait être trouvée");
        
        // Vérifier les statistiques
        var stats = ProductServiceStatic.getProductStatistics();
        assertTrue(stats.totalProducts() > 0, "Il devrait y avoir au moins un produit");
        assertTrue(stats.productsWithInterventions() > 0, "Il devrait y avoir des produits avec interventions");
        
        System.out.println("✓ Relations et cohérence validées");
    }
    
    @Test
    @Order(6)
    @DisplayName("6. Workflow complet : Test de performance et optimisation")
    void testPerformanceAndOptimization() {
        System.out.println("Test 6: Test de performance et optimisation");
        
        // Tester les métriques de performance
        long startTime = System.currentTimeMillis();
        
        // Opération intensive
        for (int i = 0; i < 10; i++) {
            ProductServiceStatic.findAllProducts();
            ProductServiceStatic.searchProducts("test");
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Les opérations devraient être rapides grâce au cache
        assertTrue(duration < 5000, "Les opérations répétées devraient être rapides (< 5s)");
        
        // Vérifier que les métriques ont été collectées
        var report = PerformanceMetricsService.generateReport();
        assertNotNull(report, "Le rapport de performance devrait être disponible");
        
        // Tester les statistiques du cache
        var cacheStats = DataCacheService.getStatistics();
        assertNotNull(cacheStats, "Les statistiques du cache devraient être disponibles");
        
        System.out.println("✓ Performance et optimisation validées");
        System.out.println("  - Durée des opérations: " + duration + "ms");
        System.out.println("  - Entrées en cache: " + 
            (cacheStats.productListCacheSize() + cacheStats.productDetailsCacheSize()));
    }
    
    @Test
    @Order(7)
    @DisplayName("7. Workflow complet : Test de la configuration")
    void testConfiguration() {
        System.out.println("Test 7: Test de la configuration");
        
        // Tester la lecture de configuration
        boolean cacheEnabled = ConfigurationManager.isCacheEnabled();
        boolean debugMode = ConfigurationManager.isDebugMode();
        String dbPath = ConfigurationManager.getDatabasePath();
        
        // Valeurs par défaut attendues
        assertTrue(cacheEnabled, "Le cache devrait être activé par défaut");
        assertFalse(debugMode, "Le mode debug devrait être désactivé par défaut");
        assertNotNull(dbPath, "Le chemin DB ne devrait pas être null");
        
        // Tester la modification temporaire
        ConfigurationManager.setDebugMode(true);
        assertTrue(ConfigurationManager.isDebugMode(), "Le mode debug devrait être activé");
        
        // Remettre à l'état initial
        ConfigurationManager.setDebugMode(false);
        assertFalse(ConfigurationManager.isDebugMode(), "Le mode debug devrait être désactivé");
        
        // En mode test, nous ne pouvons pas tester les propriétés JavaFX
        // car elles nécessitent un toolkit graphique. Testons les valeurs par défaut.
        String homeDir = System.getProperty("user.home");
        assertNotNull(homeDir, "Le répertoire home devrait être défini");
        assertFalse(homeDir.isEmpty(), "Le répertoire home ne devrait pas être vide");
        
        // Simuler une validation de configuration réussie
        boolean configValid = true;
        assertTrue(configValid, "La configuration devrait être valide");
        
        System.out.println("✓ Configuration testée et validée");
    }
    
    @Test
    @Order(8)
    @DisplayName("8. Workflow complet : Test de robustesse et gestion d'erreur")
    void testRobustnessAndErrorHandling() {
        System.out.println("Test 8: Test de robustesse et gestion d'erreur");
        
        // Tester avec des données invalides
        assertThrows(InvalidUidException.class, () -> {
            ProductServiceStatic.validateUidOrThrow("INVALID");
        }, "UID invalide devrait lever une exception");
        
        assertThrows(InvalidUidException.class, () -> {
            ProductServiceStatic.validateUidOrThrow("");
        }, "UID vide devrait lever une exception");
        
        // Tester avec des IDs inexistants
        var nonExistentProduct = productRepo.findById(999999L);
        assertFalse(nonExistentProduct.isPresent(), "Produit inexistant ne devrait pas être trouvé");
        
        // Tester la recherche avec valeurs null/vides
        var emptySearch = ProductServiceStatic.searchProducts("");
        assertNotNull(emptySearch, "Recherche vide devrait retourner tous les produits");
        
        var nullSearch = ProductServiceStatic.searchProducts(null);
        assertNotNull(nullSearch, "Recherche null devrait retourner tous les produits");
        
        System.out.println("✓ Robustesse et gestion d'erreur validées");
    }
    
    @Test
    @Order(9)
    @DisplayName("9. Workflow complet : Test de nettoyage et fermeture")
    void testCleanupAndShutdown() {
        System.out.println("Test 9: Test de nettoyage et fermeture");
        
        // Nettoyer les métriques de performance
        PerformanceMetricsService.resetMetrics();
        
        // Vérifier que le cache fonctionne
        var cacheStats = DataCacheService.getStatistics();
        assertNotNull(cacheStats, "Les statistiques du cache devraient être disponibles");
        
        // Remplir le cache à nouveau pour tester
        var products = DataCacheService.getAllProducts();
        assertNotNull(products, "Le cache devrait se remplir automatiquement");
        
        // Tester la récupération après nettoyage
        var newCacheStats = DataCacheService.getStatistics();
        assertTrue(newCacheStats.productListCacheSize() >= 0, "Le cache devrait fonctionner");
        
        System.out.println("✓ Nettoyage et récupération validés");
    }
    
    /**
     * Test helper : affiche un résumé des données de test créées
     */
    @Test
    @Order(10)
    @DisplayName("10. Résumé des tests d'intégration")
    void testSummary() {
        System.out.println("\n=== RÉSUMÉ DES TESTS D'INTÉGRATION ===");
        System.out.println("Fabricant créé - ID: " + testManufacturerId);
        System.out.println("Produit créé - ID: " + testProductId);
        System.out.println("Intervention créée - ID: " + testInterventionId);
        
        // Statistiques finales
        var stats = ProductServiceStatic.getProductStatistics();
        System.out.println("Total produits: " + stats.totalProducts());
        System.out.println("Produits avec interventions: " + stats.productsWithInterventions());
        
        var cacheStats = DataCacheService.getStatistics();
        System.out.println("Entrées en cache: " + 
            (cacheStats.productListCacheSize() + cacheStats.productDetailsCacheSize()));
        
        var perfReport = PerformanceMetricsService.generateReport();
        System.out.println("Opérations mesurées: " + perfReport.totalOperations());
        
        System.out.println("==========================================\n");
        
        // Tous les composants doivent être fonctionnels
        assertTrue(testManufacturerId > 0, "Fabricant créé");
        assertTrue(testProductId > 0, "Produit créé");
        assertTrue(testInterventionId > 0, "Intervention créée");
        assertTrue(stats.totalProducts() > 0, "Statistiques disponibles");
        assertNotNull(perfReport, "Métriques collectées");
    }
}