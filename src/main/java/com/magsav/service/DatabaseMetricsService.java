package com.magsav.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service centralisé pour collecter et analyser les métriques de performance de la base de données
 */
public final class DatabaseMetricsService {
    
    private static final Map<String, QueryMetrics> metrics = new ConcurrentHashMap<>();
    private static final AtomicLong totalQueries = new AtomicLong(0);
    private static final AtomicLong totalErrors = new AtomicLong(0);
    
    /**
     * Enregistre l'exécution d'une requête
     */
    public static void recordQuery(String operation, long durationMs, boolean success) {
        totalQueries.incrementAndGet();
        if (!success) {
            totalErrors.incrementAndGet();
        }
        
        metrics.compute(operation, (key, existing) -> {
            if (existing == null) {
                return new QueryMetrics(1, durationMs, durationMs, durationMs, success ? 1 : 0);
            } else {
                return existing.addExecution(durationMs, success);
            }
        });
    }
    
    /**
     * Récupère toutes les métriques actuelles
     */
    public static Map<String, QueryMetrics> getMetrics() {
        return Map.copyOf(metrics);
    }
    
    /**
     * Récupère les statistiques globales
     */
    public static GlobalMetrics getGlobalMetrics() {
        return new GlobalMetrics(
            totalQueries.get(),
            totalErrors.get(),
            metrics.size()
        );
    }
    
    /**
     * Remet à zéro toutes les métriques
     */
    public static void resetMetrics() {
        metrics.clear();
        totalQueries.set(0);
        totalErrors.set(0);
    }
    
    /**
     * Récupère les requêtes les plus lentes
     */
    public static Map<String, QueryMetrics> getSlowestQueries(int limit) {
        return metrics.entrySet().stream()
            .sorted((e1, e2) -> Long.compare(e2.getValue().maxDuration(), e1.getValue().maxDuration()))
            .limit(limit)
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                java.util.LinkedHashMap::new
            ));
    }
    
    /**
     * Récupère les requêtes les plus fréquentes
     */
    public static Map<String, QueryMetrics> getMostFrequentQueries(int limit) {
        return metrics.entrySet().stream()
            .sorted((e1, e2) -> Integer.compare(e2.getValue().executionCount(), e1.getValue().executionCount()))
            .limit(limit)
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                java.util.LinkedHashMap::new
            ));
    }
    
    /**
     * Métrique pour une requête spécifique
     */
    public record QueryMetrics(
        int executionCount, 
        long totalDuration, 
        long minDuration, 
        long maxDuration, 
        int successCount
    ) {
        
        public QueryMetrics addExecution(long duration, boolean success) {
            return new QueryMetrics(
                executionCount + 1,
                totalDuration + duration,
                Math.min(minDuration, duration),
                Math.max(maxDuration, duration),
                successCount + (success ? 1 : 0)
            );
        }
        
        public double getAverageDuration() {
            return executionCount > 0 ? (double) totalDuration / executionCount : 0;
        }
        
        public double getSuccessRate() {
            return executionCount > 0 ? (double) successCount / executionCount * 100 : 0;
        }
        
        public boolean isSlowQuery() {
            return getAverageDuration() > 1000; // Plus de 1 seconde en moyenne
        }
        
        public boolean hasErrors() {
            return successCount < executionCount;
        }
    }
    
    /**
     * Métriques globales du système
     */
    public record GlobalMetrics(
        long totalQueries,
        long totalErrors,
        int uniqueOperations
    ) {
        public double getErrorRate() {
            return totalQueries > 0 ? (double) totalErrors / totalQueries * 100 : 0;
        }
        
        public boolean isHealthy() {
            return getErrorRate() < 5; // Moins de 5% d'erreurs
        }
    }
    
    /**
     * Génère un rapport détaillé des métriques
     */
    public static String generateReport() {
        StringBuilder report = new StringBuilder();
        GlobalMetrics global = getGlobalMetrics();
        
        report.append("=== RAPPORT MÉTRIQUES BASE DE DONNÉES ===\n");
        report.append(String.format("Total requêtes: %d\n", global.totalQueries()));
        report.append(String.format("Total erreurs: %d (%.2f%%)\n", global.totalErrors(), global.getErrorRate()));
        report.append(String.format("Opérations uniques: %d\n", global.uniqueOperations()));
        report.append(String.format("État: %s\n", global.isHealthy() ? "✅ SAIN" : "⚠️ ATTENTION"));
        report.append("\n");
        
        // Top 5 requêtes les plus lentes
        report.append("=== TOP 5 REQUÊTES LES PLUS LENTES ===\n");
        getSlowestQueries(5).forEach((operation, metrics) -> {
            report.append(String.format("%-30s | Avg: %6.1fms | Max: %6dms | Count: %4d | Success: %5.1f%%\n",
                operation, metrics.getAverageDuration(), metrics.maxDuration(), 
                metrics.executionCount(), metrics.getSuccessRate()));
        });
        report.append("\n");
        
        // Top 5 requêtes les plus fréquentes
        report.append("=== TOP 5 REQUÊTES LES PLUS FRÉQUENTES ===\n");
        getMostFrequentQueries(5).forEach((operation, metrics) -> {
            report.append(String.format("%-30s | Count: %4d | Avg: %6.1fms | Success: %5.1f%%\n",
                operation, metrics.executionCount(), metrics.getAverageDuration(), metrics.getSuccessRate()));
        });
        
        return report.toString();
    }
    
    private DatabaseMetricsService() {
        // Utility class
    }
}