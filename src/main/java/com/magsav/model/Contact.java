package com.magsav.model;

import java.time.LocalDateTime;

/**
 * Représente un contact associé à une entité ou à un service
 */
public record Contact(
    long id,
    Long entityId,
    Long serviceId,
    String prenom,
    String nom,
    String fonction,
    String email,
    String phoneFixe,
    String phoneMobile,
    String notes,
    boolean principal,
    LocalDateTime createdAt
) {
    
    /**
     * Constructeur pour un nouveau contact d'entité (sans ID)
     */
    public static Contact createForEntity(long entityId, String prenom, String nom, String fonction, 
                                        String email, String phoneFixe, String phoneMobile, 
                                        String notes, boolean principal) {
        return new Contact(0, entityId, null, prenom, nom, fonction, email, phoneFixe, phoneMobile, notes, principal, null);
    }
    
    /**
     * Constructeur pour un nouveau contact de service (sans ID)
     */
    public static Contact createForService(long serviceId, String prenom, String nom, String fonction, 
                                         String email, String phoneFixe, String phoneMobile, 
                                         String notes, boolean principal) {
        return new Contact(0, null, serviceId, prenom, nom, fonction, email, phoneFixe, phoneMobile, notes, principal, null);
    }
    
    /**
     * Retourne le nom complet du contact
     */
    public String getFullName() {
        if (prenom != null && !prenom.trim().isEmpty()) {
            return prenom + " " + nom;
        }
        return nom;
    }
    
    /**
     * Indique si le contact est rattaché à une entité
     */
    public boolean isEntityContact() {
        return entityId != null && serviceId == null;
    }
    
    /**
     * Indique si le contact est rattaché à un service
     */
    public boolean isServiceContact() {
        return serviceId != null && entityId == null;
    }
    
    /**
     * Retourne le téléphone principal (mobile en priorité, sinon fixe)
     */
    public String getPrimaryPhone() {
        if (phoneMobile != null && !phoneMobile.trim().isEmpty()) {
            return phoneMobile;
        }
        return phoneFixe;
    }
}