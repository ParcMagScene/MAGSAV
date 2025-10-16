package com.magsav.model;

/**
 * Modèle unifié pour représenter un client dans MAGSAV
 * Peut être un particulier ou une société/administration
 */
public record ClientUnifie(
    long id,
    ClientType type,
    
    // Informations communes
    String nom,                    // Nom/prénom ou raison sociale
    String email,
    String telephone,
    String adresse,
    String codePostal,
    String ville,
    String pays,
    
    // Informations spécifiques aux sociétés
    String raisonSociale,         // null pour les particuliers
    String siret,                 // null pour les particuliers  
    String secteur,               // null pour les particuliers
    String siteWeb,               // optionnel
    
    // Métadonnées
    String notes,
    boolean isActive,
    String dateCreation,
    String dateModification
) {
    
    /**
     * Constructeur pour un client particulier
     */
    public static ClientUnifie particulier(long id, String prenom, String nom, String email, 
                                         String telephone, String adresse, String codePostal, 
                                         String ville, String notes, String dateCreation) {
        return new ClientUnifie(
            id, ClientType.PARTICULIER,
            prenom + " " + nom, email, telephone, adresse, codePostal, ville, "France",
            null, null, null, null, // Pas d'infos société
            notes, true, dateCreation, dateCreation
        );
    }
    
    /**
     * Constructeur pour un client société
     */
    public static ClientUnifie societe(long id, String raisonSociale, String email, 
                                     String telephone, String adresse, String codePostal, 
                                     String ville, String siret, String secteur, String siteWeb,
                                     String notes, String dateCreation) {
        return new ClientUnifie(
            id, ClientType.SOCIETE,
            raisonSociale, email, telephone, adresse, codePostal, ville, "France",
            raisonSociale, siret, secteur, siteWeb,
            notes, true, dateCreation, dateCreation
        );
    }
    
    /**
     * Constructeur pour une administration
     */
    public static ClientUnifie administration(long id, String nomAdministration, String email, 
                                            String telephone, String adresse, String codePostal, 
                                            String ville, String siret, String notes, String dateCreation) {
        return new ClientUnifie(
            id, ClientType.ADMINISTRATION,
            nomAdministration, email, telephone, adresse, codePostal, ville, "France",
            nomAdministration, siret, "Administration publique", null,
            notes, true, dateCreation, dateCreation
        );
    }
    
    /**
     * Retourne le nom d'affichage formaté selon le type
     */
    public String getNomAffichage() {
        return type.getIcon() + " " + nom;
    }
    
    /**
     * Indique si ce client a des informations légales (SIRET, etc.)
     */
    public boolean hasLegalInfo() {
        return type.hasLegalInfo() && siret != null && !siret.trim().isEmpty();
    }
    
    /**
     * Retourne une description complète du client
     */
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(getNomAffichage());
        
        if (hasLegalInfo()) {
            sb.append(" (SIRET: ").append(siret).append(")");
        }
        
        if (email != null && !email.trim().isEmpty()) {
            sb.append(" - ").append(email);
        }
        
        return sb.toString();
    }
    
    /**
     * Convertit vers le modèle Societe existant pour compatibilité
     */
    public Societe toSociete() {
        return new Societe(
            id,
            type.toSocieteType(),
            nom,
            email,
            telephone,
            adresse + (ville != null ? ", " + ville : ""),
            notes,
            dateCreation
        );
    }
    
    /**
     * Crée un ClientUnifie depuis un modèle Societe existant
     */
    public static ClientUnifie fromSociete(Societe societe) {
        ClientType clientType = ClientType.fromSocieteType(societe.type());
        
        return new ClientUnifie(
            societe.id(),
            clientType,
            societe.nom(),
            societe.email(),
            societe.phone(),
            societe.adresse(),
            null, // code postal à extraire de l'adresse si nécessaire
            null, // ville à extraire de l'adresse si nécessaire  
            "France",
            clientType.hasLegalInfo() ? societe.nom() : null, // raison sociale
            null, // SIRET non disponible dans l'ancien modèle
            null, // secteur non disponible
            null, // site web non disponible
            societe.notes(),
            true,
            societe.createdAt(),
            societe.createdAt()
        );
    }
}