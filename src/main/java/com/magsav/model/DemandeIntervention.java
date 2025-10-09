package com.magsav.model;

/**
 * Représente une demande d'intervention en attente de validation
 */
public record DemandeIntervention(
    long id,
    
    // Statut et type
    StatutDemande statut,
    TypeDemande typeDemande,
    
    // Référence produit existant
    Long productId,
    
    // Données produit temporaires (nouveaux produits)
    String produitNom,
    String produitSn,
    String produitUid,
    String produitFabricant,
    String produitCategory,
    String produitSubcategory,
    String produitDescription,
    
    // Informations propriétaire
    TypeProprietaire typeProprietaire,
    Long proprietaireId,
    Long demandeCreationProprietaireId,
    String proprietaireNomTemp,
    String proprietaireDetailsTemp,
    
    // Détails intervention
    String panneDescription,
    String clientNote,
    String detecteur,
    Long detectorSocieteId,
    
    // Métadonnées
    String demandeurNom,
    String dateDemande,
    String dateValidation,
    String validateurNom,
    String notesValidation,
    
    // Intervention résultante
    Long interventionId
) {
    
    public enum StatutDemande {
        EN_ATTENTE("en_attente", "En attente"),
        VALIDEE("validee", "Validée"),
        REJETEE("rejetee", "Rejetée");
        
        private final String code;
        private final String libelle;
        
        StatutDemande(String code, String libelle) {
            this.code = code;
            this.libelle = libelle;
        }
        
        public String getCode() { return code; }
        public String getLibelle() { return libelle; }
        
        public static StatutDemande fromCode(String code) {
            for (StatutDemande statut : values()) {
                if (statut.code.equals(code)) return statut;
            }
            return EN_ATTENTE;
        }
    }
    
    public enum TypeDemande {
        PRODUIT_REPERTORIE("produit_repertorie", "Produit répertorié"),
        PRODUIT_NON_REPERTORIE("produit_non_repertorie", "Produit non-répertorié");
        
        private final String code;
        private final String libelle;
        
        TypeDemande(String code, String libelle) {
            this.code = code;
            this.libelle = libelle;
        }
        
        public String getCode() { return code; }
        public String getLibelle() { return libelle; }
        
        public static TypeDemande fromCode(String code) {
            for (TypeDemande type : values()) {
                if (type.code.equals(code)) return type;
            }
            return PRODUIT_NON_REPERTORIE;
        }
    }
    
    /**
     * Types de propriétaires possibles pour une demande d'intervention
     */
    public enum TypeProprietaire {
        MAG_SCENE("MAG_SCENE", "Mag Scène"),
        PARTICULIER("PARTICULIER", "Particulier"),
        SOCIETE("SOCIETE", "Société"),
        ADMINISTRATION("ADMINISTRATION", "Administration");
        
        private final String code;
        private final String libelle;
        
        TypeProprietaire(String code, String libelle) {
            this.code = code;
            this.libelle = libelle;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getLibelle() {
            return libelle;
        }
        
        @Override
        public String toString() {
            return libelle;
        }
        
        public static TypeProprietaire fromCode(String code) {
            if (code == null) return null;
            for (TypeProprietaire type : values()) {
                if (type.code.equals(code)) return type;
            }
            return null;
        }
    }
    
    /**
     * Vérifie si la demande concerne un produit existant
     */
    public boolean isProduitExistant() {
        return typeDemande == TypeDemande.PRODUIT_REPERTORIE && productId != null;
    }
    
    /**
     * Vérifie si la demande est en attente de validation
     */
    public boolean isEnAttente() {
        return statut == StatutDemande.EN_ATTENTE;
    }
    
    /**
     * Récupère le nom du produit (existant ou temporaire)
     */
    public String getNomProduit() {
        return produitNom != null ? produitNom : "";
    }
    
    /**
     * Vérifie si le propriétaire est Mag Scène
     */
    public boolean isProprietaireMagScene() {
        return typeProprietaire == TypeProprietaire.MAG_SCENE;
    }
    
    /**
     * Vérifie si le propriétaire existe déjà dans la base
     */
    public boolean isProprietaireExistant() {
        return proprietaireId != null;
    }
    
    /**
     * Vérifie si une demande de création de propriétaire est nécessaire
     */
    public boolean requireDemandeCreationProprietaire() {
        return !isProprietaireMagScene() && !isProprietaireExistant() && proprietaireNomTemp != null;
    }
    
    /**
     * Récupère le nom d'affichage du propriétaire
     */
    public String getProprietaireDisplayName() {
        if (isProprietaireMagScene()) {
            return "Mag Scène";
        }
        if (proprietaireNomTemp != null) {
            return proprietaireNomTemp + " (à créer)";
        }
        return "Propriétaire à définir";
    }
    
    /**
     * Récupère l'UID du produit (existant ou temporaire)
     */
    public String getUidProduit() {
        return produitUid != null ? produitUid : "";
    }
}