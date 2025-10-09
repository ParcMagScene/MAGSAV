package com.magsav.model;

/**
 * Types d'entités dans le système unifié
 */
public enum EntityType {
    /**
     * Personne physique individuelle
     */
    PARTICULIER("Particulier"),
    
    /**
     * Société privée (peut proposer plusieurs services)
     */
    SOCIETE("Société"),
    
    /**
     * Administration publique (peut proposer plusieurs services)
     */
    ADMINISTRATION("Administration");
    
    private final String displayName;
    
    EntityType(String displayName) {
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