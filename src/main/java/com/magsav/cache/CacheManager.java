package com.magsav.cache;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Gestionnaire de cache simple avec TTL pour optimiser les performances
 */
public class CacheManager {
    private static final CacheManager INSTANCE = new CacheManager();
    
    private final ConcurrentHashMap<String, CacheEntry<?>> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "Cache-Cleanup");
        t.setDaemon(true);
        return t;
    });
    
    // TTL par défaut de 5 minutes
    private static final int DEFAULT_TTL_MINUTES = 5;
    
    private CacheManager() {
        // Nettoyage automatique toutes les minutes
        cleanupExecutor.scheduleAtFixedRate(this::cleanup, 1, 1, TimeUnit.MINUTES);
    }
    
    public static CacheManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Récupère une valeur du cache ou l'exécute si elle n'existe pas
     */
    public <T> T get(String key, Supplier<T> supplier) {
        return get(key, supplier, DEFAULT_TTL_MINUTES);
    }
    
    /**
     * Récupère une valeur du cache ou l'exécute si elle n'existe pas avec TTL personnalisé
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Supplier<T> supplier, int ttlMinutes) {
        CacheEntry<T> entry = (CacheEntry<T>) cache.get(key);
        
        if (entry != null && !entry.isExpired()) {
            return entry.getValue();
        }
        
        // Exécuter et mettre en cache
        T value = supplier.get();
        cache.put(key, new CacheEntry<>(value, ttlMinutes));
        return value;
    }
    
    /**
     * Met une valeur en cache directement
     */
    public <T> void put(String key, T value) {
        put(key, value, DEFAULT_TTL_MINUTES);
    }
    
    /**
     * Met une valeur en cache avec TTL personnalisé
     */
    public <T> void put(String key, T value, int ttlMinutes) {
        cache.put(key, new CacheEntry<>(value, ttlMinutes));
    }
    
    /**
     * Invalide une entrée du cache
     */
    public void invalidate(String key) {
        cache.remove(key);
    }
    
    /**
     * Invalide toutes les entrées qui commencent par le préfixe
     */
    public void invalidatePrefix(String prefix) {
        cache.entrySet().removeIf(entry -> entry.getKey().startsWith(prefix));
    }
    
    /**
     * Vide complètement le cache
     */
    public void clear() {
        cache.clear();
    }
    
    /**
     * Nettoyage des entrées expirées
     */
    private void cleanup() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * Statistiques du cache
     */
    public CacheStats getStats() {
        int total = cache.size();
        long expired = cache.values().stream().mapToLong(entry -> entry.isExpired() ? 1 : 0).sum();
        return new CacheStats(total, (int) expired, total - (int) expired);
    }
    
    /**
     * Arrêt propre du gestionnaire de cache
     */
    public void shutdown() {
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        cache.clear();
    }
    
    /**
     * Entrée de cache avec expiration
     */
    private static class CacheEntry<T> {
        private final T value;
        private final LocalDateTime expirationTime;
        
        public CacheEntry(T value, int ttlMinutes) {
            this.value = value;
            this.expirationTime = LocalDateTime.now().plus(ttlMinutes, ChronoUnit.MINUTES);
        }
        
        public T getValue() {
            return value;
        }
        
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expirationTime);
        }
    }
    
    /**
     * Statistiques du cache
     */
    public static class CacheStats {
        private final int totalEntries;
        private final int expiredEntries;
        private final int validEntries;
        
        public CacheStats(int totalEntries, int expiredEntries, int validEntries) {
            this.totalEntries = totalEntries;
            this.expiredEntries = expiredEntries;
            this.validEntries = validEntries;
        }
        
        public int getTotalEntries() { return totalEntries; }
        public int getExpiredEntries() { return expiredEntries; }
        public int getValidEntries() { return validEntries; }
        
        @Override
        public String toString() {
            return String.format("Cache: %d entrées (%d valides, %d expirées)", 
                totalEntries, validEntries, expiredEntries);
        }
    }
}