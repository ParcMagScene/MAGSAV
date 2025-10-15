package com.magsav.dto;

/**
 * DTO représentant une ligne d'utilisateur dans l'interface utilisateur
 */
public class UserRow {
    private final long id;
    private final String nom;
    private final String prenom;
    private final String email;
    private final String role;
    private final String statut;
    
    public UserRow(long id, String nom, String prenom, String email, String role, String statut) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.role = role;
        this.statut = statut;
    }
    
    // Getters
    public long getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getStatut() { return statut; }
}