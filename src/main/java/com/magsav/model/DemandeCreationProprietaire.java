package com.magsav.model;

import java.time.LocalDateTime;

/**
 * Modèle pour les demandes de création de propriétaires (particuliers, sociétés, administrations)
 * Ces demandes sont créées quand un propriétaire n'existe pas encore dans la base de données
 */
public record DemandeCreationProprietaire(
    Integer id,
    String nom,
    TypeProprietaire typeProprietaire,
    String email,
    String phone,
    String adresse,
    String notes,
    StatutDemandeProprietaire statut,
    String createdBy,
    LocalDateTime createdAt,
    LocalDateTime validatedAt,
    String validatedBy
) {
    
    /**
     * Types de propriétaires possibles
     */
    public enum TypeProprietaire {
        PARTICULIER("Particulier"),
        SOCIETE("Société"),
        ADMINISTRATION("Administration");
        
        private final String label;
        
        TypeProprietaire(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
        
        @Override
        public String toString() {
            return label;
        }
        
        public static TypeProprietaire fromString(String str) {
            if (str == null) return null;
            for (TypeProprietaire type : values()) {
                if (type.name().equals(str) || type.label.equals(str)) {
                    return type;
                }
            }
            return null;
        }
    }
    
    /**
     * Statuts possibles pour une demande de création de propriétaire
     */
    public enum StatutDemandeProprietaire {
        EN_ATTENTE("En attente"),
        VALIDEE("Validée"),
        REJETEE("Rejetée");
        
        private final String label;
        
        StatutDemandeProprietaire(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
        
        @Override
        public String toString() {
            return label;
        }
        
        public static StatutDemandeProprietaire fromString(String str) {
            if (str == null) return EN_ATTENTE;
            for (StatutDemandeProprietaire statut : values()) {
                if (statut.name().equalsIgnoreCase(str) || statut.label.equals(str)) {
                    return statut;
                }
            }
            return EN_ATTENTE;
        }
    }
    
    // Constructeur pour création d'une nouvelle demande
    public DemandeCreationProprietaire(String nom, TypeProprietaire typeProprietaire, 
                                     String email, String phone, String adresse, 
                                     String notes, String createdBy) {
        this(null, nom, typeProprietaire, email, phone, adresse, notes, 
             StatutDemandeProprietaire.EN_ATTENTE, createdBy, LocalDateTime.now(), null, null);
    }
    
    // Méthodes utilitaires
    public boolean isEnAttente() {
        return statut == StatutDemandeProprietaire.EN_ATTENTE;
    }
    
    public boolean isValidee() {
        return statut == StatutDemandeProprietaire.VALIDEE;
    }
    
    public boolean isRejetee() {
        return statut == StatutDemandeProprietaire.REJETEE;
    }
    
    public String getDisplayName() {
        return nom + " (" + typeProprietaire.getLabel() + ")";
    }
    
    /**
     * Crée une copie avec un nouveau statut
     */
    public DemandeCreationProprietaire withStatut(StatutDemandeProprietaire nouveauStatut, String validatedBy) {
        return new DemandeCreationProprietaire(
            id, nom, typeProprietaire, email, phone, adresse, notes,
            nouveauStatut, createdBy, createdAt, LocalDateTime.now(), validatedBy
        );
    }
}