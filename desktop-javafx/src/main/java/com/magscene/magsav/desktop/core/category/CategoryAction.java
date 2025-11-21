package com.magscene.magsav.desktop.core.category;

/**
 * Actions possibles sur les catégories
 * 
 * @version 3.0.0-refactored
 */
public enum CategoryAction {
    ADDED("Ajoutée"),
    UPDATED("Modifiée"),
    REMOVED("Supprimée");
    
    private final String displayName;
    
    CategoryAction(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}