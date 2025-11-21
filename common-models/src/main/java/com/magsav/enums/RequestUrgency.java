package com.magsav.enums;

/**
 * Énumération du niveau d'urgence d'une demande
 */
public enum RequestUrgency {
    LOW("Faible"),
    NORMAL("Normale"),
    HIGH("Élevée"),
    URGENT("Urgente");
    
    private final String displayName;
    
    RequestUrgency(String displayName) {
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
