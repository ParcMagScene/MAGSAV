package com.magsav.model;

/**
 * Record représentant une entité (Particulier, Société, Administration)
 */
public record Entity(
    long id,
    EntityType type,
    String nom,
    String email,
    String phone,
    String adresse,
    String siret,
    String tvaNumber,
    boolean actif
) {
    
    /**
     * Constructeur pour une nouvelle entité (sans ID)
     */
    public static Entity create(EntityType type, String nom, String email, String phone, String adresse, String siret, String tvaNumber, boolean actif) {
        return new Entity(0, type, nom, email, phone, adresse, siret, tvaNumber, actif);
    }
    
    /**
     * Crée une copie de l'entité avec un nouvel ID
     */
    public Entity withId(long newId) {
        return new Entity(newId, type, nom, email, phone, adresse, siret, tvaNumber, actif);
    }
    
    /**
     * Vérifie si cette entité peut avoir des services
     */
    public boolean canHaveServices() {
        return type == EntityType.SOCIETE || type == EntityType.ADMINISTRATION;
    }
    
    /**
     * Retourne le type sous forme de string pour la base de données
     */
    public String getTypeString() {
        return type.name();
    }
}