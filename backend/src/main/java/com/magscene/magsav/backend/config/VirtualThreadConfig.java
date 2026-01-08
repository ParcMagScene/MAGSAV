package com.magscene.magsav.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Configuration des Virtual Threads pour MAGSAV-3.0
 * Active les threads virtuels pour améliorer les performances
 */
@Configuration
@EnableAsync
public class VirtualThreadConfig {

    /**
     * Crée un ThreadFactory pour Virtual Threads
     * Utilise directement l'API Java 21 sans réflection
     */
    private ThreadFactory createVirtualThreadFactory(String prefix) {
        try {
            // Java 21+: Utilisation directe de l'API Virtual Threads
            return Thread.ofVirtual()
                    .name(prefix, 0L)
                    .factory();
        } catch (Exception e) {
            System.out.println("⚠️ Virtual Threads non disponibles, utilisation threads classiques");
            // Fallback vers threads classiques
            return Executors.defaultThreadFactory();
        }
    }

    /**
     * Vérifie si les Virtual Threads sont disponibles
     */
    private boolean isVirtualThreadsAvailable() {
        try {
            Thread.class.getMethod("ofVirtual");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Configurateur principal pour Virtual Threads
     */
    @Bean(name = "virtualThreadExecutor")
    public Executor virtualThreadExecutor() {
        if (isVirtualThreadsAvailable()) {
            System.out.println("🧵 MAGSAV-3.0: Virtual Threads ACTIVÉS avec Java 21!");
            return Executors.newCachedThreadPool(createVirtualThreadFactory("magsav-vt-"));
        } else {
            System.out.println("⚠️ MAGSAV-3.0: Virtual Threads non disponibles, utilisation pool classique");
            return createFallbackExecutor("magsav-classic-");
        }
    }

    /**
     * Executor pour les tâches asynchrones Spring
     */
    @Bean(name = "asyncTaskExecutor")
    public Executor asyncTaskExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("magsav-async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * Executor spécialisé pour les opérations SAV
     */
    @Bean(name = "savTaskExecutor")
    public Executor savTaskExecutor() {
        if (isVirtualThreadsAvailable()) {
            return Executors.newCachedThreadPool(createVirtualThreadFactory("sav-vt-"));
        } else {
            return createFallbackExecutor("sav-");
        }
    }

    /**
     * Executor pour le traitement des QR codes
     */
    @Bean(name = "qrScanExecutor")
    public Executor qrScanExecutor() {
        if (isVirtualThreadsAvailable()) {
            return Executors.newCachedThreadPool(createVirtualThreadFactory("qr-vt-"));
        } else {
            return createFallbackExecutor("qr-");
        }
    }

    /**
     * Executor pour la synchronisation des véhicules
     */
    @Bean(name = "vehicleSyncExecutor")
    public Executor vehicleSyncExecutor() {
        if (isVirtualThreadsAvailable()) {
            return Executors.newCachedThreadPool(createVirtualThreadFactory("vehicle-vt-"));
        } else {
            return createFallbackExecutor("vehicle-");
        }
    }

    /**
     * Crée un executor classique en fallback
     */
    private Executor createFallbackExecutor(String namePrefix) {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix(namePrefix);
        executor.initialize();
        return executor;
    }
}
