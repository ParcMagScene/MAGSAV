package com.magscene.magsav.desktop.core.category;

/**
 * Types de catégories MAGSAV
 * 
 * @version 3.0.0-refactored
 */
public enum CategoryType {
    EQUIPMENT("Équipements", "🎵"),
    CLIENT("Clients", "👥"),
    PROJECT("Projets", "📋"),
    SAV("SAV", "🔧");
    
    private final String displayName;
    private final String icon;
    
    CategoryType(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getIcon() {
        return icon;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}