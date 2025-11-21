package com.magscene.magsav.desktop.config;

/**
 * Configuration globale de l'application
 */
public class ApplicationConfig {
    
    // Configuration API
    public static final String API_BASE_URL = "http://localhost:8080";
    public static final int API_TIMEOUT_SECONDS = 30;
    
    // Configuration UI
    public static final String APP_TITLE = "MAGSAV 3.0 - Gestion SAV et Parc Matériel";
    public static final String APP_VERSION = "3.0.0";
    public static final int WINDOW_MIN_WIDTH = 1200;
    public static final int WINDOW_MIN_HEIGHT = 800;
    
    // Configuration Logging
    public static final boolean DEBUG_MODE = true;
    public static final boolean ENABLE_API_LOGGING = true;
    
    // Configuration Import
    public static final String[] SUPPORTED_IMPORT_FORMATS = {".xlsx", ".xls"};
    public static final int MAX_IMPORT_FILE_SIZE_MB = 50;
    
    // Configuration Cache
    public static final int VIEW_CACHE_SIZE = 10;
    public static final long VIEW_CACHE_TTL_MINUTES = 30;
    
    // Configuration Backend Health Check
    public static final int HEALTH_CHECK_INTERVAL_SECONDS = 60;
    public static final int HEALTH_CHECK_TIMEOUT_SECONDS = 5;
    
    private ApplicationConfig() {
        // Utility class - pas d'instanciation
    }
    
    /**
     * Vérifie si l'application est en mode debug
     */
    public static boolean isDebugMode() {
        return DEBUG_MODE || Boolean.getBoolean("magsav.debug");
    }
    
    /**
     * Retourne l'URL complète pour un endpoint
     */
    public static String getApiUrl(String endpoint) {
        return API_BASE_URL + (endpoint.startsWith("/") ? endpoint : "/" + endpoint);
    }
    
    /**
     * Vérifie si un format de fichier est supporté pour l'import
     */
    public static boolean isSupportedImportFormat(String filename) {
        if (filename == null) return false;
        
        String lowerFilename = filename.toLowerCase();
        for (String format : SUPPORTED_IMPORT_FORMATS) {
            if (lowerFilename.endsWith(format)) {
                return true;
            }
        }
        return false;
    }
}