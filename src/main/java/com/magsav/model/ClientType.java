package com.magsav.model;

/**
 * Énumération pour distinguer les types de clients dans MAGSAV
 */
public enum ClientType {
    
    /**
     * Client particulier (personne physique)
     * - Individus, personnes privées
     * - Pas de SIRET, pas de raison sociale
     */
    PARTICULIER("Particulier", "👤", false),
    
    /**
     * Client société/entreprise privée
     * - Entreprises privées, PME, TPE
     * - Avec SIRET et raison sociale
     */
    SOCIETE("Société", "🏢", true),
    
    /**
     * Client administration publique
     * - Mairies, services publics, collectivités
     * - Avec numéro SIRET public
     */
    ADMINISTRATION("Administration", "🏛️", true),
    
    /**
     * Client partenaire commercial
     * - Partenaires commerciaux, sous-traitants
     * - Relations privilégiées
     */
    PARTENAIRE("Partenaire", "🤝", true);
    
    private final String label;
    private final String icon;
    private final boolean hasLegalInfo; // A des informations légales (SIRET, etc.)
    
    ClientType(String label, String icon, boolean hasLegalInfo) {
        this.label = label;
        this.icon = icon;
        this.hasLegalInfo = hasLegalInfo;
    }
    
    public String getLabel() {
        return label;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public boolean hasLegalInfo() {
        return hasLegalInfo;
    }
    
    public String getDisplayName() {
        return icon + " " + label;
    }
    
    /**
     * Convertit un type de société en type de client
     */
    public static ClientType fromSocieteType(String societeType) {
        return switch (societeType.toUpperCase()) {
            case "CLIENT" -> PARTICULIER; // Par défaut, les clients sont des particuliers
            case "COMPANY", "SOCIETE" -> SOCIETE;
            case "ADMINISTRATION", "PUBLIC" -> ADMINISTRATION;
            case "PARTENAIRE", "PARTNER" -> PARTENAIRE;
            default -> PARTICULIER;
        };
    }
    
    /**
     * Convertit un type de client vers un type de société
     */
    public String toSocieteType() {
        return switch (this) {
            case PARTICULIER -> "CLIENT";
            case SOCIETE -> "COMPANY";
            case ADMINISTRATION -> "ADMINISTRATION";
            case PARTENAIRE -> "PARTENAIRE";
        };
    }
}