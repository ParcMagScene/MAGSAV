package com.magsav.model.affaires;

/**
 * Classe représentant une ligne de devis
 */
public class LigneDevis {
    private Long id;
    private Long devisId;
    private int ordre;
    
    private String designation;
    private String description;
    private String reference;
    private String unite;
    
    private Double quantite;
    private Double prixUnitaireHT;
    private Double tauxRemise;
    private Double montantRemise;
    private Double montantHT;
    
    // Constructeurs
    public LigneDevis() {}
    
    public LigneDevis(String designation, Double quantite, Double prixUnitaireHT) {
        this.designation = designation;
        this.quantite = quantite;
        this.prixUnitaireHT = prixUnitaireHT;
        this.tauxRemise = 0.0;
        calculerMontant();
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getDevisId() { return devisId; }
    public void setDevisId(Long devisId) { this.devisId = devisId; }
    
    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }
    
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    
    public String getUnite() { return unite; }
    public void setUnite(String unite) { this.unite = unite; }
    
    public Double getQuantite() { return quantite; }
    public void setQuantite(Double quantite) { 
        this.quantite = quantite;
        calculerMontant();
    }
    
    public Double getPrixUnitaireHT() { return prixUnitaireHT; }
    public void setPrixUnitaireHT(Double prixUnitaireHT) { 
        this.prixUnitaireHT = prixUnitaireHT;
        calculerMontant();
    }
    
    public Double getTauxRemise() { return tauxRemise; }
    public void setTauxRemise(Double tauxRemise) { 
        this.tauxRemise = tauxRemise;
        calculerMontant();
    }
    
    public Double getMontantRemise() { return montantRemise; }
    public void setMontantRemise(Double montantRemise) { this.montantRemise = montantRemise; }
    
    public Double getMontantHT() { return montantHT; }
    public void setMontantHT(Double montantHT) { this.montantHT = montantHT; }
    
    // Méthodes utilitaires
    private void calculerMontant() {
        if (quantite != null && prixUnitaireHT != null) {
            double montantBrut = quantite * prixUnitaireHT;
            if (tauxRemise != null && tauxRemise > 0) {
                this.montantRemise = montantBrut * tauxRemise / 100;
                this.montantHT = montantBrut - montantRemise;
            } else {
                this.montantRemise = 0.0;
                this.montantHT = montantBrut;
            }
        }
    }
    
    @Override
    public String toString() {
        return String.format("LigneDevis{designation='%s', quantite=%s, prixUnitaire=%s, montantHT=%s}", 
                           designation, quantite, prixUnitaireHT, montantHT);
    }
}