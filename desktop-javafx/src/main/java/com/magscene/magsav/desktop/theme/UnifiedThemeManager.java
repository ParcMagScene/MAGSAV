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
            
            // 1. Charger les variables CSS centralisées
            loadStylesheet(scene, "/styles/_variables.css");
            
            // 2. Ajouter le thème principal
            String cssPath = getClass().getResource(theme.getCssFile()).toExternalForm();
            scene.getStylesheets().add(cssPath);
            System.out.println("🎨 CSS CHARGÉ: " + theme.getCssFile() + " -> " + cssPath);
            
            // 3. Charger les styles de détail unifiés
            loadStylesheet(scene, "/styles/detail-unified.css");
            
            // 4. Charger les styles d'entités (entity-details.css)
            loadStylesheet(scene, "/styles/entity-details.css");
            
            // Appliquer la classe de thème à la racine
            scene.getRoot().getStyleClass().removeIf(style -> style.startsWith("theme-"));
            scene.getRoot().getStyleClass().add("theme-" + currentTheme);
            System.out.println("✅ Classe CSS appliquée: theme-" + currentTheme + " sur " + scene.getRoot().getClass().getSimpleName());
        }
    }
    
    /**
     * Charge une feuille de style si elle existe
     */
    private void loadStylesheet(Scene scene, String cssFile) {
        try {
            var resource = getClass().getResource(cssFile);
            if (resource != null) {
                scene.getStylesheets().add(resource.toExternalForm());
                System.out.println("📄 CSS supplémentaire: " + cssFile);
            }
        } catch (Exception e) {
            System.err.println("⚠️ CSS non trouvé: " + cssFile);
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
    
    // ========== Méthodes de compatibilité pour migration depuis ThemeManager ==========
    
    /**
     * Vérifie si le thème actuel est sombre
     */
    public boolean isDarkTheme() {
        return "dark".equals(currentTheme);
    }
    
    /**
     * Obtient la couleur de background principale selon le thème actuel
     */
    public String getCurrentBackgroundColor() {
        return isDarkTheme() ? "#1e3a5f" : "#FFFFFF";
    }
    
    /**
     * Obtient la couleur de background secondaire selon le thème actuel
     */
    public String getCurrentSecondaryColor() {
        return isDarkTheme() ? "#1a1a1a" : "#F8F9FA";
    }
    
    /**
     * Obtient la couleur des éléments UI selon le thème actuel
     */
    public String getCurrentUIColor() {
        return isDarkTheme() ? "#1e3a5f" : "#FFFFFF";
    }
    
    /**
     * Obtient la couleur de sélection selon le thème actuel
     */
    public String getSelectionColor() {
        return "#6B71F2";
    }
    
    /**
     * Obtient la couleur du texte de sélection selon le thème actuel
     */
    public String getSelectionTextColor() {
        return "#6B71F2";
    }
    
    /**
     * Obtient la couleur de bordure de sélection selon le thème actuel
     */
    public String getSelectionBorderColor() {
        return "#6B71F2";
    }
    
    /**
     * Obtient la couleur de statut "Succès" selon le thème actuel
     */
    public String getSuccessColor() {
        return isDarkTheme() ? "#2d5a2d" : "#d5f4e6";
    }
    
    /**
     * Obtient la couleur de statut "Avertissement" selon le thème actuel
     */
    public String getWarningColor() {
        return isDarkTheme() ? "#5a4d2d" : "#fff3cd";
    }
    
    /**
     * Obtient la couleur de statut "Erreur" selon le thème actuel
     */
    public String getErrorColor() {
        return isDarkTheme() ? "#5a2d2d" : "#f8d7da";
    }
    
    /**
     * Obtient la couleur de statut "Info" selon le thème actuel
     */
    public String getInfoColor() {
        return isDarkTheme() ? "#2d3e5a" : "#e3f2fd";
    }
    
    /**
     * Applique le thème actuel à un dialogue
     */
    public void applyThemeToDialog(javafx.scene.control.DialogPane dialogPane) {
        if (dialogPane == null) return;
        
        // Appliquer directement le CSS au DialogPane (même si pas encore dans une Scene)
        Theme theme = getThemeById(currentTheme);
        if (theme != null) {
            // Supprimer les anciens styles
            dialogPane.getStylesheets().clear();
            
            // Ajouter le CSS du thème
            String cssPath = getClass().getResource(theme.getCssFile()).toExternalForm();
            dialogPane.getStylesheets().add(cssPath);
            System.out.println("🎨 CSS CHARGÉ: " + theme.getCssFile() + " -> " + cssPath);
            
            // Appliquer la classe de thème
            dialogPane.getStyleClass().removeIf(style -> style.startsWith("theme-"));
            dialogPane.getStyleClass().add("theme-" + currentTheme);
            System.out.println("✅ Classe CSS appliquée: theme-" + currentTheme + " sur DialogPane");
        }
        
        // Si la Scene existe aussi, l'appliquer là
        if (dialogPane.getScene() != null) {
            applyThemeToScene(dialogPane.getScene());
        }
    }
}