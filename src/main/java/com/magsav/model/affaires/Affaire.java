package com.magsav.model.affaires;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modèle représentant une affaire commerciale
 * Une affaire regroupe tous les éléments commerciaux liés à un client (devis, contrats, factures, etc.)
 */
public class Affaire {
    private Long id;
    private String reference;
    private String nom;
    private String description;
    private Long clientId;
    private String clientNom;
    private StatutAffaire statut;
    private TypeAffaire type;
    private PrioriteAffaire priorite;
    
    // Informations commerciales
    private Double montantEstime;
    private Double montantReel;
    private Double tauxMarge;
    private String deviseCode;
    
    // Dates importantes
    private LocalDate dateCreation;
    private LocalDate dateEcheance;
    private LocalDate dateFermeture;
    private LocalDateTime derniereMiseAJour;
    
    // Responsables
    private String commercialResponsable;
    private String technicienResponsable;
    private String chefProjet;
    
    // Notes et commentaires
    private String notes;
    private String commentairesInternes;
    
    // Constructeurs
    public Affaire() {
        this.dateCreation = LocalDate.now();
        this.derniereMiseAJour = LocalDateTime.now();
        this.statut = StatutAffaire.PROSPECTION;
        this.priorite = PrioriteAffaire.NORMALE;
        this.deviseCode = "EUR";
    }
    
    public Affaire(String reference, String nom, Long clientId) {
        this();
        this.reference = reference;
        this.nom = nom;
        this.clientId = clientId;
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    
    public String getClientNom() { return clientNom; }
    public void setClientNom(String clientNom) { this.clientNom = clientNom; }
    
    public StatutAffaire getStatut() { return statut; }
    public void setStatut(StatutAffaire statut) { 
        this.statut = statut; 
        this.derniereMiseAJour = LocalDateTime.now();
    }
    
    public TypeAffaire getType() { return type; }
    public void setType(TypeAffaire type) { this.type = type; }
    
    public PrioriteAffaire getPriorite() { return priorite; }
    public void setPriorite(PrioriteAffaire priorite) { this.priorite = priorite; }
    
    public Double getMontantEstime() { return montantEstime; }
    public void setMontantEstime(Double montantEstime) { this.montantEstime = montantEstime; }
    
    public Double getMontantReel() { return montantReel; }
    public void setMontantReel(Double montantReel) { this.montantReel = montantReel; }
    
    public Double getTauxMarge() { return tauxMarge; }
    public void setTauxMarge(Double tauxMarge) { this.tauxMarge = tauxMarge; }
    
    public String getDeviseCode() { return deviseCode; }
    public void setDeviseCode(String deviseCode) { this.deviseCode = deviseCode; }
    
    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }
    
    public LocalDate getDateEcheance() { return dateEcheance; }
    public void setDateEcheance(LocalDate dateEcheance) { this.dateEcheance = dateEcheance; }
    
    public LocalDate getDateFermeture() { return dateFermeture; }
    public void setDateFermeture(LocalDate dateFermeture) { this.dateFermeture = dateFermeture; }
    
    public LocalDateTime getDerniereMiseAJour() { return derniereMiseAJour; }
    public void setDerniereMiseAJour(LocalDateTime derniereMiseAJour) { this.derniereMiseAJour = derniereMiseAJour; }
    
    public String getCommercialResponsable() { return commercialResponsable; }
    public void setCommercialResponsable(String commercialResponsable) { this.commercialResponsable = commercialResponsable; }
    
    public String getTechnicienResponsable() { return technicienResponsable; }
    public void setTechnicienResponsable(String technicienResponsable) { this.technicienResponsable = technicienResponsable; }
    
    public String getChefProjet() { return chefProjet; }
    public void setChefProjet(String chefProjet) { this.chefProjet = chefProjet; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getCommentairesInternes() { return commentairesInternes; }
    public void setCommentairesInternes(String commentairesInternes) { this.commentairesInternes = commentairesInternes; }
    
    // Méthodes utilitaires
    public boolean isEnCours() {
        return statut == StatutAffaire.EN_COURS || statut == StatutAffaire.NEGOCIE;
    }
    
    public boolean isTerminee() {
        return statut == StatutAffaire.GAGNEE || statut == StatutAffaire.PERDUE || statut == StatutAffaire.ANNULEE;
    }
    
    public double getTauxReussite() {
        if (statut == StatutAffaire.GAGNEE) return 100.0;
        if (statut == StatutAffaire.PERDUE || statut == StatutAffaire.ANNULEE) return 0.0;
        return switch (statut) {
            case PROSPECTION -> 20.0;
            case QUALIFIEE -> 40.0;
            case EN_COURS -> 60.0;
            case NEGOCIE -> 80.0;
            default -> 0.0;
        };
    }
    
    public boolean isEchueOuEnRetard() {
        if (dateEcheance == null) return false;
        return dateEcheance.isBefore(LocalDate.now()) && !isTerminee();
    }
    
    @Override
    public String toString() {
        return String.format("Affaire{id=%d, reference='%s', nom='%s', statut=%s}", 
                           id, reference, nom, statut);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Affaire affaire = (Affaire) obj;
        return id != null && id.equals(affaire.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}

