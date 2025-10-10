package com.magsav.gui;

import javafx.scene.Scene;
import javafx.application.Platform;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

public class ThemeManager {
    
    private static ThemeManager instance;
    private Scene currentScene;
    private Preferences prefs;
    
    // Chemins des fichiers CSS
    private static final String CSS_DIR = "src/main/resources/css/";
    private static final String CUSTOM_CSS_FILE = CSS_DIR + "custom-theme.css";
    private static final String DEFAULT_CSS_FILE = CSS_DIR + "simple-dark.css";
    
    private ThemeManager() {
        prefs = Preferences.userNodeForPackage(ThemeManager.class);
    }
    
    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }
    
    public void setScene(Scene scene) {
        this.currentScene = scene;
    }
    
    public void applyCustomTheme(AppearanceController.CustomTheme customTheme) {
        if (customTheme == null) {
            return;
        }
        
        try {
            // Générer le CSS personnalisé
            String customCSS = generateCustomCSS(customTheme);
            
            // Écrire le CSS dans un fichier temporaire
            writeCustomCSS(customCSS);
            
            // Appliquer le thème à la scène courante
            Platform.runLater(() -> {
                if (currentScene != null) {
                    applyCSS(CUSTOM_CSS_FILE);
                }
            });
            
        } catch (IOException e) {
            System.err.println("Erreur lors de l'application du thème personnalisé: " + e.getMessage());
        }
    }
    
    public void applyDefaultTheme() {
        Platform.runLater(() -> {
            if (currentScene != null) {
                applyCSS(DEFAULT_CSS_FILE);
            }
        });
    }
    
    private String generateCustomCSS(AppearanceController.CustomTheme theme) {
        return String.format("""
            /* Thème personnalisé généré dynamiquement */
            
            .root {
                -fx-base: %s;
                -fx-background: %s;
                -fx-control-inner-background: %s;
                -fx-font-family: "Segoe UI", "Arial", sans-serif;
            }
            
            /* Sidebar */
            .sidebar {
                -fx-background-color: %s;
                -fx-border-color: derive(%s, 20%%);
                -fx-border-width: 0 1 0 0;
                -fx-padding: 0;
            }
            
            /* Navigation items */
            .nav-item {
                -fx-background-color: transparent;
                -fx-padding: 12 20;
                -fx-cursor: hand;
                -fx-border-width: 0;
                -fx-background-radius: 0;
            }
            
            .nav-item:hover {
                -fx-background-color: derive(%s, 15%%);
            }
            
            .nav-item.active {
                -fx-background-color: %s;
                -fx-border-color: %s;
                -fx-border-width: 0 0 0 3;
            }
            
            .nav-icon {
                -fx-text-fill: %s;
                -fx-font-size: 16px;
                -fx-min-width: 20px;
            }
            
            .nav-label {
                -fx-text-fill: %s;
                -fx-font-size: 13px;
                -fx-font-weight: normal;
            }
            
            .nav-item.active .nav-icon,
            .nav-item.active .nav-label {
                -fx-text-fill: %s;
                -fx-font-weight: bold;
            }
            
            /* TabPane central */
            .tab-pane {
                -fx-background-color: %s;
                -fx-border-color: derive(%s, 20%%);
            }
            
            .tab-pane .tab-header-area {
                -fx-background-color: %s;
                -fx-border-color: derive(%s, 20%%);
                -fx-border-width: 0 0 1 0;
            }
            
            .tab-pane .tab {
                -fx-background-color: derive(%s, -10%%);
                -fx-text-fill: %s;
                -fx-border-color: derive(%s, 20%%);
                -fx-border-width: 0 1 0 0;
                -fx-padding: 8 16;
            }
            
            .tab-pane .tab:selected {
                -fx-background-color: %s;
                -fx-text-fill: %s;
            }
            
            .tab-pane .tab:hover {
                -fx-background-color: derive(%s, 10%%);
            }
            
            .tab-pane .tab-content-area {
                -fx-background-color: %s;
                -fx-padding: 0;
            }
            
            /* Boutons et contrôles */
            .button {
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-border-color: derive(%s, 20%%);
                -fx-border-width: 1;
                -fx-background-radius: 4;
                -fx-border-radius: 4;
                -fx-padding: 8 16;
            }
            
            .button:hover {
                -fx-background-color: derive(%s, 10%%);
            }
            
            .button:pressed {
                -fx-background-color: derive(%s, -10%%);
            }
            
            /* Labels et texte */
            .label {
                -fx-text-fill: %s;
            }
            
            /* Configuration spécifique pour l'onglet Apparence */
            .config-content {
                -fx-background-color: %s;
                -fx-padding: 20;
            }
            
            .config-section {
                -fx-background-color: derive(%s, 5%%);
                -fx-padding: 15;
                -fx-background-radius: 8;
                -fx-border-color: derive(%s, 20%%);
                -fx-border-width: 1;
                -fx-border-radius: 8;
            }
            
            .section-title {
                -fx-font-size: 18px;
                -fx-font-weight: bold;
                -fx-text-fill: %s;
            }
            
            .section-header {
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                -fx-text-fill: %s;
            }
            
            .color-value {
                -fx-font-family: "Consolas", "Monaco", monospace;
                -fx-text-fill: %s;
                -fx-font-size: 12px;
            }
            
            .theme-preview {
                -fx-background-color: %s;
                -fx-border-color: derive(%s, 20%%);
                -fx-border-width: 2;
                -fx-border-radius: 8;
                -fx-background-radius: 8;
                -fx-padding: 15;
            }
            
            .preview-sidebar {
                -fx-background-color: %s;
                -fx-padding: 10;
                -fx-background-radius: 4;
            }
            
            .preview-content {
                -fx-background-color: %s;
                -fx-padding: 10;
                -fx-background-radius: 4;
            }
            
            .preview-icon {
                -fx-text-fill: %s;
                -fx-font-size: 14px;
            }
            
            .preview-text {
                -fx-text-fill: %s;
                -fx-font-size: 12px;
            }
            
            .preview-title {
                -fx-text-fill: %s;
                -fx-font-size: 13px;
                -fx-font-weight: bold;
            }
            
            /* Styles pour les boutons d'action */
            .primary-button {
                -fx-background-color: %s;
                -fx-text-fill: white;
                -fx-font-weight: bold;
            }
            
            .primary-button:hover {
                -fx-background-color: derive(%s, 10%%);
            }
            
            .secondary-button {
                -fx-background-color: derive(%s, 10%%);
                -fx-text-fill: %s;
                -fx-border-color: %s;
            }
            
            .danger-button {
                -fx-background-color: #d32f2f;
                -fx-text-fill: white;
            }
            
            .danger-button:hover {
                -fx-background-color: #b71c1c;
            }
            
            /* Liste des presets */
            .presets-list {
                -fx-background-color: %s;
                -fx-border-color: derive(%s, 20%%);
                -fx-border-width: 1;
                -fx-border-radius: 4;
            }
            
            .presets-list .list-cell {
                -fx-background-color: transparent;
                -fx-text-fill: %s;
                -fx-padding: 8;
            }
            
            .presets-list .list-cell:selected {
                -fx-background-color: %s;
                -fx-text-fill: white;
            }
            
            .presets-list .list-cell:hover {
                -fx-background-color: derive(%s, 10%%);
            }
            """,
            // Variables de couleur (par ordre d'apparition)
            theme.getBackgroundColor(), theme.getBackgroundColor(), theme.getTabColor(), // .root
            theme.getSidebarColor(), theme.getSidebarColor(), // .sidebar
            theme.getSidebarColor(), // .nav-item:hover
            theme.getAccentColor(), theme.getAccentColor(), // .nav-item.active
            theme.getTextColor(), theme.getTextColor(), // .nav-icon, .nav-label
            theme.getTextColor(), // .nav-item.active
            theme.getBackgroundColor(), theme.getBackgroundColor(), // .tab-pane
            theme.getTabColor(), theme.getTabColor(), // .tab-header-area
            theme.getTabColor(), theme.getTextColor(), theme.getTabColor(), // .tab
            theme.getTabColor(), theme.getTextColor(), // .tab:selected
            theme.getTabColor(), // .tab:hover
            theme.getBackgroundColor(), // .tab-content-area
            theme.getAccentColor(), theme.getTextColor(), theme.getAccentColor(), // .button
            theme.getAccentColor(), // .button:hover
            theme.getAccentColor(), // .button:pressed
            theme.getTextColor(), // .label
            theme.getBackgroundColor(), // .config-content
            theme.getBackgroundColor(), theme.getBackgroundColor(), // .config-section
            theme.getTextColor(), // .section-title
            theme.getAccentColor(), // .section-header
            theme.getAccentColor(), // .color-value
            theme.getBackgroundColor(), theme.getBackgroundColor(), // .theme-preview
            theme.getSidebarColor(), // .preview-sidebar
            theme.getTabColor(), // .preview-content
            theme.getTextColor(), theme.getTextColor(), theme.getTextColor(), // .preview-*
            theme.getAccentColor(), // .primary-button
            theme.getAccentColor(), // .primary-button:hover
            theme.getBackgroundColor(), theme.getTextColor(), theme.getAccentColor(), // .secondary-button
            theme.getBackgroundColor(), theme.getBackgroundColor(), // .presets-list
            theme.getTextColor(), // .list-cell
            theme.getAccentColor(), // .list-cell:selected
            theme.getBackgroundColor() // .list-cell:hover
        );
    }
    
    private void writeCustomCSS(String cssContent) throws IOException {
        // Créer le répertoire CSS s'il n'existe pas
        Path cssDir = Paths.get(CSS_DIR);
        if (!Files.exists(cssDir)) {
            Files.createDirectories(cssDir);
        }
        
        // Écrire le contenu CSS dans le fichier
        try (FileWriter writer = new FileWriter(CUSTOM_CSS_FILE)) {
            writer.write(cssContent);
        }
    }
    
    private void applyCSS(String cssFile) {
        if (currentScene != null) {
            // Supprimer les anciens stylesheets
            currentScene.getStylesheets().clear();
            
            // Ajouter le nouveau stylesheet
            String cssPath = new File(cssFile).toURI().toString();
            currentScene.getStylesheets().add(cssPath);
        }
    }
    
    public void loadSavedTheme() {
        // Charger le thème sauvegardé depuis les préférences
        String themeType = prefs.get("theme_type", "default");
        
        if ("custom".equals(themeType)) {
            // Charger le thème personnalisé sauvegardé
            String sidebarColor = prefs.get("sidebar_color", "#1e3a5f");
            String backgroundColor = prefs.get("background_color", "#000000");
            String tabColor = prefs.get("tab_color", "#1a1a1a");
            String accentColor = prefs.get("accent_color", "#4a90e2");
            String textColor = prefs.get("text_color", "#ffffff");
            
            AppearanceController.CustomTheme customTheme = new AppearanceController.CustomTheme(
                sidebarColor, backgroundColor, tabColor, accentColor, textColor
            );
            
            applyCustomTheme(customTheme);
        } else {
            // Appliquer le thème par défaut
            applyDefaultTheme();
        }
    }
    
    public void saveThemeType(String themeType) {
        prefs.put("theme_type", themeType);
    }
}