package com.magsav.test;

import com.magsav.config.AppConfig;
import com.magsav.util.AppLogger;

/**
 * Test simple du système de logging et configuration
 */
public class LoggingTest {
    
    public static void main(String[] args) {
        AppLogger.logAppLifecycle("démarrage", "test logging");
        
        // Test des différents niveaux de log
        AppLogger.info("Test du logging INFO");
        AppLogger.warn("Test du logging WARN");
        AppLogger.debug("Test du logging DEBUG");
        
        // Test des logs spécialisés
        AppLogger.logBusiness("test opération", "param1", "param2");
        AppLogger.logSql("SELECT", "produits", "test");
        AppLogger.logUserAction("test action", "param");
        
        // Test de la configuration
        AppLogger.info("Configuration MAGSAV:");
        AppLogger.info("- Répertoire: {}", AppConfig.getMagsavDirectory());
        AppLogger.info("- Médias: {}", AppConfig.getMediaDirectory());
        AppLogger.info("- Base de données: {}", AppConfig.getDatabasePath());
        AppLogger.info("- Mode debug: {}", AppConfig.isDebugMode());
        
        // Test du timer de performance
        var timer = AppLogger.startTimer("opération test");
        try {
            Thread.sleep(100); // Simule une opération
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        timer.end();
        
        AppLogger.logAppLifecycle("arrêt", "test terminé");
    }
}