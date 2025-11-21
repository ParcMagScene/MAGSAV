package com.magsav.enums;

/**
 * Priorité d'une demande de matériel
 */
public enum RequestPriority {
    URGENT("Urgent"),
    HIGH("Haute"),
    MEDIUM("Moyenne"),
    LOW("Basse");
    
    private final String displayName;
    
    RequestPriority(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}