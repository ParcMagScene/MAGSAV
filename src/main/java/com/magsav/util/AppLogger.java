package com.magsav.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilitaire centralisé pour le logging dans l'application MAGSAV
 * 
 * Cette classe fournit des méthodes pratiques pour logger de manière cohérente
 * à travers toute l'application, avec des patterns spécifiques par type d'opération.
 */
public final class AppLogger {
    
    // Logger principal de l'application
    private static final Logger MAIN_LOGGER = LoggerFactory.getLogger("com.magsav.App");
    
    // Loggers spécialisés par couche
    private static final Logger REPO_LOGGER = LoggerFactory.getLogger("com.magsav.repo");
    private static final Logger SERVICE_LOGGER = LoggerFactory.getLogger("com.magsav.service");
    private static final Logger UI_LOGGER = LoggerFactory.getLogger("com.magsav.gui");
    private static final Logger UTIL_LOGGER = LoggerFactory.getLogger("com.magsav.util");
    
    private AppLogger() {
        // Classe utilitaire - pas d'instanciation
    }
    
    // ========== MÉTHODES GÉNÉRALES ==========
    
    /**
     * Log d'information générale
     */
    public static void info(String message, Object... args) {
        MAIN_LOGGER.info(message, args);
    }
    
    /**
     * Log d'avertissement
     */
    public static void warn(String message, Object... args) {
        MAIN_LOGGER.warn(message, args);
    }
    
    /**
     * Log d'erreur
     */
    public static void error(String message, Throwable throwable) {
        MAIN_LOGGER.error(message, throwable);
    }
    
    /**
     * Log d'erreur simple
     */
    public static void error(String message, Object... args) {
        MAIN_LOGGER.error(message, args);
    }
    
    /**
     * Log de debug (seulement en développement)
     */
    public static void debug(String message, Object... args) {
        MAIN_LOGGER.debug(message, args);
    }
    
    // ========== REPOSITORY LAYER ==========
    
    /**
     * Log d'opération SQL
     */
    public static void logSql(String operation, String table, Object... params) {
        REPO_LOGGER.info("SQL {} sur table '{}' avec paramètres: {}", operation, table, params);
    }
    
    /**
     * Log d'erreur base de données
     */
    public static void logDbError(String operation, String table, Throwable error) {
        REPO_LOGGER.error("Erreur SQL lors de {} sur table '{}': {}", operation, table, error.getMessage(), error);
    }
    
    /**
     * Log de performance DB
     */
    public static void logDbPerformance(String operation, long durationMs) {
        if (durationMs > 1000) { // Log si > 1 seconde
            REPO_LOGGER.warn("Opération DB lente: {} ({}ms)", operation, durationMs);
        } else {
            REPO_LOGGER.debug("Opération DB: {} ({}ms)", operation, durationMs);
        }
    }
    
    // ========== SERVICE LAYER ==========
    
    /**
     * Log d'opération métier
     */
    public static void logBusiness(String operation, Object... context) {
        SERVICE_LOGGER.info("Opération métier: {} - Contexte: {}", operation, context);
    }
    
    /**
     * Log de validation métier
     */
    public static void logValidation(String field, Object value, boolean isValid, String reason) {
        if (isValid) {
            SERVICE_LOGGER.debug("Validation OK pour {}: {}", field, value);
        } else {
            SERVICE_LOGGER.warn("Validation échouée pour {}: {} - Raison: {}", field, value, reason);
        }
    }
    
    /**
     * Log d'erreur métier
     */
    public static void logBusinessError(String operation, String reason, Object... context) {
        SERVICE_LOGGER.error("Erreur métier lors de '{}': {} - Contexte: {}", operation, reason, context);
    }
    
    // ========== UI LAYER ==========
    
    /**
     * Log de navigation utilisateur
     */
    public static void logNavigation(String from, String to, Object userId) {
        UI_LOGGER.info("Navigation: {} -> {} (utilisateur: {})", from, to, userId);
    }
    
    /**
     * Log d'action utilisateur
     */
    public static void logUserAction(String action, Object... params) {
        UI_LOGGER.info("Action utilisateur: {} avec paramètres: {}", action, params);
    }
    
    /**
     * Log d'erreur interface
     */
    public static void logUiError(String component, String action, Throwable error) {
        UI_LOGGER.error("Erreur UI dans '{}' lors de '{}': {}", component, action, error.getMessage(), error);
    }
    
    // ========== UTILITY METHODS ==========
    
    /**
     * Log d'opération sur fichier
     */
    public static void logFileOperation(String operation, String filePath, boolean success) {
        if (success) {
            UTIL_LOGGER.info("Opération fichier réussie: {} sur '{}'", operation, filePath);
        } else {
            UTIL_LOGGER.error("Opération fichier échouée: {} sur '{}'", operation, filePath);
        }
    }
    
    /**
     * Log de configuration
     */
    public static void logConfig(String parameter, Object value) {
        UTIL_LOGGER.info("Configuration: {} = {}", parameter, value);
    }
    
    /**
     * Log de démarrage/arrêt application
     */
    public static void logAppLifecycle(String event, Object... details) {
        MAIN_LOGGER.info("Application {}: {}", event, details);
    }
    
    // ========== PERFORMANCE MONITORING ==========
    
    /**
     * Log de performance générale
     */
    public static void logPerformance(String operation, long durationMs) {
        if (durationMs > 5000) { // > 5 secondes
            MAIN_LOGGER.error("Opération très lente: {} ({}ms)", operation, durationMs);
        } else if (durationMs > 2000) { // > 2 secondes
            MAIN_LOGGER.warn("Opération lente: {} ({}ms)", operation, durationMs);
        } else {
            MAIN_LOGGER.debug("Performance: {} ({}ms)", operation, durationMs);
        }
    }
    
    /**
     * Mesure et log automatique du temps d'exécution
     */
    public static class PerformanceTimer {
        private final String operation;
        private final long startTime;
        
        public PerformanceTimer(String operation) {
            this.operation = operation;
            this.startTime = System.currentTimeMillis();
        }
        
        public void end() {
            long duration = System.currentTimeMillis() - startTime;
            logPerformance(operation, duration);
        }
    }
    
    /**
     * Créer un timer de performance
     */
    public static PerformanceTimer startTimer(String operation) {
        return new PerformanceTimer(operation);
    }
}