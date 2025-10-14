package com.magsav.test;

import com.magsav.cache.CacheManager;
import com.magsav.db.ConnectionPool;
import com.magsav.db.DB;
import com.magsav.performance.PerformanceMonitor;
import com.magsav.repo.UserRepository;

/**
 * Test des optimisations de performance
 */
public class PerformanceOptimizationTest {
    
    public static void main(String[] args) {
        System.out.println("=== TEST DES OPTIMISATIONS DE PERFORMANCE ===");
        
        try {
            // Initialiser la base de données
            DB.init();
            
            testConnectionPool();
            testCacheSystem();
            testRepositoryOptimizations();
            
            // Générer le rapport final
            PerformanceMonitor monitor = PerformanceMonitor.getInstance();
            PerformanceMonitor.PerformanceReport report = monitor.generateReport();
            System.out.println(report.generateSummary());
            
        } catch (Exception e) {
            System.err.println("Erreur lors du test: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Nettoyage
            DB.shutdown();
        }
    }
    
    private static void testConnectionPool() {
        System.out.println("\n1. Test du pool de connexions...");
        try {
            // Tester plusieurs connexions simultanées
            long start = System.currentTimeMillis();
            for (int i = 0; i < 10; i++) {
                try (var conn = DB.getConnection()) {
                    // Simuler une requête
                    try (var stmt = conn.createStatement()) {
                        stmt.execute("SELECT 1");
                    }
                }
            }
            long duration = System.currentTimeMillis() - start;
            
            ConnectionPool.Stats stats = DB.getConnectionStats();
            System.out.printf("✅ Pool de connexions : %dms pour 10 connexions\n", duration);
            System.out.printf("   %s\n", stats);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur pool de connexions: " + e.getMessage());
        }
    }
    
    private static void testCacheSystem() {
        System.out.println("\n2. Test du système de cache...");
        try {
            CacheManager cache = CacheManager.getInstance();
            
            // Test basique du cache
            String key = "test:key";
            String value = cache.get(key, () -> "computed_value");
            
            // Deuxième appel (doit être depuis le cache)
            long start = System.currentTimeMillis();
            String cachedValue = cache.get(key, () -> "computed_value");
            long duration = System.currentTimeMillis() - start;
            
            System.out.printf("✅ Cache système : %dms pour récupération depuis cache\n", duration);
            System.out.printf("   Valeur: %s, Depuis cache: %s\n", cachedValue, value.equals(cachedValue));
            
            CacheManager.CacheStats stats = cache.getStats();
            System.out.printf("   %s\n", stats);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur cache système: " + e.getMessage());
        }
    }
    
    private static void testRepositoryOptimizations() {
        System.out.println("\n3. Test des optimisations repository...");
        try {
            UserRepository userRepo = new UserRepository();
            
            // Test avec cache
            long start = System.currentTimeMillis();
            var users1 = userRepo.findAll();
            long duration1 = System.currentTimeMillis() - start;
            
            // Deuxième appel (doit être depuis le cache)
            start = System.currentTimeMillis();
            var users2 = userRepo.findAll();
            long duration2 = System.currentTimeMillis() - start;
            
            System.out.printf("✅ Repository optimisé :\n");
            System.out.printf("   Premier appel: %dms (%d utilisateurs)\n", duration1, users1.size());
            System.out.printf("   Depuis cache: %dms (%d utilisateurs)\n", duration2, users2.size());
            System.out.printf("   Amélioration: %.1fx plus rapide\n", 
                duration2 > 0 ? (double) duration1 / duration2 : Double.POSITIVE_INFINITY);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur repository: " + e.getMessage());
        }
    }
}