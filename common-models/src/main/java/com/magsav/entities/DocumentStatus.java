package com.magsav.entities;

/**
 * Énumération du statut de traitement des documents d'une commande fournisseur
 */
public enum DocumentStatus {
    WAITING_ACK("En attente d'accusé de réception"),
    ACK_RECEIVED("Accusé de réception reçu"),
    VALIDATED("Commande validée par fournisseur"),
    SHIPPED("Expédiée"),
    DELIVERED("Livrée"),
    PROBLEM("Problème détecté"),
    CANCELLED("Annulée");
    
    private final String displayName;
    
    DocumentStatus(String displayName) {
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