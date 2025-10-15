package com.magsav.dto;

/**
 * DTO représentant une ligne de société dans l'interface utilisateur
 */
public class CompanyRow {
    private final long id;
    private final String nom;
    private final String type;
    private final String secteur;
    private final String ville;
    private final String siteweb;
    private final String email;
    private final String telephone;
    
    public CompanyRow(long id, String nom, String type, String secteur, String ville, String siteweb, String email, String telephone) {
        this.id = id;
        this.nom = nom;
        this.type = type;
        this.secteur = secteur;
        this.ville = ville;
        this.siteweb = siteweb;
        this.email = email;
        this.telephone = telephone;
    }
    
    public long getId() { return id; }
    public String getNom() { return nom; }
    public String getType() { return type; }
    public String getSecteur() { return secteur; }
    public String getVille() { return ville; }
    public String getSiteweb() { return siteweb; }
    public String getEmail() { return email; }
    public String getTelephone() { return telephone; }
    
    public String getContact() {
        if (email != null && !email.isEmpty()) {
            return email;
        } else if (telephone != null && !telephone.isEmpty()) {
            return telephone;
        }
        return "";
    }
}