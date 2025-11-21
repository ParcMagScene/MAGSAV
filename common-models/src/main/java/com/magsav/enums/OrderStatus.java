package com.magsav.enums;

/**
 * Statut d'une commande groupée
 */
public enum OrderStatus {
    DRAFT("Brouillon"),
    PENDING_VALIDATION("En attente de validation"),
    VALIDATED("Validée"),
    SENT("Envoyée"),
    DELIVERED("Livrée"),
    CANCELLED("Annulée");
    
    private final String displayName;
    
    OrderStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}