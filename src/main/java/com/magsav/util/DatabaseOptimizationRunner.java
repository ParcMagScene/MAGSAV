package com.magsav.util;

import com.magsav.service.DatabaseOptimizationService;

/**
 * Utilitaire pour tester et appliquer les optimisations de base de données
 */
public class DatabaseOptimizationRunner {
    
    public static void main(String[] args) {
        System.out.println("=== MAGSAV Database Optimization ===");
        System.out.println();
        
        // 1. Analyse des performances avant optimisation
        System.out.println("1. Analyse des performances AVANT optimisation:");
        DatabaseOptimizationService.QueryAnalysisResult beforeAnalysis = 
            DatabaseOptimizationService.analyzeQueryPerformance();
        
        printQueryAnalysis(beforeAnalysis);
        System.out.println();
        
        // 2. Application des index recommandés
        System.out.println("2. Application des index recommandés:");
        DatabaseOptimizationService.OptimizationResult optimization = 
            DatabaseOptimizationService.applyRecommendedIndexes();
        
        printOptimizationResult(optimization);
        System.out.println();
        
        // 3. Maintenance de la base
        System.out.println("3. Maintenance de la base de données:");
        DatabaseOptimizationService.DatabaseMaintenanceResult maintenance = 
            DatabaseOptimizationService.performMaintenance();
        
        printMaintenanceResult(maintenance);
        System.out.println();
        
        // 4. Analyse des performances après optimisation
        System.out.println("4. Analyse des performances APRÈS optimisation:");
        DatabaseOptimizationService.QueryAnalysisResult afterAnalysis = 
            DatabaseOptimizationService.analyzeQueryPerformance();
        
        printQueryAnalysis(afterAnalysis);
        System.out.println();
        
        // 5. Comparaison des performances
        System.out.println("5. Comparaison des performances:");
        comparePerformances(beforeAnalysis, afterAnalysis);
        
        System.out.println("=== Optimisation terminée ===");
    }
    
    private static void printQueryAnalysis(DatabaseOptimizationService.QueryAnalysisResult analysis) {
        for (DatabaseOptimizationService.QueryPerformance perf : analysis.queryResults()) {
            if (perf.success()) {
                System.out.printf("  ✅ %-25s: %3d ms (%d lignes)%n", 
                    perf.name(), perf.durationMs(), perf.rowCount());
            } else {
                System.out.printf("  ❌ %-25s: ERREUR - %s%n", 
                    perf.name(), perf.error());
            }
        }
    }
    
    private static void printOptimizationResult(DatabaseOptimizationService.OptimizationResult result) {
        System.out.println("  Index créés:");
        if (result.createdIndexes().isEmpty()) {
            System.out.println("    (aucun)");
        } else {
            result.createdIndexes().forEach(index -> System.out.println("    ✅ " + index));
        }
        
        System.out.println("  Index ignorés:");
        if (result.skippedIndexes().isEmpty()) {
            System.out.println("    (aucun)");
        } else {
            result.skippedIndexes().forEach(index -> System.out.println("    ⏭️  " + index));
        }
        
        if (!result.errors().isEmpty()) {
            System.out.println("  Erreurs:");
            result.errors().forEach(error -> System.out.println("    ❌ " + error));
        }
    }
    
    private static void printMaintenanceResult(DatabaseOptimizationService.DatabaseMaintenanceResult result) {
        System.out.printf("  Durée: %d ms%n", result.durationMs());
        
        System.out.println("  Opérations:");
        if (result.operations().isEmpty()) {
            System.out.println("    (aucune)");
        } else {
            result.operations().forEach(op -> System.out.println("    ✅ " + op));
        }
        
        if (!result.errors().isEmpty()) {
            System.out.println("  Erreurs:");
            result.errors().forEach(error -> System.out.println("    ❌ " + error));
        }
    }
    
    private static void comparePerformances(
        DatabaseOptimizationService.QueryAnalysisResult before,
        DatabaseOptimizationService.QueryAnalysisResult after) {
        
        for (int i = 0; i < Math.min(before.queryResults().size(), after.queryResults().size()); i++) {
            DatabaseOptimizationService.QueryPerformance beforePerf = before.queryResults().get(i);
            DatabaseOptimizationService.QueryPerformance afterPerf = after.queryResults().get(i);
            
            if (beforePerf.success() && afterPerf.success()) {
                long improvement = beforePerf.durationMs() - afterPerf.durationMs();
                double percentImprovement = beforePerf.durationMs() > 0 ? 
                    (improvement * 100.0 / beforePerf.durationMs()) : 0;
                
                String icon = improvement > 0 ? "🚀" : improvement < 0 ? "⚠️" : "➡️";
                System.out.printf("  %s %-25s: %3d ms → %3d ms (%+.1f%%)%n", 
                    icon, beforePerf.name(), beforePerf.durationMs(), afterPerf.durationMs(), percentImprovement);
            }
        }
    }
}