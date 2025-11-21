package com.magsav.enums;

/**
 * Énumération du statut d'import d'un catalogue
 */
public enum ImportStatus {
    PENDING("En attente"),
    IN_PROGRESS("En cours"),
    COMPLETED("Terminé"),
    FAILED("Échec"),
    CANCELLED("Annulé");
    
    private final String displayName;
    
    ImportStatus(String displayName) {
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
