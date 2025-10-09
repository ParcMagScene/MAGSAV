package com.magsav.model;

import java.time.LocalDateTime;

/**
 * Représente un service proposé par une société ou une administration
 */
public record Service(
    long id,
    long entityId,
    ServiceType type,
    String nom,
    String description,
    boolean actif,
    LocalDateTime createdAt
) {
    
    /**
     * Constructeur pour un nouveau service (sans ID)
     */
    public static Service create(long entityId, ServiceType type, String description, boolean actif) {
        return new Service(0, entityId, type, null, description, actif, null);
    }
    
    /**
     * Constructeur pour un nouveau service actif (sans ID ni description)
     */
    public static Service create(long entityId, ServiceType type) {
        return new Service(0, entityId, type, null, null, true, null);
    }
    
    /**
     * Constructeur pour un nouveau service actif par défaut
     */
    public static Service create(long entityId, ServiceType type, String nom, String description) {
        return new Service(0, entityId, type, nom, description, true, null);
    }
    
    /**
     * Retourne le type de service sous forme de chaîne pour la BDD
     */
    public String getTypeString() {
        return type.name();
    }
    
    /**
     * Retourne le nom d'affichage complet du service
     */
    public String getDisplayName() {
        if (nom != null && !nom.trim().isEmpty()) {
            return nom + " (" + type.getDisplayName() + ")";
        }
        return type.getDisplayName();
    }
}