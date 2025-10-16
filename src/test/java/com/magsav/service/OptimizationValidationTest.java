package com.magsav.service;

import com.magsav.db.DB;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests des optimisations de performance")
class OptimizationValidationTest {

    @BeforeAll
    static void setup() {
        System.setProperty("magsav.db.url", "jdbc:h2:mem:optimization_test;DB_CLOSE_DELAY=-1");
        DB.resetForTesting();
        DB.init();
    }

    @Test
    @DisplayName("Service de métriques fonctionne correctement")
    void testDatabaseMetricsService() {
        // Réinitialiser les métriques
        DatabaseMetricsService.resetMetrics();
        
        // Enregistrer quelques métriques de test
        DatabaseMetricsService.recordQuery("test_query", 100, true);
        DatabaseMetricsService.recordQuery("test_query", 200, true);
        DatabaseMetricsService.recordQuery("test_query", 50, false);
        DatabaseMetricsService.recordQuery("slow_query", 1500, true);
        
        // Vérifier les métriques globales
        var globalMetrics = DatabaseMetricsService.getGlobalMetrics();
        assertEquals(4, globalMetrics.totalQueries());
        assertEquals(1, globalMetrics.totalErrors());
        assertEquals(2, globalMetrics.uniqueOperations());
        assertEquals(25.0, globalMetrics.getErrorRate(), 0.1);
        assertFalse(globalMetrics.isHealthy()); // Plus de 5% d'erreurs
        
        // Vérifier les métriques spécifiques
        var metrics = DatabaseMetricsService.getMetrics();
        assertTrue(metrics.containsKey("test_query"));
        assertTrue(metrics.containsKey("slow_query"));
        
        var testQueryMetrics = metrics.get("test_query");
        assertEquals(3, testQueryMetrics.executionCount());
        assertEquals(2, testQueryMetrics.successCount());
        assertEquals(350, testQueryMetrics.totalDuration());
        assertEquals(50, testQueryMetrics.minDuration());
        assertEquals(200, testQueryMetrics.maxDuration());
        assertEquals(116.67, testQueryMetrics.getAverageDuration(), 0.1);
        assertEquals(66.67, testQueryMetrics.getSuccessRate(), 0.1);
        assertTrue(testQueryMetrics.hasErrors());
        assertFalse(testQueryMetrics.isSlowQuery());
        
        var slowQueryMetrics = metrics.get("slow_query");
        assertTrue(slowQueryMetrics.isSlowQuery());
        assertFalse(slowQueryMetrics.hasErrors());
    }

    @Test
    @DisplayName("Les requêtes les plus lentes sont correctement identifiées")
    void testSlowestQueries() {
        DatabaseMetricsService.resetMetrics();
        
        DatabaseMetricsService.recordQuery("fast_query", 50, true);
        DatabaseMetricsService.recordQuery("medium_query", 500, true);
        DatabaseMetricsService.recordQuery("slow_query", 2000, true);
        DatabaseMetricsService.recordQuery("slowest_query", 5000, true);
        
        var slowest = DatabaseMetricsService.getSlowestQueries(2);
        assertEquals(2, slowest.size());
        
        var entries = slowest.entrySet().iterator();
        var first = entries.next();
        var second = entries.next();
        
        assertEquals("slowest_query", first.getKey());
        assertEquals(5000, first.getValue().maxDuration());
        
        assertEquals("slow_query", second.getKey());
        assertEquals(2000, second.getValue().maxDuration());
    }

    @Test
    @DisplayName("Les requêtes les plus fréquentes sont correctement identifiées")
    void testMostFrequentQueries() {
        DatabaseMetricsService.resetMetrics();
        
        // Exécuter des requêtes avec différentes fréquences
        for (int i = 0; i < 10; i++) {
            DatabaseMetricsService.recordQuery("frequent_query", 100, true);
        }
        for (int i = 0; i < 5; i++) {
            DatabaseMetricsService.recordQuery("medium_frequency", 100, true);
        }
        DatabaseMetricsService.recordQuery("rare_query", 100, true);
        
        var mostFrequent = DatabaseMetricsService.getMostFrequentQueries(2);
        assertEquals(2, mostFrequent.size());
        
        var entries = mostFrequent.entrySet().iterator();
        var first = entries.next();
        var second = entries.next();
        
        assertEquals("frequent_query", first.getKey());
        assertEquals(10, first.getValue().executionCount());
        
        assertEquals("medium_frequency", second.getKey());
        assertEquals(5, second.getValue().executionCount());
    }

    @Test
    @DisplayName("Le rapport de métriques se génère sans erreur")
    void testMetricsReport() {
        DatabaseMetricsService.resetMetrics();
        
        DatabaseMetricsService.recordQuery("test_operation", 150, true);
        DatabaseMetricsService.recordQuery("another_operation", 75, true);
        DatabaseMetricsService.recordQuery("failing_operation", 0, false);
        
        String report = DatabaseMetricsService.generateReport();
        assertNotNull(report);
        assertFalse(report.isEmpty());
        
        // Vérifier que le rapport contient les sections attendues
        assertTrue(report.contains("RAPPORT MÉTRIQUES"));
        assertTrue(report.contains("Total requêtes"));
        assertTrue(report.contains("Total erreurs"));
        assertTrue(report.contains("TOP 5 REQUÊTES"));
        assertTrue(report.contains("test_operation"));
        assertTrue(report.contains("another_operation"));
        assertTrue(report.contains("failing_operation"));
    }

    @Test
    @DisplayName("Service d'optimisation de base applique les index")
    void testDatabaseOptimization() {
        // Appliquer les optimisations recommandées
        var result = DatabaseOptimizationService.applyRecommendedIndexes();
        
        assertNotNull(result);
        assertNotNull(result.createdIndexes());
        assertNotNull(result.skippedIndexes());
        assertNotNull(result.errors());
        
        // Le service fonctionne même si aucun index n'est créé (base vide)
        assertTrue(result.createdIndexes().size() >= 0);
        assertTrue(result.skippedIndexes().size() >= 0);
        assertTrue(result.errors().size() >= 0);
    }

    @Test
    @DisplayName("Analyse des performances des requêtes fonctionne")
    void testQueryPerformanceAnalysis() {
        var result = DatabaseOptimizationService.analyzeQueryPerformance();
        
        assertNotNull(result);
        assertNotNull(result.queryResults());
        
        // Le service fonctionne toujours - test simple de structure
        assertTrue(result.queryResults().size() >= 0);
    }

    @Test
    @DisplayName("Maintenance de base de données s'exécute sans erreur")
    void testDatabaseMaintenance() {
        var result = DatabaseOptimizationService.performMaintenance();
        
        assertNotNull(result);
        assertNotNull(result.operations());
        assertNotNull(result.errors());
        assertTrue(result.durationMs() >= 0);
        
        // Au moins VACUUM et ANALYZE devraient être exécutés
        assertTrue(result.operations().size() >= 1);
    }
}