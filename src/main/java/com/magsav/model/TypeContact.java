package com.magsav.model;

/**
 * Types de contacts dans le système MAGSAV
 */
public enum TypeContact {
    UTILISATEUR("👨‍💼", "Utilisateur", "Utilisateur du système MAGSAV"),
    CONTACT_SOCIETE("🏢", "Contact Société", "Contact d'un service/département d'une société"),
    CONTACT_PARTICULIER("👤", "Contact Particulier", "Contact personnel d'un particulier");
    
    private final String icone;
    private final String libelle;
    private final String description;
    
    TypeContact(String icone, String libelle, String description) {
        this.icone = icone;
        this.libelle = libelle;
        this.description = description;
    }
    
    public String getIcone() { return icone; }
    public String getLibelle() { return libelle; }
    public String getDescription() { return description; }
    
    public String getDisplayName() {
        return icone + " " + libelle;
    }
}