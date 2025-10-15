package com.magsav.dto;

/**
 * DTO représentant une ligne de catégorie dans l'interface utilisateur
 */
public class CategoryRow {
    private final long id;
    private final String nom;
    private final String description;
    private final int nbProduits;
    private final String dateCreation;
    
    public CategoryRow(long id, String nom, String description, int nbProduits, String dateCreation) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.nbProduits = nbProduits;
        this.dateCreation = dateCreation;
    }
    
    public long getId() { return id; }
    public String getNom() { return nom; }
    public String getDescription() { return description; }
    public int getNbProduits() { return nbProduits; }
    public String getDateCreation() { return dateCreation; }
}