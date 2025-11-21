package com.magsav.entities;

/**
 * Énumération des protocoles de commande supportés par les fournisseurs
 */
public enum OrderProtocol {
    EMAIL("Email"),
    ONLINE("Plateforme en ligne"),
    PHONE("Téléphone"),
    FAX("Fax"),
    API("API/EDI");
    
    private final String displayName;
    
    OrderProtocol(String displayName) {
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