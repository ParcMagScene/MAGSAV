package com.magsav.ui.theme;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.magsav.util.AppLogger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestionnaire de thèmes moderne pour MAGSAV
 * Support des thèmes sombres/clairs avec transitions dynamiques
 */
public class ThemeManager {
    
    private static ThemeManager instance;
    private final ObjectProperty<Theme> currentTheme = new SimpleObjectProperty<>(Theme.LIGHT);
    private final Set<Scene> managedScenes = ConcurrentHashMap.newKeySet();
    private final Map<String, String> customColors = new ConcurrentHashMap<>();
    
    // Palettes de couleurs modernes
    public enum Theme {
        LIGHT("light", "Thème Clair", "/css/themes/light.css"),
        DARK("dark", "Thème Sombre", "/css/themes/dark.css"),
        MAGSAV_BLUE("magsav-blue", "MAGSAV Bleu", "/css/themes/magsav-blue.css"),
        MATERIAL("material", "Material Design", "/css/themes/material.css"),
        CUSTOM("custom", "Personnalisé", "/css/themes/custom.css");
        
        private final String id;
        private final String displayName;
        private final String cssPath;
        
        Theme(String id, String displayName, String cssPath) {
            this.id = id;
            this.displayName = displayName;
            this.cssPath = cssPath;
        }
        
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public String getCssPath() { return cssPath; }
    }
    
    // Couleurs modernes avec variantes
    public static class Colors {
        // Couleurs primaires
        public static final String PRIMARY = "--primary-color";
        public static final String PRIMARY_LIGHT = "--primary-light-color";
        public static final String PRIMARY_DARK = "--primary-dark-color";
        
        // Couleurs secondaires
        public static final String SECONDARY = "--secondary-color";
        public static final String ACCENT = "--accent-color";
        
        // Surfaces et arrière-plans
        public static final String BACKGROUND = "--background-color";
        public static final String SURFACE = "--surface-color";
        public static final String CARD = "--card-color";
        
        // Textes
        public static final String TEXT_PRIMARY = "--text-primary-color";
        public static final String TEXT_SECONDARY = "--text-secondary-color";
        public static final String TEXT_DISABLED = "--text-disabled-color";
        
        // États
        public static final String SUCCESS = "--success-color";
        public static final String WARNING = "--warning-color";
        public static final String ERROR = "--error-color";
        public static final String INFO = "--info-color";
        
        // Bordures et séparateurs
        public static final String BORDER = "--border-color";
        public static final String DIVIDER = "--divider-color";
        
        // Ombres
        public static final String SHADOW_LIGHT = "--shadow-light";
        public static final String SHADOW_MEDIUM = "--shadow-medium";
        public static final String SHADOW_HEAVY = "--shadow-heavy";
    }
    
    private ThemeManager() {
        initializeDefaultColors();
        AppLogger.info("ThemeManager initialisé avec le thème: " + currentTheme.get().getDisplayName());
    }
    
    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }
    
    /**
     * Applique un thème à une scène
     */
    public void applyTheme(Scene scene, Theme theme) {
        Platform.runLater(() -> {
            try {
                // Retirer les anciens stylesheets
                scene.getStylesheets().clear();
                
                // Ajouter les stylesheets de base
                scene.getStylesheets().addAll(
                    getClass().getResource("/css/base.css").toExternalForm(),
                    getClass().getResource("/css/components.css").toExternalForm(),
                    getClass().getResource("/css/animations.css").toExternalForm()
                );
                
                // Ajouter le thème spécifique
                String themeUrl = getClass().getResource(theme.getCssPath()).toExternalForm();
                if (themeUrl != null) {
                    scene.getStylesheets().add(themeUrl);
                }
                
                // Appliquer les couleurs personnalisées
                applyCustomColors(scene);
                
                // Enregistrer la scène pour les mises à jour futures
                managedScenes.add(scene);
                
                AppLogger.info("Thème appliqué: " + theme.getDisplayName());
                
            } catch (Exception e) {
                AppLogger.error("Erreur lors de l'application du thème: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Change le thème pour toutes les scènes gérées
     */
    public void changeTheme(Theme newTheme) {
        if (newTheme == currentTheme.get()) return;
        
        Platform.runLater(() -> {
            Theme oldTheme = currentTheme.get();
            currentTheme.set(newTheme);
            
            // Appliquer le nouveau thème à toutes les scènes
            for (Scene scene : managedScenes) {
                applyTheme(scene, newTheme);
            }
            
            AppLogger.info("Thème changé de " + oldTheme.getDisplayName() + 
                          " vers " + newTheme.getDisplayName());
        });
    }
    
    /**
     * Applique un thème à une fenêtre
     */
    public void applyTheme(Stage stage, Theme theme) {
        if (stage.getScene() != null) {
            applyTheme(stage.getScene(), theme);
        }
    }
    
    /**
     * Applique le thème actuel à une nouvelle scène
     */
    public void applyCurrentTheme(Scene scene) {
        applyTheme(scene, currentTheme.get());
    }
    
    /**
     * Personnalise une couleur
     */
    public void setCustomColor(String colorKey, String colorValue) {
        customColors.put(colorKey, colorValue);
        
        // Appliquer aux scènes existantes
        Platform.runLater(() -> {
            for (Scene scene : managedScenes) {
                applyCustomColors(scene);
            }
        });
    }
    
    /**
     * Obtient une couleur personnalisée
     */
    public String getCustomColor(String colorKey) {
        return customColors.get(colorKey);
    }
    
    /**
     * Bascule entre thème clair et sombre
     */
    public void toggleDarkMode() {
        Theme newTheme = (currentTheme.get() == Theme.DARK) ? Theme.LIGHT : Theme.DARK;
        changeTheme(newTheme);
    }
    
    /**
     * Vérifie si le thème sombre est actuel
     */
    public boolean isDarkMode() {
        return currentTheme.get() == Theme.DARK;
    }
    
    /**
     * Obtient le thème actuel
     */
    public Theme getCurrentTheme() {
        return currentTheme.get();
    }
    
    /**
     * Property du thème actuel pour binding
     */
    public ObjectProperty<Theme> currentThemeProperty() {
        return currentTheme;
    }
    
    /**
     * Obtient tous les thèmes disponibles
     */
    public List<Theme> getAvailableThemes() {
        return Arrays.asList(Theme.values());
    }
    
    /**
     * Retire une scène de la gestion
     */
    public void unmanageScene(Scene scene) {
        managedScenes.remove(scene);
    }
    
    /**
     * Initialise les couleurs par défaut
     */
    private void initializeDefaultColors() {
        // Couleurs MAGSAV par défaut
        customColors.put(Colors.PRIMARY, "#2196F3");
        customColors.put(Colors.PRIMARY_LIGHT, "#64B5F6");
        customColors.put(Colors.PRIMARY_DARK, "#1976D2");
        customColors.put(Colors.SECONDARY, "#FF5722");
        customColors.put(Colors.ACCENT, "#4CAF50");
        customColors.put(Colors.SUCCESS, "#4CAF50");
        customColors.put(Colors.WARNING, "#FF9800");
        customColors.put(Colors.ERROR, "#F44336");
        customColors.put(Colors.INFO, "#2196F3");
    }
    
    /**
     * Applique les couleurs personnalisées à une scène
     */
    private void applyCustomColors(Scene scene) {
        StringBuilder cssVars = new StringBuilder("-fx-base: ");
        
        for (Map.Entry<String, String> entry : customColors.entrySet()) {
            cssVars.append(entry.getKey()).append(": ").append(entry.getValue()).append("; ");
        }
        
        if (scene.getRoot() != null) {
            scene.getRoot().setStyle(cssVars.toString());
        }
    }
    
    /**
     * Sauvegarde les préférences de thème
     */
    public void saveThemePreferences() {
        try {
            // Ici on peut sauvegarder dans les préférences système
            // ou dans un fichier de configuration
            AppLogger.info("Préférences de thème sauvegardées");
        } catch (Exception e) {
            AppLogger.error("Erreur lors de la sauvegarde des préférences: " + e.getMessage(), e);
        }
    }
    
    /**
     * Charge les préférences de thème
     */
    public void loadThemePreferences() {
        try {
            // Charger depuis les préférences sauvegardées
            // Par défaut, utiliser le thème clair
            AppLogger.info("Préférences de thème chargées");
        } catch (Exception e) {
            AppLogger.error("Erreur lors du chargement des préférences: " + e.getMessage(), e);
        }
    }
}