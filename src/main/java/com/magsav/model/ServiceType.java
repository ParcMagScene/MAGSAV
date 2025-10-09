package com.magsav.model;

/**
 * Types de services proposés par les sociétés et administrations
 */
public enum ServiceType {
    /**
     * Service de vente/commerce
     */
    VENTE("Vente", "Service commercial et de distribution"),
    
    /**
     * Service après-vente externe
     */
    SAV("SAV", "Service après-vente et réparation"),
    
    /**
     * Service de fourniture de pièces détachées
     */
    PIECES("Pièces", "Fourniture de pièces détachées et composants"),
    
    /**
     * Service de fabrication/production
     */
    FABRICATION("Fabrication", "Production et manufacturing"),
    
    /**
     * Autre type de service
     */
    AUTRE("Autre", "Autre type de service");
    
    private final String displayName;
    private final String description;
    
    ServiceType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}