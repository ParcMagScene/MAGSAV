package com.magsav.ui.icons;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import com.magsav.util.AppLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * Service d'icônes modernes pour MAGSAV
 * Support Material Design Icons et FontAwesome
 */
public class IconService {
    
    private static IconService instance;
    private final Map<String, String> materialIcons = new HashMap<>();
    private final Map<String, String> fontAwesomeIcons = new HashMap<>();
    
    // Tailles d'icônes standard
    public enum Size {
        SMALL(16),
        MEDIUM(24),
        LARGE(32),
        XLARGE(48);
        
        private final int pixels;
        
        Size(int pixels) {
            this.pixels = pixels;
        }
        
        public int getPixels() {
            return pixels;
        }
    }
    
    private IconService() {
        initializeMaterialIcons();
        initializeFontAwesomeIcons();
        AppLogger.info("IconService initialisé avec " + 
                      (materialIcons.size() + fontAwesomeIcons.size()) + " icônes");
    }
    
    public static IconService getInstance() {
        if (instance == null) {
            instance = new IconService();
        }
        return instance;
    }
    
    /**
     * Crée une icône Material Design
     */
    public Node createMaterialIcon(String iconName, Size size, Color color) {
        String path = materialIcons.get(iconName);
        if (path == null) {
            AppLogger.info("Icône Material non trouvée: " + iconName);
            return createFallbackIcon(iconName, size, color);
        }
        
        SVGPath icon = new SVGPath();
        icon.setContent(path);
        icon.setFill(color);
        
        // Redimensionner l'icône
        double scale = size.getPixels() / 24.0; // Material Icons sont 24x24 par défaut
        icon.setScaleX(scale);
        icon.setScaleY(scale);
        
        // Ajouter les classes CSS
        icon.getStyleClass().addAll("icon", "material-icon", "size-" + size.name().toLowerCase());
        
        return icon;
    }
    
    /**
     * Crée une icône Material Design avec couleur par défaut
     */
    public Node createMaterialIcon(String iconName, Size size) {
        return createMaterialIcon(iconName, size, Color.BLACK);
    }
    
    /**
     * Crée une icône Material Design taille moyenne
     */
    public Node createMaterialIcon(String iconName) {
        return createMaterialIcon(iconName, Size.MEDIUM);
    }
    
    /**
     * Crée une icône FontAwesome
     */
    public Node createFontAwesomeIcon(String iconName, Size size, Color color) {
        String unicode = fontAwesomeIcons.get(iconName);
        if (unicode == null) {
            AppLogger.info("Icône FontAwesome non trouvée: " + iconName);
            return createFallbackIcon(iconName, size, color);
        }
        
        Text icon = new Text(unicode);
        icon.setFill(color);
        icon.setFont(Font.font("FontAwesome", size.getPixels()));
        
        // Ajouter les classes CSS
        icon.getStyleClass().addAll("icon", "fontawesome-icon", "size-" + size.name().toLowerCase());
        
        return icon;
    }
    
    /**
     * Crée une icône de fallback simple
     */
    private Node createFallbackIcon(String iconName, Size size, Color color) {
        Text fallback = new Text("?");
        fallback.setFill(color);
        fallback.setFont(Font.font("System", size.getPixels()));
        fallback.getStyleClass().addAll("icon", "fallback-icon");
        
        AppLogger.info("Icône de fallback créée pour: " + iconName);
        return fallback;
    }
    
    /**
     * Crée une icône avec gestion automatique du type
     */
    public Node createIcon(String iconName, Size size, Color color) {
        // Essayer Material Design en premier
        if (materialIcons.containsKey(iconName)) {
            return createMaterialIcon(iconName, size, color);
        }
        
        // Puis FontAwesome
        if (fontAwesomeIcons.containsKey(iconName)) {
            return createFontAwesomeIcon(iconName, size, color);
        }
        
        // Fallback
        return createFallbackIcon(iconName, size, color);
    }
    
    /**
     * Crée une icône simple avec nom uniquement
     */
    public Node createIcon(String iconName) {
        return createIcon(iconName, Size.MEDIUM, Color.BLACK);
    }
    
    /**
     * Vérifie si une icône existe
     */
    public boolean hasIcon(String iconName) {
        return materialIcons.containsKey(iconName) || fontAwesomeIcons.containsKey(iconName);
    }
    
    /**
     * Initialise les icônes Material Design les plus utilisées
     */
    private void initializeMaterialIcons() {
        // Navigation
        materialIcons.put("home", "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z");
        materialIcons.put("menu", "M3 18h18v-2H3v2zm0-5h18v-2H3v2zm0-7v2h18V6H3z");
        materialIcons.put("arrow_back", "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z");
        materialIcons.put("arrow_forward", "M12 4l-1.41 1.41L16.17 11H4v2h12.17l-5.58 5.59L12 20l8-8z");
        materialIcons.put("close", "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z");
        
        // Actions
        materialIcons.put("add", "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z");
        materialIcons.put("edit", "M3 17.46v3.04c0 .28.22.5.5.5h3.04c.13 0 .26-.05.35-.15L17.81 9.94l-3.75-3.75L3.15 17.1c-.1.1-.15.22-.15.36zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z");
        materialIcons.put("delete", "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
        materialIcons.put("save", "M17 3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V7l-4-4zm-5 16c-1.66 0-3-1.34-3-3s1.34-3 3-3 3 1.34 3 3-1.34 3-3 3zm3-10H5V5h10v4z");
        materialIcons.put("search", "M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z");
        materialIcons.put("refresh", "M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z");
        
        // Fichiers et dossiers
        materialIcons.put("folder", "M10 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z");
        materialIcons.put("file", "M6 2c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 2 2h8l6-6V4c0-1.1-.9-2-2-2H6zm7 7V3.5L18.5 9H13z");
        materialIcons.put("upload", "M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z");
        materialIcons.put("download", "M5,20H19V18H5M19,9H15V3H9V9H5L12,16L19,9Z");
        
        // Paramètres et outils
        materialIcons.put("settings", "M19.14,12.94c0.04-0.3,0.06-0.61,0.06-0.94c0-0.32-0.02-0.64-0.07-0.94l2.03-1.58c0.18-0.14,0.23-0.41,0.12-0.61 l-1.92-3.32c-0.12-0.22-0.37-0.29-0.59-0.22l-2.39,0.96c-0.5-0.38-1.03-0.7-1.62-0.94L14.4,2.81c-0.04-0.24-0.24-0.41-0.48-0.41 h-3.84c-0.24,0-0.43,0.17-0.47,0.41L9.25,5.35C8.66,5.59,8.12,5.92,7.63,6.29L5.24,5.33c-0.22-0.08-0.47,0-0.59,0.22L2.74,8.87 C2.62,9.08,2.66,9.34,2.86,9.48l2.03,1.58C4.84,11.36,4.8,11.69,4.8,12s0.02,0.64,0.07,0.94l-2.03,1.58 c-0.18,0.14-0.23,0.41-0.12,0.61l1.92,3.32c0.12,0.22,0.37,0.29,0.59,0.22l2.39-0.96c0.5,0.38,1.03,0.7,1.62,0.94l0.36,2.54 c0.05,0.24,0.24,0.41,0.48,0.41h3.84c0.24,0,0.44-0.17,0.47-0.41l0.36-2.54c0.59-0.24,1.13-0.56,1.62-0.94l2.39,0.96 c0.22,0.08,0.47,0,0.59-0.22l1.92-3.32c0.12-0.22,0.07-0.47-0.12-0.61L19.14,12.94z M12,15.6c-1.98,0-3.6-1.62-3.6-3.6 s1.62-3.6,3.6-3.6s3.6,1.62,3.6,3.6S13.98,15.6,12,15.6z");
        materialIcons.put("info", "M12,2C6.48,2 2,6.48 2,12C2,17.52 6.48,22 12,22C17.52,22 22,17.52 22,12C22,6.48 17.52,2 12,2M13,17H11V11H13M13,9H11V7H13");
        materialIcons.put("warning", "M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z");
        materialIcons.put("error", "M12,2C6.48,2 2,6.48 2,12C2,17.52 6.48,22 12,22C17.52,22 22,17.52 22,12C22,6.48 17.52,2 12,2M15.5,14L14,15.5L12,13.5L10,15.5L8.5,14L10.5,12L8.5,10L10,8.5L12,10.5L14,8.5L15.5,10L13.5,12");
        
        // Communication
        materialIcons.put("email", "M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z");
        materialIcons.put("phone", "M6.62 10.79c1.44 2.83 3.76 5.14 6.59 6.59l2.2-2.2c.27-.27.67-.36 1.02-.24 1.12.37 2.33.57 3.57.57.55 0 1 .45 1 1V20c0 .55-.45 1-1 1-9.39 0-17-7.61-17-17 0-.55.45-1 1-1h3.5c.55 0 1 .45 1 1 0 1.25.2 2.45.57 3.57.11.35.03.74-.25 1.02l-2.2 2.2z");
        
        // Produits et inventaire
        materialIcons.put("inventory", "M20 2H4c-1 0-2 .9-2 2v3.01c0 .72.43 1.34 1 1.69V20c0 1.1 1.1 2 2 2h14c.9 0 2-.9 2-2V8.7c.57-.35 1-.97 1-1.69V4c0-1.1-1-2-2-2zM19 20H5V9h14v11zm1-13H4V4h16v3z");
        materialIcons.put("shopping_cart", "M7 18c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zM1 2v2h2l3.6 7.59-1.35 2.45c-.16.28-.25.61-.25.96 0 1.1.9 2 2 2h12v-2H7.42c-.14 0-.25-.11-.25-.25l.03-.12L8.1 13h7.45c.75 0 1.41-.41 1.75-1.03L21.7 4H5.21l-.94-2H1zm16 16c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z");
        
        AppLogger.info("Icônes Material Design initialisées: " + materialIcons.size());
    }
    
    /**
     * Initialise quelques icônes FontAwesome essentielles
     */
    private void initializeFontAwesomeIcons() {
        // Navigation de base
        fontAwesomeIcons.put("bars", "\uf0c9");
        fontAwesomeIcons.put("times", "\uf00d");
        fontAwesomeIcons.put("chevron-left", "\uf053");
        fontAwesomeIcons.put("chevron-right", "\uf054");
        fontAwesomeIcons.put("chevron-up", "\uf077");
        fontAwesomeIcons.put("chevron-down", "\uf078");
        
        // Actions
        fontAwesomeIcons.put("plus", "\uf067");
        fontAwesomeIcons.put("minus", "\uf068");
        fontAwesomeIcons.put("pencil", "\uf040");
        fontAwesomeIcons.put("trash", "\uf1f8");
        fontAwesomeIcons.put("save", "\uf0c7");
        fontAwesomeIcons.put("search", "\uf002");
        
        // États
        fontAwesomeIcons.put("check", "\uf00c");
        fontAwesomeIcons.put("exclamation", "\uf12a");
        fontAwesomeIcons.put("question", "\uf128");
        fontAwesomeIcons.put("info", "\uf129");
        
        AppLogger.info("Icônes FontAwesome initialisées: " + fontAwesomeIcons.size());
    }
    
    /**
     * Obtient la liste de toutes les icônes disponibles
     */
    public Map<String, String> getAllIcons() {
        Map<String, String> allIcons = new HashMap<>();
        for (String key : materialIcons.keySet()) {
            allIcons.put(key, "Material Design");
        }
        for (String key : fontAwesomeIcons.keySet()) {
            allIcons.put(key, "FontAwesome");
        }
        return allIcons;
    }
}