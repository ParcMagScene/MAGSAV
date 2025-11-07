package com.magscene.magsav.desktop.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * Utilitaire pour le monitoring des performances mémoire
 * Permet le profiling en temps réel pour l'optimisation
 */
public class MemoryProfiler {
    
    private static final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private static final double MB = 1024.0 * 1024.0;
    
    /**
     * Affiche l'état actuel de la mémoire
     */
    public static void logMemoryUsage(String context) {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        
        System.out.printf("📊 [%s] Mémoire - Heap: %.1f/%.1f MB | Non-Heap: %.1f MB%n", 
            context,
            heapUsage.getUsed() / MB,
            heapUsage.getMax() / MB,
            nonHeapUsage.getUsed() / MB
        );
    }
    
    /**
     * Retourne l'utilisation mémoire heap en MB
     */
    public static double getHeapUsageMB() {
        return memoryBean.getHeapMemoryUsage().getUsed() / MB;
    }
    
    /**
     * Retourne l'utilisation mémoire maximale en MB
     */
    public static double getMaxHeapMB() {
        return memoryBean.getHeapMemoryUsage().getMax() / MB;
    }
    
    /**
     * Force un garbage collection et log l'effet
     */
    public static void forceGCAndLog(String context) {
        double beforeGC = getHeapUsageMB();
        System.gc();
        // Attendre un peu pour que le GC agisse
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        double afterGC = getHeapUsageMB();
        
        System.out.printf("🗑️ [%s] GC: %.1f MB → %.1f MB (libéré: %.1f MB)%n", 
            context, beforeGC, afterGC, beforeGC - afterGC);
    }
    
    /**
     * Vérifie si l'utilisation mémoire dépasse le seuil (en pourcentage)
     */
    public static boolean isMemoryUsageHigh(double thresholdPercent) {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        double usagePercent = (double) heapUsage.getUsed() / heapUsage.getMax() * 100;
        return usagePercent > thresholdPercent;
    }
    
    /**
     * Monitoring continu de la mémoire (pour debug)
     */
    public static void startContinuousMonitoring(String context, long intervalMs) {
        Thread monitorThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                logMemoryUsage(context);
                try {
                    Thread.sleep(intervalMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        monitorThread.setDaemon(true);
        monitorThread.setName("MemoryProfiler-" + context);
        monitorThread.start();
    }
}
