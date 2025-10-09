package com.magsav.service;

import com.magsav.util.AppLogger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Service de métriques de performance
 * Mesure et agrège les temps de réponse des opérations critiques
 */
public final class PerformanceMetricsService {
    
    // Métriques par opération
    private static final Map<String, OperationMetrics> metrics = new ConcurrentHashMap<>();
    
    // Seuils d'alerte (en millisecondes)
    private static final long SLOW_OPERATION_THRESHOLD = 1000; // 1 seconde
    private static final long VERY_SLOW_OPERATION_THRESHOLD = 5000; // 5 secondes
    
    // Types d'opérations mesurées
    public static final String OP_LOAD_ALL_PRODUCTS = "load_all_products";
    public static final String OP_SEARCH_PRODUCTS = "search_products";
    public static final String OP_LOAD_PRODUCT_DETAILS = "load_product_details";
    public static final String OP_GET_PRODUCT_INTERVENTIONS = "get_product_interventions";
    public static final String OP_GET_PRODUCT_STATISTICS = "get_product_statistics";
    public static final String OP_LOAD_INTERVENTIONS = "load_interventions";
    public static final String OP_SAVE_PRODUCT = "save_product";
    public static final String OP_SAVE_INTERVENTION = "save_intervention";
    public static final String OP_DATABASE_QUERY = "database_query";
    public static final String OP_CACHE_ACCESS = "cache_access";
    public static final String OP_FILE_IMPORT = "file_import";
    public static final String OP_NAVIGATION = "navigation";
    
    /**
     * Démarre la mesure d'une opération
     */
    public static PerformanceTimer startTimer(String operationName) {
        return new PerformanceTimer(operationName);
    }
    
    /**
     * Enregistre une mesure d'opération
     */
    public static void recordOperation(String operationName, long durationMs) {
        recordOperation(operationName, durationMs, true);
    }
    
    /**
     * Enregistre une mesure d'opération avec statut de succès
     */
    public static void recordOperation(String operationName, long durationMs, boolean success) {
        OperationMetrics opMetrics = metrics.computeIfAbsent(operationName, 
            k -> new OperationMetrics(operationName));
        
        opMetrics.record(durationMs, success);
        
        // Alertes pour les opérations lentes
        if (durationMs > VERY_SLOW_OPERATION_THRESHOLD) {
            AppLogger.warn("performance", "Opération TRÈS LENTE: {} - {}ms", operationName, durationMs);
        } else if (durationMs > SLOW_OPERATION_THRESHOLD) {
            AppLogger.info("performance", "Opération lente: {} - {}ms", operationName, durationMs);
        }
        
        AppLogger.debug("Métriques: {} - {}ms (succès: {})", operationName, durationMs, success);
    }
    
    /**
     * Récupère les métriques d'une opération
     */
    public static OperationMetrics getMetrics(String operationName) {
        return metrics.get(operationName);
    }
    
    /**
     * Récupère toutes les métriques
     */
    public static Map<String, OperationMetrics> getAllMetrics() {
        return new ConcurrentHashMap<>(metrics);
    }
    
    /**
     * Génère un rapport de performance
     */
    public static PerformanceReport generateReport() {
        AppLogger.info("performance", "PerformanceMetricsService: Génération rapport de performance");
        
        List<OperationSummary> summaries = new ArrayList<>();
        long totalOperations = 0;
        long totalDuration = 0;
        int slowOperations = 0;
        
        for (OperationMetrics opMetrics : metrics.values()) {
            OperationSummary summary = opMetrics.getSummary();
            summaries.add(summary);
            
            totalOperations += summary.totalCalls();
            totalDuration += summary.totalDuration();
            
            if (summary.averageDuration() > SLOW_OPERATION_THRESHOLD) {
                slowOperations++;
            }
        }
        
        // Trier par temps total décroissant
        summaries.sort((a, b) -> Long.compare(b.totalDuration(), a.totalDuration()));
        
        double averageResponseTime = totalOperations > 0 ? (double) totalDuration / totalOperations : 0;
        
        return new PerformanceReport(
            summaries,
            totalOperations,
            totalDuration,
            averageResponseTime,
            slowOperations,
            identifyBottlenecks(summaries)
        );
    }
    
    /**
     * Identifie les goulots d'étranglement
     */
    private static List<String> identifyBottlenecks(List<OperationSummary> summaries) {
        return summaries.stream()
            .filter(s -> s.averageDuration() > SLOW_OPERATION_THRESHOLD || 
                        s.maxDuration() > VERY_SLOW_OPERATION_THRESHOLD)
            .map(s -> String.format("%s (moy: %dms, max: %dms)", 
                s.operationName(), s.averageDuration(), s.maxDuration()))
            .collect(Collectors.toList());
    }
    
    /**
     * Remet à zéro toutes les métriques
     */
    public static void resetMetrics() {
        AppLogger.info("performance", "PerformanceMetricsService: Reset des métriques");
        metrics.clear();
    }
    
    /**
     * Remet à zéro les métriques d'une opération spécifique
     */
    public static void resetMetrics(String operationName) {
        AppLogger.info("performance", "PerformanceMetricsService: Reset métriques {}", operationName);
        metrics.remove(operationName);
    }
    
    /**
     * Affiche un résumé des métriques dans les logs
     */
    public static void logSummary() {
        PerformanceReport report = generateReport();
        
        AppLogger.info("performance", "=== RÉSUMÉ PERFORMANCE ===");
        AppLogger.info("performance", "Total opérations: {}", report.totalOperations());
        AppLogger.info("performance", "Temps de réponse moyen: {:.1f}ms", report.averageResponseTime());
        AppLogger.info("performance", "Opérations lentes: {}", report.slowOperations());
        
        if (!report.bottlenecks().isEmpty()) {
            AppLogger.warn("performance", "Goulots d'étranglement détectés:");
            report.bottlenecks().forEach(bottleneck -> 
                AppLogger.warn("performance", "  - {}", bottleneck));
        }
        
        AppLogger.info("performance", "Top 5 opérations par temps total:");
        report.operationSummaries().stream()
            .limit(5)
            .forEach(summary -> 
                AppLogger.info("performance", "  {} - {}ms total, {:.1f}ms moyen ({} appels)",
                    summary.operationName(), summary.totalDuration(), 
                    summary.averageDuration(), summary.totalCalls()));
    }
    
    /**
     * Timer pour mesurer la performance d'une opération
     */
    public static class PerformanceTimer implements AutoCloseable {
        private final String operationName;
        private final long startTime;
        private boolean success = true;
        
        public PerformanceTimer(String operationName) {
            this.operationName = operationName;
            this.startTime = System.currentTimeMillis();
            AppLogger.debug("Début mesure: {}", operationName);
        }
        
        public void markFailure() {
            this.success = false;
        }
        
        @Override
        public void close() {
            long duration = System.currentTimeMillis() - startTime;
            recordOperation(operationName, duration, success);
        }
    }
    
    /**
     * Métriques pour une opération spécifique
     */
    private static class OperationMetrics {
        private final String operationName;
        private final LongAdder totalCalls = new LongAdder();
        private final LongAdder successfulCalls = new LongAdder();
        private final LongAdder totalDuration = new LongAdder();
        private final AtomicLong minDuration = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxDuration = new AtomicLong(0);
        
        public OperationMetrics(String operationName) {
            this.operationName = operationName;
        }
        
        public void record(long durationMs, boolean success) {
            totalCalls.increment();
            if (success) {
                successfulCalls.increment();
            }
            
            totalDuration.add(durationMs);
            
            // Mise à jour min/max de façon thread-safe
            minDuration.updateAndGet(current -> Math.min(current, durationMs));
            maxDuration.updateAndGet(current -> Math.max(current, durationMs));
        }
        
        public OperationSummary getSummary() {
            long calls = totalCalls.sum();
            long duration = totalDuration.sum();
            long successful = successfulCalls.sum();
            
            long avgDuration = calls > 0 ? duration / calls : 0;
            double successRate = calls > 0 ? (double) successful / calls * 100 : 0;
            
            return new OperationSummary(
                operationName,
                calls,
                successful,
                duration,
                avgDuration,
                minDuration.get() == Long.MAX_VALUE ? 0 : minDuration.get(),
                maxDuration.get(),
                successRate
            );
        }
    }
    
    /**
     * Résumé des métriques d'une opération
     */
    public record OperationSummary(
        String operationName,
        long totalCalls,
        long successfulCalls,
        long totalDuration,
        long averageDuration,
        long minDuration,
        long maxDuration,
        double successRate
    ) {}
    
    /**
     * Rapport complet de performance
     */
    public record PerformanceReport(
        List<OperationSummary> operationSummaries,
        long totalOperations,
        long totalDuration,
        double averageResponseTime,
        int slowOperations,
        List<String> bottlenecks
    ) {}

    private PerformanceMetricsService() {}
}