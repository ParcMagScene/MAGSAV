package com.magscene.magsav.desktop.utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Gestionnaire automatique de nettoyage des ressources pour optimiser la mémoire
 * Utilise WeakReference et nettoyage périodique pour éviter les fuites mémoire
 */
public class ResourceCleanupManager {
    
    private static final ResourceCleanupManager INSTANCE = new ResourceCleanupManager();
    private final ScheduledExecutorService cleanupExecutor;
    private final List<WeakReference<AutoCloseable>> managedResources;
    private final List<Runnable> cleanupTasks;
    
    // Configuration
    private static final long CLEANUP_INTERVAL_SECONDS = 30;
    private static final long MEMORY_CHECK_INTERVAL_SECONDS = 10;
    private static final double MEMORY_PRESSURE_THRESHOLD = 80.0; // 80% de la heap
    
    private ResourceCleanupManager() {
        this.cleanupExecutor = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "ResourceCleanup");
            t.setDaemon(true);
            return t;
        });
        this.managedResources = new ArrayList<>();
        this.cleanupTasks = new ArrayList<>();
        
        startPeriodicCleanup();
        startMemoryMonitoring();
    }
    
    public static ResourceCleanupManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Enregistre une ressource pour nettoyage automatique
     */
    public void registerResource(AutoCloseable resource) {
        synchronized (managedResources) {
            managedResources.add(new WeakReference<>(resource));
        }
        System.out.println("🗂️ Ressource enregistrée pour nettoyage automatique");
    }
    
    /**
     * Enregistre une tâche de nettoyage personnalisée
     */
    public void registerCleanupTask(Runnable cleanupTask) {
        synchronized (cleanupTasks) {
            cleanupTasks.add(cleanupTask);
        }
        System.out.println("🧹 Tâche de nettoyage personnalisée enregistrée");
    }
    
    /**
     * Nettoyage périodique des ressources
     */
    private void startPeriodicCleanup() {
        cleanupExecutor.scheduleWithFixedDelay(() -> {
            try {
                performCleanup();
            } catch (Exception e) {
                System.err.println("Erreur lors du nettoyage périodique: " + e.getMessage());
            }
        }, CLEANUP_INTERVAL_SECONDS, CLEANUP_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }
    
    /**
     * Monitoring de la mémoire avec cleanup forcé si nécessaire
     */
    private void startMemoryMonitoring() {
        cleanupExecutor.scheduleWithFixedDelay(() -> {
            try {
                if (MemoryProfiler.isMemoryUsageHigh(MEMORY_PRESSURE_THRESHOLD)) {
                    System.out.println("⚠️ Pression mémoire détectée - nettoyage forcé");
                    performCleanup();
                    MemoryProfiler.forceGCAndLog("Memory Pressure Cleanup");
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du monitoring mémoire: " + e.getMessage());
            }
        }, MEMORY_CHECK_INTERVAL_SECONDS, MEMORY_CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }
    
    /**
     * Effectue le nettoyage des ressources
     */
    public void performCleanup() {
        int cleanedResources = 0;
        int cleanedTasks = 0;
        
        // Nettoyage des ressources AutoCloseable
        synchronized (managedResources) {
            Iterator<WeakReference<AutoCloseable>> iterator = managedResources.iterator();
            while (iterator.hasNext()) {
                WeakReference<AutoCloseable> ref = iterator.next();
                AutoCloseable resource = ref.get();
                
                if (resource == null) {
                    // Ressource déjà garbage collectée
                    iterator.remove();
                    cleanedResources++;
                } else {
                    try {
                        resource.close();
                        iterator.remove();
                        cleanedResources++;
                    } catch (Exception e) {
                        System.err.println("Erreur fermeture ressource: " + e.getMessage());
                    }
                }
            }
        }
        
        // Exécution des tâches de nettoyage
        synchronized (cleanupTasks) {
            Iterator<Runnable> iterator = cleanupTasks.iterator();
            while (iterator.hasNext()) {
                try {
                    iterator.next().run();
                    cleanedTasks++;
                } catch (Exception e) {
                    System.err.println("Erreur tâche nettoyage: " + e.getMessage());
                }
            }
        }
        
        if (cleanedResources > 0 || cleanedTasks > 0) {
            System.out.printf("🧹 Nettoyage: %d ressources, %d tâches exécutées%n", 
                cleanedResources, cleanedTasks);
        }
    }
    
    /**
     * Nettoyage d'urgence en cas de forte pression mémoire
     */
    public void emergencyCleanup() {
        System.out.println("🚨 Nettoyage d'urgence activé");
        
        performCleanup();
        
        // Force plusieurs GC
        for (int i = 0; i < 3; i++) {
            System.gc();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        MemoryProfiler.logMemoryUsage("Emergency Cleanup Complete");
    }
    
    /**
     * Statistiques du gestionnaire
     */
    public void logStatistics() {
        synchronized (managedResources) {
            synchronized (cleanupTasks) {
                System.out.printf("📊 ResourceManager: %d ressources, %d tâches surveillées%n",
                    managedResources.size(), cleanupTasks.size());
            }
        }
    }
    
    /**
     * Arrêt propre du gestionnaire
     */
    public void shutdown() {
        System.out.println("🛑 Arrêt ResourceCleanupManager...");
        performCleanup();
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("✅ ResourceCleanupManager arrêté");
    }
}
