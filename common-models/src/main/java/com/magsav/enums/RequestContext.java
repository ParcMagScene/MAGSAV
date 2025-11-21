package com.magsav.enums;

/**
 * Énumération du contexte d'une demande de matériel
 */
public enum RequestContext {
    SALES("Affaire commerciale"),
    INSTALLATION("Installation"),
    SAV("Service Après-Vente"),
    MAINTENANCE("Maintenance"),
    STOCK("Réapprovisionnement stock"),
    RESEARCH("Recherche & Développement"),
    EVENT("Événement/Spectacle"),
    TRAINING("Formation"),
    OTHER("Autre");
    
    private final String displayName;
    
    RequestContext(String displayName) {
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
