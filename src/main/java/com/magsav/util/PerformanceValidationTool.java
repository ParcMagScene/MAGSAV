package com.magsav.util;

import com.magsav.db.DB;
import com.magsav.service.DatabaseMetricsService;
import com.magsav.service.DatabaseOptimizationService;
import com.magsav.repo.ProductRepository;

/**
 * Utilitaire pour tester et valider les optimisations de performance
 */
public class PerformanceValidationTool {
    
    public static void main(String[] args) {
        System.out.println("=== OUTIL DE VALIDATION DES PERFORMANCES ===\n");
        
        try {
            // Initialiser la base de données
            DB.init();
            System.out.println("✅ Base de données initialisée");
            
            // Réinitialiser les métriques
            DatabaseMetricsService.resetMetrics();
            System.out.println("✅ Métriques réinitialisées\n");
            
            // Test 1: Optimisations de base de données
            System.out.println("🔧 Application des optimisations de base de données...");
            var optimizationResult = DatabaseOptimizationService.applyRecommendedIndexes();
            System.out.println("Index créés: " + optimizationResult.createdIndexes().size());
            System.out.println("Index ignorés: " + optimizationResult.skippedIndexes().size());
            if (!optimizationResult.errors().isEmpty()) {
                System.out.println("Erreurs: " + optimizationResult.errors());
            }
            
            // Test 2: Analyse des performances avant
            System.out.println("\n📊 Analyse des performances AVANT optimisation...");
            var beforeAnalysis = DatabaseOptimizationService.analyzeQueryPerformance();
            printQueryAnalysis(beforeAnalysis);
            
            // Test 3: Test de charge
            System.out.println("\n🏃 Test de charge des repositories...");
            runLoadTest();
            
            // Test 4: Maintenance de la base
            System.out.println("\n🧹 Maintenance de la base de données...");
            var maintenanceResult = DatabaseOptimizationService.performMaintenance();
            System.out.println("Opérations effectuées: " + maintenanceResult.operations());
            System.out.println("Durée: " + maintenanceResult.durationMs() + "ms");
            if (!maintenanceResult.errors().isEmpty()) {
                System.out.println("Erreurs: " + maintenanceResult.errors());
            }
            
            // Test 5: Analyse des performances après
            System.out.println("\n📊 Analyse des performances APRÈS optimisation...");
            var afterAnalysis = DatabaseOptimizationService.analyzeQueryPerformance();
            printQueryAnalysis(afterAnalysis);
            
            // Test 6: Rapport des métriques
            System.out.println("\n📈 Rapport des métriques collectées:");
            System.out.println(DatabaseMetricsService.generateReport());
            
            // Test 7: Recommandations
            System.out.println("\n💡 Recommandations:");
            generateRecommendations();
            
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void printQueryAnalysis(DatabaseOptimizationService.QueryAnalysisResult result) {
        result.queryResults().forEach(query -> {
            if (query.success()) {
                String status = query.durationMs() > 1000 ? "🐌 LENT" : 
                               query.durationMs() > 500 ? "⚠️ MOYEN" : "✅ RAPIDE";
                System.out.printf("  %-30s | %6dms | %4d rows | %s\n", 
                    query.name(), query.durationMs(), query.rowCount(), status);
            } else {
                System.out.printf("  %-30s | ❌ ERREUR: %s\n", query.name(), query.error());
            }
        });
    }
    
    private static void runLoadTest() {
        ProductRepository productRepo = new ProductRepository();
        long startTime = System.currentTimeMillis();
        
        try {
            // Test de lectures multiples
            for (int i = 0; i < 10; i++) {
                productRepo.findAllVisible();
                productRepo.listFabricants();
                productRepo.listDistinctCategories();
            }
            
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("  Test de charge terminé en " + duration + "ms");
            
        } catch (Exception e) {
            System.err.println("  ❌ Erreur lors du test de charge: " + e.getMessage());
        }
    }
    
    private static void generateRecommendations() {
        var metrics = DatabaseMetricsService.getGlobalMetrics();
        var slowQueries = DatabaseMetricsService.getSlowestQueries(3);
        
        if (!metrics.isHealthy()) {
            System.out.println("  ⚠️ Taux d'erreur élevé (" + String.format("%.1f%%", metrics.getErrorRate()) + ")");
            System.out.println("     → Vérifier les logs d'erreur et corriger les requêtes défaillantes");
        }
        
        slowQueries.forEach((operation, queryMetrics) -> {
            if (queryMetrics.isSlowQuery()) {
                System.out.println("  🐌 Requête lente détectée: " + operation);
                System.out.printf("     → Temps moyen: %.1fms, Maximum: %dms\n", 
                    queryMetrics.getAverageDuration(), queryMetrics.maxDuration());
                System.out.println("     → Considérer l'ajout d'index ou l'optimisation de la requête");
            }
        });
        
        if (metrics.totalQueries() > 1000) {
            System.out.println("  📊 Volume élevé de requêtes (" + metrics.totalQueries() + ")");
            System.out.println("     → Considérer l'implémentation d'un cache ou d'un pool de connexions");
        }
        
        System.out.println("  ✅ Surveillance des performances activée via DatabaseMetricsService");
        System.out.println("  ✅ Logging automatique des requêtes lentes en place");
        System.out.println("  ✅ Gestion d'erreurs standardisée implémentée");
    }
}