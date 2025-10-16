package com.magsav.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Représente une société/entreprise/administration avec tous ses détails
 */
public class Societe {
    private long id;
    private String typeSociete; // CLIENT, FOURNISSEUR, FABRICANT, PARTENAIRE
    private String nom;
    private String email;
    private String telephone;
    private String adresse;
    private String codePostal;
    private String ville;
    private String pays;
    private String siret;
    private String secteurActivite;
    private String siteWeb;
    private String notes;
    private boolean actif;
    private String dateCreation;
    private String dateModification;
    
    // Constructeurs
    public Societe() {
        this.actif = true;
        this.pays = "France";
        this.dateCreation = LocalDateTime.now().toString();
        this.dateModification = this.dateCreation;
    }
    
    public Societe(long id, String typeSociete, String nom, String email, 
                   String telephone, String adresse, String notes, String dateCreation) {
        this();
        this.id = id;
        this.typeSociete = typeSociete;
        this.nom = nom;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
        this.notes = notes;
        this.dateCreation = dateCreation;
    }
    
    // Getters et Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public String getTypeSociete() { return typeSociete; }
    public void setTypeSociete(String typeSociete) { 
        this.typeSociete = typeSociete; 
        this.dateModification = LocalDateTime.now().toString();
    }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { 
        this.nom = nom; 
        this.dateModification = LocalDateTime.now().toString();
    }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { 
        this.email = email; 
        this.dateModification = LocalDateTime.now().toString();
    }
    
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { 
        this.telephone = telephone; 
        this.dateModification = LocalDateTime.now().toString();
    }
    
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { 
        this.adresse = adresse; 
        this.dateModification = LocalDateTime.now().toString();
    }
    
    public String getCodePostal() { return codePostal; }
    public void setCodePostal(String codePostal) { 
        this.codePostal = codePostal; 
        this.dateModification = LocalDateTime.now().toString();
    }
    
    public String getVille() { return ville; }
    public void setVille(String ville) { 
        this.ville = ville; 
        this.dateModification = LocalDateTime.now().toString();
    }
    
    public String getPays() { return pays; }
    public void setPays(String pays) { 
        this.pays = pays; 
        this.dateModification = LocalDateTime.now().toString();
    }
    
    public String getSiret() { return siret; }
    public void setSiret(String siret) { 
        this.siret = siret; 
        this.dateModification = LocalDateTime.now().toString();
    }
    
    public String getSecteurActivite() { return secteurActivite; }
    public void setSecteurActivite(String secteurActivite) { 
        this.secteurActivite = secteurActivite; 
        this.dateModification = LocalDateTime.now().toString();
    }
    
    public String getSiteWeb() { return siteWeb; }
    public void setSiteWeb(String siteWeb) { 
        this.siteWeb = siteWeb; 
        this.dateModification = LocalDateTime.now().toString();
    }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { 
        this.notes = notes; 
        this.dateModification = LocalDateTime.now().toString();
    }
    
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { 
        this.actif = actif; 
        this.dateModification = LocalDateTime.now().toString();
    }
    
    public String getDateCreation() { return dateCreation; }
    public void setDateCreation(String dateCreation) { this.dateCreation = dateCreation; }
    
    public String getDateModification() { return dateModification; }
    public void setDateModification(String dateModification) { this.dateModification = dateModification; }
    
    // Méthodes utilitaires
    
    /**
     * Compatibilité avec l'ancien record - getter phone()
     */
    public String phone() { return telephone; }
    
    /**
     * Compatibilité avec l'ancien record - getter type()
     */
    public String type() { return typeSociete; }
    
    /**
     * Compatibilité avec l'ancien record - getter nom()
     */
    public String nom() { return nom; }
    
    /**
     * Compatibilité avec l'ancien record - getter email()
     */
    public String email() { return email; }
    
    /**
     * Compatibilité avec l'ancien record - getter adresse()
     */
    public String adresse() { return adresse; }
    
    /**
     * Compatibilité avec l'ancien record - getter notes()
     */
    public String notes() { return notes; }
    
    /**
     * Compatibilité avec l'ancien record - getter createdAt()
     */
    public String createdAt() { return dateCreation; }
    
    /**
     * Compatibilité avec l'ancien record - getter id()
     */
    public long id() { return id; }
    
    /**
     * Vérifie si cette société est un client
     */
    public boolean isClient() {
        return "CLIENT".equals(typeSociete);
    }
    
    /**
     * Retourne une représentation textuelle complète
     */
    @Override
    public String toString() {
        return String.format("%s (%s) - %s", nom, typeSociete, email);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Societe societe = (Societe) obj;
        return id == societe.id;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}