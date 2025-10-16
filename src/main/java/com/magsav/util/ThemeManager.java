package com.magsav.util;

import javafx.scene.Scene;
import javafx.scene.control.Alert;

/**
 * Utilitaire pour appliquer le thème dark unifié dans toute l'application
 */
public final class ThemeManager {
    
    private static final String DARK_THEME_CSS = "/css/simple-dark.css";
    
    private ThemeManager() {}
    
    /**
     * Applique le thème dark unifié à une scène
     */
    public static void applyDarkTheme(Scene scene) {
        if (scene != null) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(ThemeManager.class.getResource(DARK_THEME_CSS).toExternalForm());
        }
    }
    
    /**
     * Applique le thème dark unifié à une boîte de dialogue
     */
    public static void applyDarkTheme(Alert alert) {
        if (alert != null && alert.getDialogPane() != null) {
            alert.getDialogPane().getStylesheets().clear();
            alert.getDialogPane().getStylesheets().add(ThemeManager.class.getResource(DARK_THEME_CSS).toExternalForm());
        }
    }
    
    /**
     * Obtient l'URL du CSS du thème dark unifié
     */
    public static String getDarkThemeUrl() {
        return ThemeManager.class.getResource(DARK_THEME_CSS).toExternalForm();
    }
}