package com.magscene.magsav.desktop.theme;

import javafx.scene.Scene;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.*;

/**
 * Gestionnaire centralisé des thèmes MAGSAV-3.0
 * 
 * @deprecated Cette classe est maintenant un wrapper vers UnifiedThemeManager
 * pour assurer la compatibilité avec le code existant.
 * Utilisez directement UnifiedThemeManager pour les nouveaux développements.
 */
@Deprecated
public class ThemeManager {
    
    private static ThemeManager instance;
    private final UnifiedThemeManager unifiedManager = UnifiedThemeManager.getInstance();
    
    private ThemeManager() {
        // Délégation vers UnifiedThemeManager
    }
    
    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }
    
    /**
     * Applique un thème à la scène courante
     * @deprecated Utilise UnifiedThemeManager
     */
    @Deprecated
    public void applyTheme(String themeId) {
        unifiedManager.applyTheme(themeId);
    }
    
    /**
     * Définit la scène courante pour l'application des thèmes
     * @deprecated Utilise UnifiedThemeManager.applyThemeToScene()
     */
    @Deprecated
    public void setScene(Scene scene) {
        unifiedManager.applyThemeToScene(scene);
    }
    
    /**
     * Réapplique le thème actuel
     * @deprecated Utilise UnifiedThemeManager.applyTheme()
     */
    @Deprecated
    public void reapplyCurrentTheme() {
        unifiedManager.applyTheme(unifiedManager.getCurrentTheme());
    }
    
    /**
     * Obtient le thème actuellement actif
     */
    public String getCurrentTheme() {
        return unifiedManager.getCurrentTheme();
    }
    
    /**
     * Obtient tous les thèmes disponibles
     */
    public ObservableList<Theme> getAvailableThemes() {
        List<Theme> themeList = new ArrayList<>();
        for (UnifiedThemeManager.Theme uTheme : unifiedManager.getAvailableThemes()) {
            // Convertit UnifiedThemeManager.Theme vers l'ancien Theme
            themeList.add(new Theme(
                uTheme.getId(),
                uTheme.getDisplayName(),
                "Thème " + uTheme.getDisplayName(),
                uTheme.getCssFile()
            ));
        }
        return FXCollections.observableArrayList(themeList);
    }
    
    /**
     * Obtient un thème par son ID
     */
    public Theme getTheme(String themeId) {
        for (UnifiedThemeManager.Theme uTheme : unifiedManager.getAvailableThemes()) {
            if (uTheme.getId().equals(themeId)) {
                return new Theme(
                    uTheme.getId(),
                    uTheme.getDisplayName(),
                    "Thème " + uTheme.getDisplayName(),
                    uTheme.getCssFile()
                );
            }
        }
        return null;
    }
    
    /**
     * @deprecated Non supporté dans la nouvelle architecture
     */
    @Deprecated
    public void addCustomTheme(Theme theme) {
        System.err.println("addCustomTheme() n'est plus supporté - utilisez UnifiedThemeManager");
    }
    
    /**
     * @deprecated Non supporté dans la nouvelle architecture
     */
    @Deprecated
    public boolean removeCustomTheme(String themeId) {
        System.err.println("removeCustomTheme() n'est plus supporté - utilisez UnifiedThemeManager");
        return false;
    }
    
    /**
     * Obtient la preview d'un thème (couleurs principales)
     */
    public ThemePreview getThemePreview(String themeId) {
        switch (themeId) {
            case "light":
                return new ThemePreview("#FFFFFF", "#F8F9FA", "#6B71F2", "#212529");
            case "dark":
                return new ThemePreview("#1e3a5f", "#1a1a1a", "#4a90e2", "#ffffff");
            case "blue":
                return new ThemePreview("#E3F2FD", "#BBDEFB", "#2196F3", "#0D47A1");
            case "green":
                return new ThemePreview("#E8F5E9", "#C8E6C9", "#4CAF50", "#1B5E20");
            default:
                return new ThemePreview("#FFFFFF", "#F8F9FA", "#6B71F2", "#212529");
        }
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
     * Vérifie si le thème actuel est sombre
     */
    public boolean isDarkTheme() {
        return "dark".equals(unifiedManager.getCurrentTheme());
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
     * Obtient la couleur de sélection selon le thème actuel
     */
    public String getSelectionColor() {
        return "#6B71F2"; // Couleur de sélection unifiée pour tous les thèmes
    }
    
    /**
     * Obtient la couleur du texte de sélection selon le thème actuel
     */
    public String getSelectionTextColor() {
        return "#6B71F2"; // Couleur de texte pour les éléments sélectionnés
    }
    
    /**
     * Obtient la couleur de bordure de sélection selon le thème actuel
     */
    public String getSelectionBorderColor() {
        return "#6B71F2"; // Couleur de bordure pour les éléments sélectionnés
    }
    
    /**
     * Applique le thème actuel à un dialogue
     * @param dialogPane Le DialogPane auquel appliquer le thème
     */
    public void applyThemeToDialog(javafx.scene.control.DialogPane dialogPane) {
        if (dialogPane != null && dialogPane.getScene() != null) {
            unifiedManager.applyThemeToScene(dialogPane.getScene());
        }
    }
    
    /**
     * Applique le thème actuel à une scène spécifique (pour les dialogues personnalisés)
     * @param scene La Scene à laquelle appliquer le thème
     */
    public void applyThemeToScene(Scene scene) {
        unifiedManager.applyThemeToScene(scene);
    }
    
    /**
     * Classe représentant un aperçu de thème
     */
    public static class ThemePreview {
        private final String backgroundColor;
        private final String secondaryColor;
        private final String accentColor;
        private final String textColor;
        
        public ThemePreview(String backgroundColor, String secondaryColor, String accentColor, String textColor) {
            this.backgroundColor = backgroundColor;
            this.secondaryColor = secondaryColor;
            this.accentColor = accentColor;
            this.textColor = textColor;
        }
        
        // Getters
        public String getBackgroundColor() { return backgroundColor; }
        public String getSecondaryColor() { return secondaryColor; }
        public String getAccentColor() { return accentColor; }
        public String getTextColor() { return textColor; }
    }
}
