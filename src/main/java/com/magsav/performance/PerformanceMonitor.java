package com.magsav.performance;

import com.magsav.cache.CacheManager;
import com.magsav.db.ConnectionPool;
import com.magsav.db.DB;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service de monitoring des performances pour MAGSAV
 */
public class PerformanceMonitor {
    private static final PerformanceMonitor INSTANCE = new PerformanceMonitor();
    
    private final AtomicLong totalQueries = new AtomicLong(0);
    private final AtomicLong totalCacheHits = new AtomicLong(0);
    private final AtomicLong totalCacheMisses = new AtomicLong(0);
    private final ConcurrentHashMap<String, QueryStats> queryStats = new ConcurrentHashMap<>();
    
    private PerformanceMonitor() {}
    
    public static PerformanceMonitor getInstance() {
        return INSTANCE;
    }
    
    /**
     * Enregistre l'exécution d'une requête
     */
    public void recordQuery(String operation, long durationMs, boolean fromCache) {
        totalQueries.incrementAndGet();
        
        if (fromCache) {
            totalCacheHits.incrementAndGet();
        } else {
            totalCacheMisses.incrementAndGet();
        }
        
        queryStats.compute(operation, (key, stats) -> {
            if (stats == null) {
                stats = new QueryStats();
            }
            stats.addExecution(durationMs, fromCache);
            return stats;
        });
    }
    
    /**
     * Génère un rapport de performance
     */
    public PerformanceReport generateReport() {
        CacheManager.CacheStats cacheStats = CacheManager.getInstance().getStats();
        ConnectionPool.Stats connectionStats = DB.getConnectionStats();
        
        return new PerformanceReport(
            totalQueries.get(),
            totalCacheHits.get(),
            totalCacheMisses.get(),
            getCacheHitRatio(),
            cacheStats,
            connectionStats,
            new ConcurrentHashMap<>(queryStats) // Copie pour éviter les modifications concurrentes
        );
    }
    
    /**
     * Calcule le taux de succès du cache
     */
    public double getCacheHitRatio() {
        long hits = totalCacheHits.get();
        long total = totalQueries.get();
        return total > 0 ? (double) hits / total * 100 : 0;
    }
    
    /**
     * Remet à zéro les statistiques
     */
    public void reset() {
        totalQueries.set(0);
        totalCacheHits.set(0);
        totalCacheMisses.set(0);
        queryStats.clear();
    }
    
    /**
     * Statistiques pour une opération spécifique
     */
    public static class QueryStats {
        private final AtomicLong executions = new AtomicLong(0);
        private final AtomicLong totalDuration = new AtomicLong(0);
        private final AtomicLong cacheHits = new AtomicLong(0);
        private long minDuration = Long.MAX_VALUE;
        private long maxDuration = 0;
        
        public void addExecution(long durationMs, boolean fromCache) {
            executions.incrementAndGet();
            
            if (!fromCache) {
                totalDuration.addAndGet(durationMs);
                minDuration = Math.min(minDuration, durationMs);
                maxDuration = Math.max(maxDuration, durationMs);
            } else {
                cacheHits.incrementAndGet();
            }
        }
        
        public long getExecutions() { return executions.get(); }
        public long getTotalDuration() { return totalDuration.get(); }
        public long getCacheHits() { return cacheHits.get(); }
        public long getMinDuration() { return minDuration == Long.MAX_VALUE ? 0 : minDuration; }
        public long getMaxDuration() { return maxDuration; }
        
        public double getAverageDuration() {
            long dbExecutions = executions.get() - cacheHits.get();
            return dbExecutions > 0 ? (double) totalDuration.get() / dbExecutions : 0;
        }
        
        public double getCacheHitRatio() {
            long total = executions.get();
            return total > 0 ? (double) cacheHits.get() / total * 100 : 0;
        }
        
        @Override
        public String toString() {
            return String.format(
                "Exécutions: %d, Cache: %.1f%%, Durée moy: %.1fms (min: %dms, max: %dms)",
                executions.get(), getCacheHitRatio(), getAverageDuration(),
                getMinDuration(), getMaxDuration()
            );
        }
    }
    
    /**
     * Rapport de performance complet
     */
    public static class PerformanceReport {
        private final long totalQueries;
        private final long cacheHits;
        private final long cacheMisses;
        private final double cacheHitRatio;
        private final CacheManager.CacheStats cacheStats;
        private final ConnectionPool.Stats connectionStats;
        private final ConcurrentHashMap<String, QueryStats> queryStats;
        
        public PerformanceReport(long totalQueries, long cacheHits, long cacheMisses, 
                               double cacheHitRatio, CacheManager.CacheStats cacheStats,
                               ConnectionPool.Stats connectionStats,
                               ConcurrentHashMap<String, QueryStats> queryStats) {
            this.totalQueries = totalQueries;
            this.cacheHits = cacheHits;
            this.cacheMisses = cacheMisses;
            this.cacheHitRatio = cacheHitRatio;
            this.cacheStats = cacheStats;
            this.connectionStats = connectionStats;
            this.queryStats = queryStats;
        }
        
        public String generateSummary() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== RAPPORT DE PERFORMANCE MAGSAV ===\n");
            sb.append(String.format("Total requêtes: %d\n", totalQueries));
            sb.append(String.format("Cache hit ratio: %.1f%% (%d hits, %d misses)\n", 
                cacheHitRatio, cacheHits, cacheMisses));
            sb.append(String.format("%s\n", cacheStats));
            sb.append(String.format("%s\n", connectionStats));
            
            if (!queryStats.isEmpty()) {
                sb.append("\n=== STATISTIQUES PAR OPÉRATION ===\n");
                queryStats.entrySet().stream()
                    .sorted((e1, e2) -> Long.compare(e2.getValue().getExecutions(), e1.getValue().getExecutions()))
                    .forEach(entry -> sb.append(String.format("%-30s: %s\n", entry.getKey(), entry.getValue())));
            }
            
            sb.append("=====================================\n");
            return sb.toString();
        }
        
        // Getters
        public long getTotalQueries() { return totalQueries; }
        public long getCacheHits() { return cacheHits; }
        public long getCacheMisses() { return cacheMisses; }
        public double getCacheHitRatio() { return cacheHitRatio; }
        public CacheManager.CacheStats getCacheStats() { return cacheStats; }
        public ConnectionPool.Stats getConnectionStats() { return connectionStats; }
        public ConcurrentHashMap<String, QueryStats> getQueryStats() { return queryStats; }
    }
}