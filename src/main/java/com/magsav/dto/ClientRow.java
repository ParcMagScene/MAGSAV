package com.magsav.dto;

/**
 * DTO représentant une ligne de client dans l'interface utilisateur
 */
public class ClientRow {
    private final long id;
    private final String nom;
    private final String type;
    private final String email;
    private final String telephone;
    private final String ville;
    private final int nbInterventions;
    
    public ClientRow(long id, String nom, String type, String email, String telephone, String ville, int nbInterventions) {
        this.id = id;
        this.nom = nom;
        this.type = type;
        this.email = email;
        this.telephone = telephone;
        this.ville = ville;
        this.nbInterventions = nbInterventions;
    }
    
    public long getId() { return id; }
    public String getNom() { return nom; }
    public String getType() { return type; }
    public String getEmail() { return email; }
    public String getTelephone() { return telephone; }
    public String getVille() { return ville; }
    public int getNbInterventions() { return nbInterventions; }
    
    public String getTypeDisplay() {
        return "PARTICULIER".equals(type) ? "👤 Particulier" : "🏢 Société";
    }
}