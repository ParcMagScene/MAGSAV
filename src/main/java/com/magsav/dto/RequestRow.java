package com.magsav.dto;

/**
 * DTO représentant une ligne de demande dans l'interface utilisateur
 */
public class RequestRow {
    private final long id;
    private final String type;
    private final String status;
    private final String fournisseurNom;
    private final String createdAt;
    private final String commentaire;
    private final String urgence;
    private final String description;
    
    public RequestRow(long id, String type, String status, String fournisseurNom, 
                     String createdAt, String commentaire, String urgence, String description) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.fournisseurNom = fournisseurNom;
        this.createdAt = createdAt;
        this.commentaire = commentaire;
        this.urgence = urgence;
        this.description = description;
    }
    
    // Getters
    public long id() { return id; }
    public String type() { return type; }
    public String status() { return status; }
    public String fournisseurNom() { return fournisseurNom; }
    public String createdAt() { return createdAt; }
    public String commentaire() { return commentaire; }
    public String urgence() { return urgence; }
    public String description() { return description; }
}