package com.magscene.magsav.desktop.theme;

import javafx.scene.Scene;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * Gestionnaire centralisé des thèmes MAGSAV-3.0
 * Permet la gestion dynamique des thèmes : clair, sombre, colorés et personnalisés
 */
public class ThemeManager {
    
    private static ThemeManager instance;
    private final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
    private final Map<String, Theme> themes = new LinkedHashMap<>();
    private String currentTheme = "dark";
    private Scene currentScene;
    
    private ThemeManager() {
        initializeDefaultThemes();
        loadCurrentThemeFromPrefs();
    }
    
    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }
    
    /**
     * Initialise les thèmes par défaut
     */
    private void initializeDefaultThemes() {
        // Thème Clair (basé sur charte graphique authentique)
        themes.put("light", new Theme(
            "light", 
            "Thème Clair", 
            "Interface claire et moderne selon la charte MAGSAV", 
            "/styles/theme-light.css"
        ));
        
        // Thème Sombre (basé sur charte graphique authentique)
        themes.put("dark", new Theme(
            "dark", 
            "Thème Sombre", 
            "Interface sombre authentique SANS BORDURES selon la charte MAGSAV", 
            "/styles/theme-dark-ultra.css"
        ));
        
        // Pour l'instant, nous gardons seulement les deux thèmes authentiques
        // D'autres thèmes pourront être ajoutés plus tard selon la charte graphique
    }
    
    /**
     * Applique un thème à la scène courante
     */
    public void applyTheme(String themeId) {
        if (currentScene == null) {
            System.err.println("Aucune scène définie pour l'application du thème");
            return;
        }
        
        Theme theme = themes.get(themeId);
        if (theme == null) {
            System.err.println("Thème introuvable: " + themeId);
            return;
        }
        
        try {
            // Supprime les anciens stylesheets
            currentScene.getStylesheets().clear();
            
            // Charge les nouveaux stylesheets
            for (String cssFile : theme.getCssFiles()) {
                String cssUrl = getClass().getResource(cssFile).toExternalForm();
                currentScene.getStylesheets().add(cssUrl);
                System.out.println("🎨 CSS chargé: " + cssFile + " -> " + cssUrl);
            }
            
            currentTheme = themeId;
            saveCurrentThemeToPrefs();
            
            System.out.println("✅ Thème appliqué: " + theme.getDisplayName());
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'application du thème: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Définit la scène courante pour l'application des thèmes
     */
    public void setScene(Scene scene) {
        this.currentScene = scene;
        // Applique le thème sauvegardé au démarrage
        applyTheme(currentTheme);
    }
    
    /**
     * Réapplique le thème actuel pour forcer l'override des CSS
     * Utile après le chargement de nouvelles vues qui ajoutent leurs propres CSS
     */
    public void reapplyCurrentTheme() {
        applyTheme(currentTheme);
    }
    
    /**
     * Obtient le thème actuellement actif
     */
    public String getCurrentTheme() {
        return currentTheme;
    }
    
    /**
     * Obtient tous les thèmes disponibles
     */
    public ObservableList<Theme> getAvailableThemes() {
        return FXCollections.observableArrayList(themes.values());
    }
    
    /**
     * Obtient un thème par son ID
     */
    public Theme getTheme(String themeId) {
        return themes.get(themeId);
    }
    
    /**
     * Ajoute un thème personnalisé
     */
    public void addCustomTheme(Theme theme) {
        themes.put(theme.getId(), theme);
        System.out.println("Thème personnalisé ajouté: " + theme.getDisplayName());
    }
    
    /**
     * Supprime un thème personnalisé
     */
    public boolean removeCustomTheme(String themeId) {
        // Ne permet pas de supprimer les thèmes par défaut
        if (Arrays.asList("light", "dark", "ocean-blue", "forest-green").contains(themeId)) {
            return false;
        }
        
        Theme removed = themes.remove(themeId);
        if (removed != null) {
            // Si le thème supprimé était actif, revenir au thème par défaut
            if (currentTheme.equals(themeId)) {
                applyTheme("light");
            }
            System.out.println("Thème supprimé: " + removed.getDisplayName());
            return true;
        }
        return false;
    }
    
    /**
     * Sauvegarde le thème actuel dans les préférences
     */
    private void saveCurrentThemeToPrefs() {
        prefs.put("currentTheme", currentTheme);
    }
    
    /**
     * Charge le thème actuel depuis les préférences
     */
    private void loadCurrentThemeFromPrefs() {
        currentTheme = prefs.get("currentTheme", "dark");
    }
    
    /**
     * Obtient la preview d'un thème (couleurs principales)
     */
    public ThemePreview getThemePreview(String themeId) {
        switch (themeId) {
            case "light":
                return new ThemePreview("#ffffff", "#f8f9fa", "#007bff", "#343a40");
            case "dark":
                return new ThemePreview("#1e3a5f", "#1a1a1a", "#4a90e2", "#ffffff");
            default:
                return new ThemePreview("#ffffff", "#f8f9fa", "#007bff", "#343a40");
        }
    }
    
    /**
     * Obtient la couleur de background principale selon le thème actuel
     */
    public String getCurrentBackgroundColor() {
        return isDarkTheme() ? "#1e3a5f" : "#f8f9fa";
    }
    
    /**
     * Obtient la couleur de background secondaire selon le thème actuel
     */
    public String getCurrentSecondaryColor() {
        return isDarkTheme() ? "#1a1a1a" : "#ffffff";
    }
    
    /**
     * Obtient la couleur des éléments UI selon le thème actuel
     */
    public String getCurrentUIColor() {
        return isDarkTheme() ? "#2c2c2c" : "#ffffff";
    }
    
    /**
     * Vérifie si le thème actuel est sombre
     */
    public boolean isDarkTheme() {
        return "dark".equals(currentTheme);
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
        return "#142240"; // Couleur de sélection unifiée pour tous les thèmes
    }
    
    /**
     * Obtient la couleur du texte de sélection selon le thème actuel
     */
    public String getSelectionTextColor() {
        return "#7DD3FC"; // Couleur de texte pour les éléments sélectionnés
    }
    
    /**
     * Obtient la couleur de bordure de sélection selon le thème actuel
     */
    public String getSelectionBorderColor() {
        return "#6B71F2"; // Couleur de bordure pour les éléments sélectionnés
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