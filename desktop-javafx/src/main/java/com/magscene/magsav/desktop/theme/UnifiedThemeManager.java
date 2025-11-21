package com.magscene.magsav.desktop.theme;

import javafx.scene.Scene;
import java.util.prefs.Preferences;

/**
 * Gestionnaire de thèmes unifié et simplifié pour MAGSAV 3.0
 * 
 * Architecture CSS pure avec variables CSS et classes de style unifiées
 * Thème par défaut : Light (configuration depuis les paramètres uniquement)
 * 
 * @version 3.0.0-unified-light
 */
public class UnifiedThemeManager {
    
    private static UnifiedThemeManager instance;
    private final Preferences prefs = Preferences.userNodeForPackage(UnifiedThemeManager.class);
    
    private String currentTheme = "light";
    private Scene currentScene;
    
    // Thèmes disponibles - Par défaut Light uniquement
    public enum Theme {
        LIGHT("light", "Thème Clair", "/styles/magsav-light.css");
        // D'autres thèmes peuvent être ajoutés ici selon la charte graphique
        
        private final String id;
        private final String displayName;
        private final String cssFile;
        
        Theme(String id, String displayName, String cssFile) {
            this.id = id;
            this.displayName = displayName;
            this.cssFile = cssFile;
        }
        
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public String getCssFile() { return cssFile; }
    }
    
    private UnifiedThemeManager() {
        loadSavedTheme();
    }
    
    public static UnifiedThemeManager getInstance() {
        if (instance == null) {
            instance = new UnifiedThemeManager();
        }
        return instance;
    }
    
    /**
     * Applique un thème à la scène
     */
    public void applyTheme(String themeId) {
        Theme theme = getThemeById(themeId);
        if (theme != null) {
            currentTheme = themeId;
            saveCurrentTheme();
            
            if (currentScene != null) {
                applyThemeToScene(currentScene);
            }
        }
    }
    
    /**
     * Applique le thème à une scène spécifique
     */
    public void applyThemeToScene(Scene scene) {
        if (scene == null) return;
        
        this.currentScene = scene;
        Theme theme = getThemeById(currentTheme);
        
        if (theme != null) {
            // Supprimer tous les anciens styles
            scene.getStylesheets().clear();
            
            // Ajouter le nouveau thème
            String cssPath = getClass().getResource(theme.getCssFile()).toExternalForm();
            scene.getStylesheets().add(cssPath);
            System.out.println("🎨 CSS CHARGÉ: " + theme.getCssFile() + " -> " + cssPath);
            
            // Appliquer la classe de thème à la racine
            scene.getRoot().getStyleClass().removeIf(style -> style.startsWith("theme-"));
            scene.getRoot().getStyleClass().add("theme-" + currentTheme);
            System.out.println("✅ Classe CSS appliquée: theme-" + currentTheme + " sur " + scene.getRoot().getClass().getSimpleName());
        }
    }
    
    /**
     * Obtient le thème actuel
     */
    public String getCurrentTheme() {
        return currentTheme;
    }
    
    /**
     * Obtient tous les thèmes disponibles
     */
    public Theme[] getAvailableThemes() {
        return Theme.values();
    }
    
    /**
     * Obtient un thème par son ID
     */
    private Theme getThemeById(String id) {
        for (Theme theme : Theme.values()) {
            if (theme.getId().equals(id)) {
                return theme;
            }
        }
        return Theme.LIGHT; // Thème par défaut
    }
    
    /**
     * Sauvegarde le thème actuel
     */
    private void saveCurrentTheme() {
        prefs.put("current_theme", currentTheme);
    }
    
    /**
     * Charge le thème sauvegardé
     */
    private void loadSavedTheme() {
        currentTheme = prefs.get("current_theme", "light");
    }
    
    /**
     * Applique une classe CSS unifiée à un composant
     */
    public static void applyStyle(javafx.scene.Node node, String styleClass) {
        if (node != null && styleClass != null) {
            node.getStyleClass().add(styleClass);
        }
    }
    
    /**
     * Supprime tous les styles inline et applique les classes CSS
     */
    public static void cleanInlineStyles(javafx.scene.Node node) {
        if (node != null) {
            node.setStyle("");
        }
    }
}