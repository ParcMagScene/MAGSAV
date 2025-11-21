package com.magsav.entities;

/**
 * Énumération du statut d'une commande groupée
 */
public enum GroupedOrderStatus {
    OPEN("Ouverte"),
    THRESHOLD_REACHED("Seuil atteint"),
    VALIDATED("Validée"),
    ORDERED("Commandée"),
    DELIVERED("Livrée"),
    CANCELLED("Annulée");
    
    private final String displayName;
    
    GroupedOrderStatus(String displayName) {
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