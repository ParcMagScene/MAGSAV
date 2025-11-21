package com.magsav.entities;

/**
 * Énumération du statut d'une allocation de commande
 */
public enum AllocationStatus {
    ALLOCATED("Allouée"),
    CONFIRMED("Confirmée"),
    PARTIALLY_DELIVERED("Partiellement livrée"),
    DELIVERED("Livrée"),
    CANCELLED("Annulée"),
    BACK_ORDERED("En attente fournisseur");
    
    private final String displayName;
    
    AllocationStatus(String displayName) {
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