package com.magsav.config;

import com.magsav.util.AppLogger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Configuration centralisée de l'application MAGSAV
 * 
 * Cette classe charge les paramètres depuis application.properties
 * et fournit des valeurs par défaut si le fichier n'existe pas.
 */
public final class AppConfig {
    
    private static final Properties properties = new Properties();
    private static boolean initialized = false;
    
    // Valeurs par défaut - utiliser le répertoire de travail courant du projet
    private static final String DEFAULT_PROJECT_DIR = System.getProperty("user.dir");
    private static final String DEFAULT_MAGSAV_DIR = DEFAULT_PROJECT_DIR;
    private static final String DEFAULT_MEDIA_DIR = DEFAULT_MAGSAV_DIR + "/medias";
    private static final String DEFAULT_DB_PATH = DEFAULT_MAGSAV_DIR + "/data/MAGSAV.db";
    private static final String DEFAULT_LOGS_DIR = DEFAULT_MAGSAV_DIR + "/logs";
    
    static {
        initialize();
    }
    
    private AppConfig() {
        // Classe utilitaire - pas d'instanciation
    }
    
    /**
     * Initialise la configuration en chargeant application.properties
     */
    private static void initialize() {
        if (initialized) return;
        
        try (InputStream input = AppConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            
            if (input != null) {
                properties.load(input);
                AppLogger.info("Configuration chargée depuis application.properties");
            } else {
                AppLogger.info("Fichier application.properties non trouvé, utilisation des valeurs par défaut");
            }
            
        } catch (IOException e) {
            AppLogger.error("Erreur lors du chargement de la configuration", e);
        }
        
        initialized = true;
        logConfiguration();
    }
    
    /**
     * Log de la configuration au démarrage
     */
    private static void logConfiguration() {
        AppLogger.logConfig("MAGSAV_DIR", getMagsavDirectory());
        AppLogger.logConfig("MEDIA_DIR", getMediaDirectory());
        AppLogger.logConfig("DB_PATH", getDatabasePath());
        AppLogger.logConfig("LOGS_DIR", getLogsDirectory());
    }
    
    // ========== GETTERS CONFIGURATION ==========
    
    /**
     * Répertoire principal MAGSAV
     */
    public static String getMagsavDirectory() {
        return getProperty("magsav.directory", DEFAULT_MAGSAV_DIR);
    }
    
    /**
     * Répertoire des médias
     */
    public static String getMediaDirectory() {
        return getProperty("magsav.media.directory", DEFAULT_MEDIA_DIR);
    }
    
    /**
     * Répertoire des photos
     */
    public static String getPhotosDirectory() {
        return getMediaDirectory() + "/photos";
    }
    
    /**
     * Répertoire des logos
     */
    public static String getLogosDirectory() {
        return getMediaDirectory() + "/logos";
    }
    
    /**
     * Répertoire des QR codes
     */
    public static String getQrCodesDirectory() {
        return getMediaDirectory() + "/qrcodes";
    }
    
    /**
     * Chemin vers la base de données
     */
    public static String getDatabasePath() {
        return getProperty("magsav.database.path", DEFAULT_DB_PATH);
    }
    
    /**
     * Répertoire des logs
     */
    public static String getLogsDirectory() {
        return getProperty("magsav.logs.directory", DEFAULT_LOGS_DIR);
    }
    
    /**
     * Pool de connexions DB - taille minimum
     */
    public static int getDbPoolMinSize() {
        return getIntProperty("magsav.database.pool.min", 2);
    }
    
    /**
     * Pool de connexions DB - taille maximum
     */
    public static int getDbPoolMaxSize() {
        return getIntProperty("magsav.database.pool.max", 10);
    }
    
    /**
     * Timeout de connexion DB en millisecondes
     */
    public static long getDbConnectionTimeout() {
        return getLongProperty("magsav.database.timeout", 30000L);
    }
    
    /**
     * Mode debug activé
     */
    public static boolean isDebugMode() {
        return getBooleanProperty("magsav.debug", false);
    }
    
    /**
     * Taille maximale des fichiers médias en MB
     */
    public static int getMaxMediaFileSizeMB() {
        return getIntProperty("magsav.media.max.size.mb", 10);
    }
    
    /**
     * Extensions de fichiers autorisées pour les images
     */
    public static String[] getAllowedImageExtensions() {
        String extensions = getProperty("magsav.media.allowed.extensions", "jpg,jpeg,png,gif,bmp");
        return extensions.split(",");
    }
    
    // ========== MÉTHODES UTILITAIRES ==========
    
    /**
     * Récupère une propriété string avec valeur par défaut
     */
    private static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Récupère une propriété int avec valeur par défaut
     */
    private static int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            AppLogger.warn("Propriété '{}' invalide, utilisation de la valeur par défaut: {}", key, defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * Récupère une propriété long avec valeur par défaut
     */
    private static long getLongProperty(String key, long defaultValue) {
        try {
            return Long.parseLong(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            AppLogger.warn("Propriété '{}' invalide, utilisation de la valeur par défaut: {}", key, defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * Récupère une propriété boolean avec valeur par défaut
     */
    private static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }
    
    /**
     * Convertit un chemin string en Path
     */
    public static Path getPath(String pathString) {
        return Paths.get(pathString);
    }
    
    /**
     * Répertoire principal MAGSAV en tant que Path
     */
    public static Path getMagsavPath() {
        return getPath(getMagsavDirectory());
    }
    
    /**
     * Répertoire des médias en tant que Path
     */
    public static Path getMediaPath() {
        return getPath(getMediaDirectory());
    }
    
    /**
     * Répertoire des photos en tant que Path
     */
    public static Path getPhotosPath() {
        return getPath(getPhotosDirectory());
    }
    
    /**
     * Répertoire des logos en tant que Path
     */
    public static Path getLogosPath() {
        return getPath(getLogosDirectory());
    }
    
    /**
     * Recharge la configuration (utile pour les tests)
     */
    public static void reload() {
        initialized = false;
        properties.clear();
        initialize();
    }
}