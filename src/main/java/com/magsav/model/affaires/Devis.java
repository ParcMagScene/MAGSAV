package com.magsav.model.affaires;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Modèle représentant un devis commercial
 */
public class Devis {
    private Long id;
    private String numero;
    private Long affaireId;
    private Long clientId;
    private String clientNom;
    
    // Informations du devis
    private String objet;
    private String description;
    private StatutDevis statut;
    private int version;
    
    // Montants
    private Double montantHT;
    private Double tauxTVA;
    private Double montantTVA;
    private Double montantTTC;
    private String deviseCode;
    
    // Dates
    private LocalDate dateCreation;
    private LocalDate dateValidite;
    private LocalDate dateAcceptation;
    private LocalDateTime derniereMiseAJour;
    
    // Conditions commerciales
    private String conditionsPaiement;
    private String delaiLivraison;
    private String validite;
    private String modalitesLivraison;
    
    // Responsables
    private String commercialRedacteur;
    private String validateur;
    
    // Lignes du devis
    private List<LigneDevis> lignes;
    
    // Constructeurs
    public Devis() {
        this.dateCreation = LocalDate.now();
        this.derniereMiseAJour = LocalDateTime.now();
        this.statut = StatutDevis.BROUILLON;
        this.version = 1;
        this.lignes = new ArrayList<>();
        this.tauxTVA = 20.0; // TVA par défaut
        this.deviseCode = "EUR";
    }
    
    public Devis(String objet, Long affaireId, Long clientId) {
        this();
        this.objet = objet;
        this.affaireId = affaireId;
        this.clientId = clientId;
    }
    
    /**
     * Constructeur de copie pour créer une nouvelle version d'un devis
     */
    public Devis(Devis original) {
        this.numero = original.numero; // Sera modifié par le service
        this.affaireId = original.affaireId;
        this.clientId = original.clientId;
        this.clientNom = original.clientNom;
        this.objet = original.objet;
        this.description = original.description;
        this.statut = StatutDevis.BROUILLON;
        this.version = original.version; // Sera incrémenté par le service
        this.montantHT = original.montantHT;
        this.tauxTVA = original.tauxTVA;
        this.montantTVA = original.montantTVA;
        this.montantTTC = original.montantTTC;
        this.deviseCode = original.deviseCode;
        this.dateCreation = LocalDate.now();
        this.dateValidite = null; // Sera définie par le service
        this.dateAcceptation = null;
        this.derniereMiseAJour = LocalDateTime.now();
        this.conditionsPaiement = original.conditionsPaiement;
        this.delaiLivraison = original.delaiLivraison;
        this.validite = original.validite;
        this.modalitesLivraison = original.modalitesLivraison;
        this.commercialRedacteur = original.commercialRedacteur;
        this.validateur = null;
        
        // Copie des lignes
        this.lignes = new ArrayList<>();
        if (original.lignes != null) {
            for (LigneDevis ligne : original.lignes) {
                LigneDevis nouvelleLigne = new LigneDevis();
                nouvelleLigne.setOrdre(ligne.getOrdre());
                nouvelleLigne.setDesignation(ligne.getDesignation());
                nouvelleLigne.setDescription(ligne.getDescription());
                nouvelleLigne.setReference(ligne.getReference());
                nouvelleLigne.setUnite(ligne.getUnite());
                nouvelleLigne.setQuantite(ligne.getQuantite());
                nouvelleLigne.setPrixUnitaireHT(ligne.getPrixUnitaireHT());
                nouvelleLigne.setTauxRemise(ligne.getTauxRemise());
                nouvelleLigne.setMontantRemise(ligne.getMontantRemise());
                nouvelleLigne.setMontantHT(ligne.getMontantHT());
                this.lignes.add(nouvelleLigne);
            }
        }
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    
    public Long getAffaireId() { return affaireId; }
    public void setAffaireId(Long affaireId) { this.affaireId = affaireId; }
    
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    
    public String getClientNom() { return clientNom; }
    public void setClientNom(String clientNom) { this.clientNom = clientNom; }
    
    public String getObjet() { return objet; }
    public void setObjet(String objet) { this.objet = objet; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public StatutDevis getStatut() { return statut; }
    public void setStatut(StatutDevis statut) { 
        this.statut = statut; 
        this.derniereMiseAJour = LocalDateTime.now();
        if (statut == StatutDevis.ACCEPTE && dateAcceptation == null) {
            this.dateAcceptation = LocalDate.now();
        }
    }
    
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    
    public Double getMontantHT() { return montantHT; }
    public void setMontantHT(Double montantHT) { 
        this.montantHT = montantHT;
        calculerMontants();
    }
    
    public Double getTauxTVA() { return tauxTVA; }
    public void setTauxTVA(Double tauxTVA) { 
        this.tauxTVA = tauxTVA;
        calculerMontants();
    }
    
    public Double getMontantTVA() { return montantTVA; }
    public void setMontantTVA(Double montantTVA) { this.montantTVA = montantTVA; }
    
    public Double getMontantTTC() { return montantTTC; }
    public void setMontantTTC(Double montantTTC) { this.montantTTC = montantTTC; }
    
    public String getDeviseCode() { return deviseCode; }
    public void setDeviseCode(String deviseCode) { this.deviseCode = deviseCode; }
    
    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }
    
    public LocalDate getDateValidite() { return dateValidite; }
    public void setDateValidite(LocalDate dateValidite) { this.dateValidite = dateValidite; }
    
    public LocalDate getDateAcceptation() { return dateAcceptation; }
    public void setDateAcceptation(LocalDate dateAcceptation) { this.dateAcceptation = dateAcceptation; }
    
    public LocalDateTime getDerniereMiseAJour() { return derniereMiseAJour; }
    public void setDerniereMiseAJour(LocalDateTime derniereMiseAJour) { this.derniereMiseAJour = derniereMiseAJour; }
    
    public String getConditionsPaiement() { return conditionsPaiement; }
    public void setConditionsPaiement(String conditionsPaiement) { this.conditionsPaiement = conditionsPaiement; }
    
    public String getDelaiLivraison() { return delaiLivraison; }
    public void setDelaiLivraison(String delaiLivraison) { this.delaiLivraison = delaiLivraison; }
    
    public String getValidite() { return validite; }
    public void setValidite(String validite) { this.validite = validite; }
    
    public String getModalitesLivraison() { return modalitesLivraison; }
    public void setModalitesLivraison(String modalitesLivraison) { this.modalitesLivraison = modalitesLivraison; }
    
    public String getCommercialRedacteur() { return commercialRedacteur; }
    public void setCommercialRedacteur(String commercialRedacteur) { this.commercialRedacteur = commercialRedacteur; }
    
    public String getValidateur() { return validateur; }
    public void setValidateur(String validateur) { this.validateur = validateur; }
    
    public List<LigneDevis> getLignes() { return lignes; }
    public void setLignes(List<LigneDevis> lignes) { 
        this.lignes = lignes;
        recalculerMontants();
    }
    
    // Méthodes utilitaires
    private String genererNumero() {
        return "DEV-" + LocalDate.now().getYear() + "-" + System.currentTimeMillis() % 10000;
    }
    
    private void calculerMontants() {
        if (montantHT != null && tauxTVA != null) {
            this.montantTVA = montantHT * tauxTVA / 100;
            this.montantTTC = montantHT + montantTVA;
        }
    }
    
    public void recalculerMontants() {
        if (lignes != null && !lignes.isEmpty()) {
            this.montantHT = lignes.stream()
                    .mapToDouble(ligne -> ligne.getMontantHT() != null ? ligne.getMontantHT() : 0.0)
                    .sum();
            calculerMontants();
        }
    }
    
    public void ajouterLigne(LigneDevis ligne) {
        if (lignes == null) {
            lignes = new ArrayList<>();
        }
        lignes.add(ligne);
        recalculerMontants();
    }
    
    public void supprimerLigne(LigneDevis ligne) {
        if (lignes != null) {
            lignes.remove(ligne);
            recalculerMontants();
        }
    }
    
    public boolean isModifiable() {
        return statut == StatutDevis.BROUILLON || statut == StatutDevis.EN_ATTENTE;
    }
    
    public boolean isValide() {
        return dateValidite == null || !dateValidite.isBefore(LocalDate.now());
    }
    
    public boolean isExpire() {
        return dateValidite != null && dateValidite.isBefore(LocalDate.now());
    }
    
    public int getNombreLignes() {
        return lignes != null ? lignes.size() : 0;
    }
    
    @Override
    public String toString() {
        return String.format("Devis{id=%d, numero='%s', objet='%s', statut=%s}", 
                           id, numero, objet, statut);
    }
}

