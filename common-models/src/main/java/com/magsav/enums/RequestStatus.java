package com.magsav.enums;

/**
 * Statut d'une demande de matériel
 */
public enum RequestStatus {
    DRAFT("Brouillon"),
    PENDING_APPROVAL("En attente d'approbation"),
    APPROVED("Approuvée"),
    REJECTED("Rejetée"),
    INTEGRATED("Intégrée aux commandes"),
    PARTIALLY_DELIVERED("Partiellement livrée"),
    COMPLETED("Terminée"),
    CANCELLED("Annulée");
    
    private final String displayName;
    
    RequestStatus(String displayName) {
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
