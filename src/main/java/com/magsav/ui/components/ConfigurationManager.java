package com.magsav.ui.components;

import com.magsav.service.DataCacheService;
import com.magsav.util.AppLogger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Gestionnaire de configuration de l'application MAGSAV
 * Gère les préférences utilisateur et les paramètres système
 */
public final class ConfigurationManager {
    
    private static final String CONFIG_FILE = "magsav.properties";
    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.magsav";
    private static final Path CONFIG_PATH = Paths.get(CONFIG_DIR, CONFIG_FILE);
    
    private static final Properties config = new Properties();
    private static boolean loaded = false;
    
    // Propriétés JavaFX pour binding
    private static final BooleanProperty debugMode = new SimpleBooleanProperty(false);
    private static final BooleanProperty cacheEnabled = new SimpleBooleanProperty(true);
    private static final BooleanProperty performanceMonitoring = new SimpleBooleanProperty(true);
    private static final BooleanProperty autoSave = new SimpleBooleanProperty(true);
    private static final StringProperty databasePath = new SimpleStringProperty("");
    private static final StringProperty mediaDirectory = new SimpleStringProperty("");
    private static final StringProperty logLevel = new SimpleStringProperty("INFO");
    
    private ConfigurationManager() {}
    
    /**
     * Charge la configuration depuis le fichier
     */
    public static void loadConfiguration() {
        if (loaded) return;
        
        try {
            // Créer le répertoire si nécessaire
            Files.createDirectories(Paths.get(CONFIG_DIR));
            
            // Charger le fichier s'il existe
            if (Files.exists(CONFIG_PATH)) {
                try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
                    config.load(input);
                    AppLogger.info("config", "Configuration chargée depuis " + CONFIG_PATH);
                }
            } else {
                // Créer configuration par défaut
                setDefaultConfiguration();
                saveConfiguration();
                AppLogger.info("config", "Configuration par défaut créée");
            }
            
            // Synchroniser avec les propriétés JavaFX
            syncFromProperties();
            loaded = true;
            
        } catch (IOException e) {
            AppLogger.error("config", "Erreur lors du chargement de la configuration", e);
            setDefaultConfiguration();
            syncFromProperties();
            loaded = true;
        }
    }
    
    /**
     * Sauvegarde la configuration dans le fichier
     */
    public static void saveConfiguration() {
        try {
            // Synchroniser les propriétés JavaFX vers Properties
            syncToProperties();
            
            Files.createDirectories(Paths.get(CONFIG_DIR));
            try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
                config.store(output, "Configuration MAGSAV - " + java.time.LocalDateTime.now());
                AppLogger.info("config", "Configuration sauvegardée dans " + CONFIG_PATH);
            }
        } catch (IOException e) {
            AppLogger.error("config", "Erreur lors de la sauvegarde de la configuration", e);
        }
    }
    
    /**
     * Définit la configuration par défaut
     */
    private static void setDefaultConfiguration() {
        config.setProperty("debug.mode", "false");
        config.setProperty("cache.enabled", "true");
        config.setProperty("cache.ttl.minutes", "5");
        config.setProperty("performance.monitoring", "true");
        config.setProperty("auto.save", "true");
        config.setProperty("database.path", System.getProperty("user.home") + "/MAGSAV/MAGSAV.db");
        config.setProperty("media.directory", System.getProperty("user.home") + "/MAGSAV/medias");
        config.setProperty("log.level", "INFO");
        config.setProperty("window.maximized", "false");
        config.setProperty("pagination.size", "20");
        config.setProperty("ui.theme", "default");
    }
    
    /**
     * Synchronise depuis Properties vers JavaFX Properties
     */
    private static void syncFromProperties() {
        debugMode.set(getBooleanProperty("debug.mode", false));
        cacheEnabled.set(getBooleanProperty("cache.enabled", true));
        performanceMonitoring.set(getBooleanProperty("performance.monitoring", true));
        autoSave.set(getBooleanProperty("auto.save", true));
        databasePath.set(getStringProperty("database.path", System.getProperty("user.home") + "/MAGSAV/MAGSAV.db"));
        mediaDirectory.set(getStringProperty("media.directory", System.getProperty("user.home") + "/MAGSAV/medias"));
        logLevel.set(getStringProperty("log.level", "INFO"));
    }
    
    /**
     * Synchronise depuis JavaFX Properties vers Properties
     */
    private static void syncToProperties() {
        config.setProperty("debug.mode", String.valueOf(debugMode.get()));
        config.setProperty("cache.enabled", String.valueOf(cacheEnabled.get()));
        config.setProperty("performance.monitoring", String.valueOf(performanceMonitoring.get()));
        config.setProperty("auto.save", String.valueOf(autoSave.get()));
        config.setProperty("database.path", databasePath.get());
        config.setProperty("media.directory", mediaDirectory.get());
        config.setProperty("log.level", logLevel.get());
    }
    
    /**
     * Applique la configuration aux services
     */
    public static void applyConfiguration() {
        // Mode debug pour ErrorManager
        ErrorManager.setDebugMode(debugMode.get());
        
        // Configuration du cache
        if (!cacheEnabled.get()) {
            DataCacheService.invalidateAllCache();
        }
        
        AppLogger.info("config", "Configuration appliquée aux services");
    }
    
    // Accesseurs pour les propriétés JavaFX (pour binding dans l'UI)
    public static BooleanProperty debugModeProperty() { return debugMode; }
    public static BooleanProperty cacheEnabledProperty() { return cacheEnabled; }
    public static BooleanProperty performanceMonitoringProperty() { return performanceMonitoring; }
    public static BooleanProperty autoSaveProperty() { return autoSave; }
    public static StringProperty databasePathProperty() { return databasePath; }
    public static StringProperty mediaDirectoryProperty() { return mediaDirectory; }
    public static StringProperty logLevelProperty() { return logLevel; }
    
    // Accesseurs simples
    public static boolean isDebugMode() { return debugMode.get(); }
    public static boolean isCacheEnabled() { return cacheEnabled.get(); }
    public static boolean isPerformanceMonitoring() { return performanceMonitoring.get(); }
    public static boolean isAutoSave() { return autoSave.get(); }
    public static String getDatabasePath() { return databasePath.get(); }
    public static String getMediaDirectory() { return mediaDirectory.get(); }
    public static String getLogLevel() { return logLevel.get(); }
    
    // Mutateurs
    public static void setDebugMode(boolean debug) { 
        debugMode.set(debug);
        if (autoSave.get()) saveConfiguration();
    }
    
    public static void setCacheEnabled(boolean enabled) { 
        cacheEnabled.set(enabled);
        if (autoSave.get()) saveConfiguration();
    }
    
    public static void setPerformanceMonitoring(boolean enabled) { 
        performanceMonitoring.set(enabled);
        if (autoSave.get()) saveConfiguration();
    }
    
    public static void setDatabasePath(String path) { 
        databasePath.set(path);
        if (autoSave.get()) saveConfiguration();
    }
    
    public static void setMediaDirectory(String directory) { 
        mediaDirectory.set(directory);
        if (autoSave.get()) saveConfiguration();
    }
    
    public static void setLogLevel(String level) { 
        logLevel.set(level);
        if (autoSave.get()) saveConfiguration();
    }
    
    /**
     * Méthodes utilitaires pour les properties
     */
    public static String getStringProperty(String key, String defaultValue) {
        return config.getProperty(key, defaultValue);
    }
    
    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = config.getProperty(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }
    
    public static int getIntProperty(String key, int defaultValue) {
        String value = config.getProperty(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    public static void setProperty(String key, String value) {
        config.setProperty(key, value);
        if (autoSave.get()) saveConfiguration();
    }
    
    public static void setProperty(String key, boolean value) {
        setProperty(key, String.valueOf(value));
    }
    
    public static void setProperty(String key, int value) {
        setProperty(key, String.valueOf(value));
    }
    
    /**
     * Remet la configuration par défaut
     */
    public static void resetToDefaults() {
        config.clear();
        setDefaultConfiguration();
        syncFromProperties();
        saveConfiguration();
        applyConfiguration();
        AppLogger.info("config", "Configuration remise aux valeurs par défaut");
    }
    
    /**
     * Obtient un rapport de configuration
     */
    public static String getConfigurationReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Configuration MAGSAV ===\n");
        sb.append("Fichier de configuration : ").append(CONFIG_PATH).append("\n\n");
        sb.append("Mode debug : ").append(isDebugMode()).append("\n");
        sb.append("Cache activé : ").append(isCacheEnabled()).append("\n");
        sb.append("Monitoring performances : ").append(isPerformanceMonitoring()).append("\n");
        sb.append("Sauvegarde automatique : ").append(isAutoSave()).append("\n");
        sb.append("Chemin base de données : ").append(getDatabasePath()).append("\n");
        sb.append("Répertoire médias : ").append(getMediaDirectory()).append("\n");
        sb.append("Niveau de log : ").append(getLogLevel()).append("\n");
        
        // Paramètres additionnels
        sb.append("\nParamètres additionnels :\n");
        config.stringPropertyNames().stream()
            .filter(key -> !key.startsWith("debug.") && !key.startsWith("cache.") && 
                          !key.startsWith("performance.") && !key.startsWith("auto.") &&
                          !key.equals("database.path") && !key.equals("media.directory") &&
                          !key.equals("log.level"))
            .sorted()
            .forEach(key -> sb.append("  ").append(key).append(" = ").append(config.getProperty(key)).append("\n"));
        
        return sb.toString();
    }
    
    /**
     * Vérifie la validité de la configuration
     */
    public static boolean validateConfiguration() {
        boolean valid = true;
        
        // Vérifier le chemin de la base de données
        String dbPath = getDatabasePath();
        if (dbPath.isEmpty()) {
            AppLogger.warn("config", "Chemin de base de données non configuré");
            valid = false;
        }
        
        // Vérifier le répertoire des médias
        String mediaDir = getMediaDirectory();
        if (!mediaDir.isEmpty()) {
            try {
                Path mediaPath = Paths.get(mediaDir);
                if (!Files.exists(mediaPath)) {
                    Files.createDirectories(mediaPath);
                    AppLogger.info("config", "Répertoire médias créé : " + mediaDir);
                }
            } catch (IOException e) {
                AppLogger.error("config", "Impossible de créer le répertoire médias : " + mediaDir, e);
                valid = false;
            }
        }
        
        return valid;
    }
    
    /**
     * Initialise la configuration au démarrage de l'application
     */
    public static void initialize() {
        loadConfiguration();
        validateConfiguration();
        applyConfiguration();
        AppLogger.info("config", "Gestionnaire de configuration initialisé");
    }
}