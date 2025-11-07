package com.magscene.magsav.desktop.theme;

import java.util.Arrays;
import java.util.List;

/**
 * Représente un thème d'interface utilisateur
 */
public class Theme {
    
    private final String id;
    private final String displayName;
    private final String description;
    private final List<String> cssFiles;
    private final boolean isDefault;
    
    /**
     * Constructeur pour un thème par défaut
     */
    public Theme(String id, String displayName, String description, String... cssFiles) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.cssFiles = Arrays.asList(cssFiles);
        this.isDefault = true;
    }
    
    /**
     * Constructeur pour un thème personnalisé
     */
    public Theme(String id, String displayName, String description, List<String> cssFiles, boolean isDefault) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.cssFiles = cssFiles;
        this.isDefault = isDefault;
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public List<String> getCssFiles() {
        return cssFiles;
    }
    
    public boolean isDefault() {
        return isDefault;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Theme theme = (Theme) obj;
        return id.equals(theme.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}